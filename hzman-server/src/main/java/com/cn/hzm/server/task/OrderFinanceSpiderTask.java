package com.cn.hzm.server.task;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.finance.event.ShipmentEvent;
import com.cn.hzm.core.aws.resp.finance.ListFinancialEventsResponse;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderFinanceDO;
import com.cn.hzm.core.entity.thread.UpdateRecordFuture;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.OrderFinanceService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.cache.SaleInfoCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/18 10:26 上午
 */
@Slf4j
@Component
public class OrderFinanceSpiderTask {

    @Autowired
    private AwsClient awsClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderFinanceService orderFinanceService;

    @Autowired
    private DailyStatTask dailyStatTask;

    @Autowired
    private SaleInfoCache saleInfoCache;

    private Semaphore orderFinanceSemaphore;

    private ExecutorService updateThreadExecutor;

    @Value("${spider.switch:false}")
    private Boolean spiderSwitch;

    /**
     * 线程任务：无限爬取远端订单财务
     * todo 可以是订单更新之后的后续任务
     */
    @PostConstruct
    public void initTask() {
        if (!spiderSwitch) {
            log.info("测试环境关闭爬虫订单财务任务");
            return;
        }

        //订单商品爬取资源定时充能
        orderFinanceSemaphore = new Semaphore(30);
        ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderScheduledTask.scheduleAtFixedRate(() -> {
            if (orderFinanceSemaphore.availablePermits() < 30) {
                orderFinanceSemaphore.release(1);
            }
        }, 30, 2, TimeUnit.SECONDS);

        //批量更新订单线程
        // 等待队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1000);
        updateThreadExecutor = new ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS,
                workQueue, r -> new Thread(r, "update-order-thread"));


        //爬取订单任务
        ExecutorService createOrderTask = Executors.newSingleThreadExecutor();
        createOrderTask.execute(this::OrderFinanceSpider);
    }

    private void OrderFinanceSpider() {
        while (true) {
            try {
                List<OrderDO> orders = orderService.getOrdersByOrderStatusAndFinanceStatus(ContextConst.AMAZON_STATUS_SHIPPED, 0, 5000);
                if (!CollectionUtils.isEmpty(orders)) {
                    Set<String> needFixSaleInfoDay = Sets.newHashSet();
                    doSpiderOrderFinance(needFixSaleInfoDay, orders);

                    if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                        dailyStatTask.statSaleInfoByMulchDate(needFixSaleInfoDay);
                        saleInfoCache.refreshDailySaleInfo(needFixSaleInfoDay);
                    }
                }

                if (orders.size() < 5000) {
                    log.info("待爬取订单财务信息数量较少，休息10分钟");
                    Thread.sleep(10 * 60 * 1000);
                }
            } catch (HzmException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("爬虫任务触发限流");
                }
            } catch (Exception e) {
                log.error("订单财务信息爬取失败：", e);
            }
        }
    }

    private void doSpiderOrderFinance(Set<String> needFixSaleInfoDay, List<OrderDO> totalOrders) {
        log.info("数据库中需要爬取订单财务信息数量：{}", totalOrders.size());

        int dbOrderNum = totalOrders.size();
        int limit = 50;
        int dealTimes = dbOrderNum / 50;
        long startTime = System.currentTimeMillis();

        List<Callable<UpdateRecordFuture>> callables = Lists.newArrayList();
        for (int curNum = 0; curNum <= dealTimes; curNum++) {
            int start = curNum * limit;
            int end = Math.min(dbOrderNum, (curNum + 1) * limit);
            List<OrderDO> subOrders = totalOrders.subList(start, end);
            callables.add(() -> {
                //获取资源
                log.info("处理更新财务数据 start【{}】 end【{}】", start, end);
                Set<String> threadFixSaleInfoDay = Sets.newHashSet();
                int updateCount = 0;

                for (OrderDO order : subOrders) {
                    try {
                        orderFinanceSemaphore.acquire();
                        ListFinancialEventsResponse response = awsClient.getFinanceByOrderId(order.getAmazonOrderId());
                        if (response == null) {
                            continue;
                        }

                        if (response.getListFinancialEventsResult().getFinancialEvents().getShipmentEventList().getList() == null) {
                            log.info("订单财务信息 amazonOrderId【{}】purchaseTime:【{}】为空", order.getAmazonOrderId(), order.getPurchaseDate());
                            continue;
                        }

                        //设置null
                        response.getListFinancialEventsResult().getFinancialEvents().getShipmentEventList()
                                .getList().forEach(se -> se.getShipmentItemList().getList().forEach(si -> si.setPromotionList(null)));

                        Boolean result;
                        OrderFinanceDO orderFinanceDO = orderFinanceService.getOrderFinanceByAmazonId(order.getAmazonOrderId());
                        if (orderFinanceDO == null) {
                            JSONObject jo = (JSONObject) JSONObject.toJSON(response.getListFinancialEventsResult().getFinancialEvents());
                            orderFinanceDO = JSONObject.toJavaObject(jo, OrderFinanceDO.class);
                            jo.remove("shipmentEventList");
                            orderFinanceDO.setOther_event_list(jo.toJSONString());
                            orderFinanceDO.setAmazonOrderId(order.getAmazonOrderId());
                            result = orderFinanceService.createOrderFinance(orderFinanceDO);
                        } else {
                            JSONObject jo = (JSONObject) JSONObject.toJSON(response.getListFinancialEventsResult().getFinancialEvents());
                            OrderFinanceDO newOrderFinanceDO = JSONObject.toJavaObject(jo, OrderFinanceDO.class);
                            jo.remove("shipmentEventList");
                            newOrderFinanceDO.setOther_event_list(jo.toJSONString());
                            newOrderFinanceDO.setAmazonOrderId(order.getAmazonOrderId());
                            newOrderFinanceDO.setId(orderFinanceDO.getId());
                            newOrderFinanceDO.setCtime(orderFinanceDO.getCtime());
                            result = orderFinanceService.updateOrderFinance(orderFinanceDO);
                        }

                        if (result) {
                            log.info("更新订单财务信息 amazonOrderId【{}】purchaseTime:【{}】", order.getAmazonOrderId(), order.getPurchaseDate());
                            threadFixSaleInfoDay.add(TimeUtil.getSimpleFormat(order.getPurchaseDate()));
                            updateCount++;

                            //更新订单
                            order.setIsFinance(1);
                            orderService.updateOrder(order);
                        }
                    } catch (Exception e) {
                        log.error("更新订单【{}】财务错误：{}", order.getAmazonOrderId(), e.getMessage(), e);
                    }
                }
                return new UpdateRecordFuture(threadFixSaleInfoDay, updateCount);
            });
        }

        try {
            List<Future<UpdateRecordFuture>> returnFutures = updateThreadExecutor.invokeAll(callables);
            int updateAll = 0;
            for (Future<UpdateRecordFuture> future : returnFutures) {
                UpdateRecordFuture facade;
                try {
                    facade = future.get();
                    needFixSaleInfoDay.addAll(facade.getThreadFixSaleInfoDay());
                    updateAll += facade.getCount();
                } catch (Exception e) {
                    log.error("error:", e);
                }
            }
            log.info("更新订单财务任务结束，本次任务耗时：{}, 共更新订单【{}】条", System.currentTimeMillis() - startTime, updateAll);
        } catch (Exception e) {
            log.error("更新订单错误：{}", e.getMessage(), e);
        }
    }
}
