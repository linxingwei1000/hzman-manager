package com.cn.hzm.server.task;

import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.order.Order;
import com.cn.hzm.core.aws.domain.order.OrderItem;
import com.cn.hzm.core.aws.resp.order.*;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.entity.thread.UpdateRecordFuture;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.cache.SaleInfoCache;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.service.OperateDependService;
import com.cn.hzm.server.util.ConvertUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/18 10:26 上午
 */
@Slf4j
@Component
public class OrderSpiderTask {

    @Autowired
    private OperateDependService operateDependService;

    @Autowired
    private AwsClient awsClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private DailyStatTask dailyStatTask;

    @Autowired
    private SaleInfoCache saleInfoCache;

    @Autowired
    private ItemDealService itemDealService;

    private Semaphore orderSemaphore;

    private Semaphore getOrderSemaphore;

    private Semaphore orderItemSemaphore;

    private ExecutorService updateThreadExecutor;

    @Value("${spider.switch:false}")
    private Boolean spiderSwitch;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;


    /**
     * 单个amazonId爬取任务
     *
     */
    public void updateAmazonOrder(List<String> amazonIds) {
        List<OrderDO> orders = orderService.getOrdersByAmazonIds(amazonIds);
        if (!CollectionUtils.isEmpty(orders)) {

            log.info("修复亚马逊订单错误数据：{}", orders.size());
            long bTime = System.currentTimeMillis();
            Set<String> needFixSaleInfoDay = Sets.newHashSet();
            doUpdateOrder(needFixSaleInfoDay, orders);

            if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                dailyStatTask.statSaleInfoByMulchDate(needFixSaleInfoDay);
                saleInfoCache.refreshDailySaleInfo(needFixSaleInfoDay);
            }
            log.info("修复亚马逊订单错误数据完成，耗时：{}", System.currentTimeMillis() - bTime);
        }
    }

    /**
     * 线程任务：无限爬取远端订单
     */
    @PostConstruct
    public void initTask() {
        if (!spiderSwitch) {
            log.info("测试环境关闭爬虫任务");
            return;
        }

        //订单商品爬取资源定时充能
        orderSemaphore = new Semaphore(6);
        ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderScheduledTask.scheduleAtFixedRate(() -> {
            if (orderSemaphore.availablePermits() < 6) {
                orderSemaphore.release(1);
            }
        }, 60, 60, TimeUnit.SECONDS);

        //订单商品爬取资源定时充能
        getOrderSemaphore = new Semaphore(6);
        ScheduledThreadPoolExecutor getOrderScheduledTask = new ScheduledThreadPoolExecutor(1);
        getOrderScheduledTask.scheduleAtFixedRate(() -> {
            if (getOrderSemaphore.availablePermits() < 6) {
                getOrderSemaphore.release(1);
            }
        }, 60, 60, TimeUnit.SECONDS);

        //订单商品爬取资源定时充能
        orderItemSemaphore = new Semaphore(30);
        ScheduledThreadPoolExecutor scheduledTask = new ScheduledThreadPoolExecutor(1);
        scheduledTask.scheduleAtFixedRate(() -> {
            if (orderItemSemaphore.availablePermits() < 30) {
                orderItemSemaphore.release(1);
            }
        }, 60, 2, TimeUnit.SECONDS);

        //批量更新订单线程
        // 等待队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1000);
        updateThreadExecutor = new ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS,
                workQueue, r -> new Thread(r, "update-order-thread"));


        //爬取订单任务
        ExecutorService createOrderTask = Executors.newSingleThreadExecutor();
        createOrderTask.execute(this::createOrderSpider);

        //更新订单任务
        ExecutorService updateOrderSpider = Executors.newSingleThreadExecutor();
        updateOrderSpider.execute(this::updateOrderSpider);
    }

    private void createOrderSpider() {
        while (true) {
            try {
                doSpiderOrder();
            } catch (HzmException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("爬虫任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateOrderSpider() {
        while (true) {
            try {
                List<OrderDO> orders = orderService.getOrdersByOrderStatus(ContextConst.AMAZON_STATUS_PENDING);
                if (!CollectionUtils.isEmpty(orders)) {

                    Set<String> needFixSaleInfoDay = Sets.newHashSet();
                    doUpdateOrder(needFixSaleInfoDay, orders);

                    if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                        dailyStatTask.statSaleInfoByMulchDate(needFixSaleInfoDay);
                        saleInfoCache.refreshDailySaleInfo(needFixSaleInfoDay);
                    }
                }

                Thread.sleep(10 * 60 * 1000);
            } catch (HzmException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("订单状态更新任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void doUpdateOrder(Set<String> needFixSaleInfoDay, List<OrderDO> totalOrders) {
        log.info("数据库中需要更新订单的数量：{}", totalOrders.size());

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
                log.info("处理更新订单 start【{}】 end【{}】", start, end);
                Set<String> threadFixSaleInfoDay = Sets.newHashSet();
                int updateCount = 0;
                try {
                    List<String> subIds = subOrders.stream().map(OrderDO::getAmazonOrderId).collect(Collectors.toList());
                    getOrderSemaphore.acquire();
                    GetOrderResponse orderResponse = awsClient.getOrder(subIds);

                    //剔除Pending状态订单
                    List<Order> tmp = orderResponse.getGetOrderResult().getOrders().getList().stream()
                            .filter(order -> !ContextConst.AMAZON_STATUS_PENDING.equals(order.getOrderStatus()))
                            .collect(Collectors.toList());

                    Map<String, OrderDO> orderMap = subOrders.stream().collect(Collectors.toMap(OrderDO::getAmazonOrderId, orderDO -> orderDO));
                    for (Order order : tmp) {
                        String amazonId = order.getAmazonOrderId();

                        //获取资源
                        orderItemSemaphore.acquire();
                        ListOrderItemsResponse tokenResponse = awsClient.getListOrderItemsByAmazonId(amazonId);
                        //网络问题引起数据无法获取
                        if (tokenResponse == null) {
                            continue;
                        }

                        parseOrderItem(tokenResponse.getListOrderItemsResult().getOrderItems().getList(), amazonId);

                        String nextToken = tokenResponse.getListOrderItemsResult().getNextToken();
                        while (!StringUtils.isEmpty(nextToken)) {

                            //获取资源
                            orderItemSemaphore.acquire();
                            ListOrderItemsByNextTokenResponse response = awsClient.getListOrderItemByAmazonIds(nextToken);

                            nextToken = response.getListOrderItemsByNextTokenResult().getNextToken();
                            parseOrderItem(response.getListOrderItemsByNextTokenResult().getOrderItems().getList(), amazonId);
                        }

                        //最后处理，保证订单商品更新完成
                        OrderDO old = orderMap.get(amazonId);
                        OrderDO update = new OrderDO();
                        update.setId(old.getId());
                        update.setOtherConfig(old.getOtherConfig());
                        ConvertUtil.convertToOrderDO(update, order);
                        orderService.updateOrder(update);

                        log.info("更新订单状态 amazonOrderId【{}】purchaseTime:【{}】", amazonId, update.getPurchaseDate());
                        threadFixSaleInfoDay.add(TimeUtil.getSimpleFormat(TimeUtil.transform(order.getPurchaseDate())));
                        updateCount++;
                    }
                } catch (Exception e) {
                    log.error("更新订单错误：{}", e.getMessage(), e);
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
            log.info("更新订单任务结束，本次任务耗时：{}, 共更新订单【{}】条", System.currentTimeMillis() - startTime, updateAll);
        } catch (Exception e) {
            log.error("更新订单错误：{}", e.getMessage(), e);
        }
    }

    private void doSpiderOrder() throws ParseException, InterruptedException {
        log.info("爬取订单任务开始");
        long startTime = System.currentTimeMillis();
        String strBeginDate = operateDependService.getValueByKey(ContextConst.OPERATE_SPIDER_CREATE_ORDER);

        Date beginDate = TimeUtil.transformUTCToDate(strBeginDate);
        Date endDate = TimeUtil.dateFixByDay(beginDate, 0, 0, 30);
        String strEndDate = TimeUtil.dateToUTC(endDate);
        int total = 0;

        if (System.currentTimeMillis() - beginDate.getTime() < DURATION_SECOND) {
            //如果拉取当天数据，每20分钟执行一次
            log.info("爬取订单任务未满足时间条件，退出本次任务，休息20分钟");
            Thread.sleep(20 * 60 * 1000);
            return;
        }

        //获取资源
        orderSemaphore.acquire();
        ListOrdersResponse r = awsClient.getListOrder(strBeginDate, strEndDate, true);
        if (r == null) {
            log.info("爬取订单任务异常结束，耗时：{}，爬取时间范围：{}--{}, 爬取总数：{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate, total);
            return;
        }

        List<String> amazonIds = parseOrderResp(r.getListOrdersResult().getOrders().getList());
        log.info("时间段【{}】---【{}】爬取订单数量：{}", strBeginDate, strEndDate, amazonIds.size());
        getOrderItems(amazonIds);
        total += amazonIds.size();

        String nextToken = r.getListOrdersResult().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            orderSemaphore.acquire();
            ListOrdersByNextTokenResponse tokenResponse = awsClient.getListOrderByToken(nextToken);
            nextToken = tokenResponse.getListOrdersByNextTokenResult().getNextToken();

            List<String> tmpIds = parseOrderResp(tokenResponse.getListOrdersByNextTokenResult().getOrders().getList());
            log.info("时间段【{}】---【{}】nextToken 爬取订单数量：{}", strBeginDate, strEndDate, amazonIds.size());
            getOrderItems(tmpIds);
            total += tmpIds.size();
        }

        operateDependService.updateValueByKey(ContextConst.OPERATE_SPIDER_CREATE_ORDER, strEndDate);
        log.info("爬取订单任务结束，耗时：{}，爬取时间范围：{}--{}, 爬取总数：{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate, total);
    }

    /**
     * 解析order为本数据库所用
     *
     * @param list
     * @return
     */
    private List<String> parseOrderResp(List<Order> list) throws ParseException {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }

        List<String> amazonIds = Lists.newArrayList();
        for (Order order : list) {
            amazonIds.add(order.getAmazonOrderId());

            OrderDO old = orderService.getOrderByAmazonId(order.getAmazonOrderId());
            if (old == null) {
                orderService.createOrder(ConvertUtil.convertToOrderDO(new OrderDO(), order));
            }
        }
        return amazonIds;
    }

    /**
     * 获取订单商品信息
     *
     * @param amazonIds
     * @throws InterruptedException
     */
    private void getOrderItems(List<String> amazonIds) throws InterruptedException, ParseException {
        for (String amazonId : amazonIds) {
            //获取资源
            orderItemSemaphore.acquire();
            ListOrderItemsResponse tokenResponse = awsClient.getListOrderItemsByAmazonId(amazonId);
            parseOrderItem(tokenResponse.getListOrderItemsResult().getOrderItems().getList(), amazonId);

            String nextToken = tokenResponse.getListOrderItemsResult().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                //获取资源
                orderItemSemaphore.acquire();
                ListOrderItemsByNextTokenResponse response = awsClient.getListOrderItemByAmazonIds(nextToken);
                nextToken = response.getListOrderItemsByNextTokenResult().getNextToken();
                parseOrderItem(response.getListOrderItemsByNextTokenResult().getOrderItems().getList(), amazonId);
            }
        }
    }

    private void parseOrderItem(List<OrderItem> list, String amazonId) throws ParseException {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (OrderItem orderItem : list) {
            OrderItemDO old = orderItemService.getOrderItemByOrderItemId(orderItem.getOrderItemId());
            OrderItemDO update = new OrderItemDO();
            if (old != null) {
                //已经处理过订单，不做任何操作
                update.setId(old.getId());
                update.setOtherConfig(old.getOtherConfig());
                orderItemService.updateOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));
            } else {
                orderItemService.createOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));

                //爬取商品信息
                itemDealService.processSync(orderItem.getSellerSKU());

                //刷新库存
                log.info("amazonOrderId【{}】 sku【{}】刷新库存", amazonId, orderItem.getSellerSKU());
                itemDealService.dealSkuInventory(orderItem.getSellerSKU(), "refresh", 0);
            }
        }
    }


}
