package com.cn.hzm.core.task;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.cache.SaleInfoCache;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.thread.UpdateRecordFuture;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.processor.DailyStatProcessor;
import com.cn.hzm.core.repository.dao.AmazonOrderDao;
import com.cn.hzm.core.repository.dao.AmazonOrderFinanceDao;
import com.cn.hzm.core.repository.dao.AmazonOrderItemDao;
import com.cn.hzm.core.repository.dao.AwsSpiderTaskDao;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.spa.SpaManager;
import com.cn.hzm.core.spa.finance.model.ListFinancialEventsResponse;
import com.cn.hzm.core.spa.order.model.*;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
public class OrderSpiderTask implements ITask{

    private SpaManager spaManager;

    private Integer spiderTaskId;

    private AwsSpiderTaskDao awsSpiderTaskDao;

    private AmazonOrderDao amazonOrderDao;

    private AmazonOrderItemDao amazonOrderItemDao;

    private AmazonOrderFinanceDao amazonOrderFinanceDao;

    private ItemService itemService;

    private DailyStatProcessor dailyStatProcessor;

    private SaleInfoCache saleInfoCache;

    private Semaphore orderSemaphore;

    private Semaphore getOrderSemaphore;

    private Semaphore orderItemSemaphore;

    private Semaphore orderFinanceSemaphore;

    private ExecutorService updateThreadExecutor;

    private ExecutorService updateFinanceThreadExecutor;

    private volatile Boolean spiderSwitch;

    private volatile boolean closeSwitch;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;

    public OrderSpiderTask(SpaManager spaManager, Integer spiderTaskId,
                           AwsSpiderTaskDao awsSpiderTaskDao, AmazonOrderDao amazonOrderDao, AmazonOrderItemDao amazonOrderItemDao,
                           AmazonOrderFinanceDao amazonOrderFinanceDao, ItemService itemService, DailyStatProcessor dailyStatProcessor,
                           SaleInfoCache saleInfoCache, boolean spiderSwitch) {
        this.spaManager = spaManager;
        this.spiderTaskId = spiderTaskId;
        this.awsSpiderTaskDao = awsSpiderTaskDao;
        this.amazonOrderDao = amazonOrderDao;
        this.amazonOrderItemDao = amazonOrderItemDao;
        this.amazonOrderFinanceDao = amazonOrderFinanceDao;
        this.itemService = itemService;
        this.dailyStatProcessor = dailyStatProcessor;
        this.saleInfoCache = saleInfoCache;
        this.spiderSwitch = spiderSwitch;
    }

    @Override
    public void setSpiderSwitch(boolean spiderSwitch) {
        this.spiderSwitch = spiderSwitch;
    }

    @Override
    public void start() {
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
        }, 60000, 4500, TimeUnit.MILLISECONDS);

        //订单商品爬取资源定时充能
        orderFinanceSemaphore = new Semaphore(30);
        ScheduledThreadPoolExecutor orderFinanceScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderFinanceScheduledTask.scheduleAtFixedRate(() -> {
            if (orderFinanceSemaphore.availablePermits() < 30) {
                orderFinanceSemaphore.release(1);
            }
        }, 30, 4, TimeUnit.SECONDS);

        //批量更新订单财务线程
        updateFinanceThreadExecutor = new ThreadPoolExecutor(2, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000), r -> new Thread(r, "update-order-finance-thread"));


        //批量更新订单线程
        updateThreadExecutor = new ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000), r -> new Thread(r, "update-order-thread"));


        //爬取订单任务
        ExecutorService createOrderTask = Executors.newSingleThreadExecutor();
        createOrderTask.execute(this::createOrderSpider);

        //更新订单任务
        ExecutorService updateOrderSpider = Executors.newSingleThreadExecutor();
        updateOrderSpider.execute(this::updateOrderSpider);

        //爬取订单任务
        ExecutorService updateOrderFinanceTask = Executors.newSingleThreadExecutor();
        updateOrderFinanceTask.execute(this::orderFinanceSpider);
    }

    @Override
    public void close() {
        closeSwitch = true;
    }

    /**
     * amazonId爬取任务能力
     */
    @Override
    public String spideData(List<String> amazonIds) {
        List<AmazonOrderDo> orders = amazonOrderDao.getOrdersByAmazonIds(amazonIds);
        if (!CollectionUtils.isEmpty(orders)) {

            log.info("修复亚马逊订单错误数据：{}", orders.size());
            long bTime = System.currentTimeMillis();
            Set<String> needFixSaleInfoDay = Sets.newHashSet();
            doUpdateOrder(needFixSaleInfoDay, orders);

            if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                dailyStatProcessor.statSaleInfoByMulchDate(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
                saleInfoCache.refreshDailySaleInfo(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
            }
            log.info("修复亚马逊订单错误数据完成，耗时：{}", System.currentTimeMillis() - bTime);
        }
        return null;
    }

    private void createOrderSpider() {
        while (true) {
            try {
                if (!spiderSwitch) {
                    log.info("订单爬取任务已被停止，等待激活重试");
                    Thread.sleep(10* 60 * 1000);
                    continue;
                }

                if(closeSwitch){
                    log.info("订单爬取任务已被关闭");
                    break;
                }

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
                if (!spiderSwitch) {
                    log.info("订单爬取任务已被停止，等待激活重试");
                    Thread.sleep(10 * 60 * 1000);
                    continue;
                }

                if(closeSwitch){
                    log.info("订单爬取任务已被关闭");
                    break;
                }

                List<AmazonOrderDo> orders = amazonOrderDao.getOrdersByOrderStatus(spaManager.getAwsUserMarketId(), Order.OrderStatusEnum.PENDING.getValue());
                if (!CollectionUtils.isEmpty(orders)) {

                    Set<String> needFixSaleInfoDay = Sets.newHashSet();
                    doUpdateOrder(needFixSaleInfoDay, orders);

                    if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                        dailyStatProcessor.statSaleInfoByMulchDate(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
                        saleInfoCache.refreshDailySaleInfo(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
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


    private void doUpdateOrder(Set<String> needFixSaleInfoDay, List<AmazonOrderDo> totalOrders) {
        log.info("数据库中需要更新订单的数量：{}", totalOrders.size());

        int dbOrderNum = totalOrders.size();
        int limit = 50;
        int dealTimes = dbOrderNum / 50;
        long startTime = System.currentTimeMillis();

        List<Callable<UpdateRecordFuture>> callables = Lists.newArrayList();
        for (int curNum = 0; curNum <= dealTimes; curNum++) {
            int start = curNum * limit;
            int end = Math.min(dbOrderNum, (curNum + 1) * limit);
            List<AmazonOrderDo> subOrders = totalOrders.subList(start, end);
            callables.add(() -> {
                //获取资源
                log.info("处理更新订单 start【{}】 end【{}】", start, end);
                Set<String> threadFixSaleInfoDay = Sets.newHashSet();
                int updateCount = 0;
                try {
                    List<String> subIds = subOrders.stream().map(AmazonOrderDo::getAmazonOrderId).collect(Collectors.toList());
                    getOrderSemaphore.acquire();
                    GetOrdersResponse orderResponse = spaManager.orderListByOrderIds(subIds);

                    //剔除Pending状态订单
                    List<Order> tmp = orderResponse.getPayload().getOrders().stream()
                            .filter(order -> !Order.OrderStatusEnum.PENDING.equals(order.getOrderStatus()))
                            .collect(Collectors.toList());

                    Map<String, AmazonOrderDo> orderMap = subOrders.stream().collect(Collectors.toMap(AmazonOrderDo::getAmazonOrderId, orderDO -> orderDO));
                    for (Order order : tmp) {
                        String amazonId = order.getAmazonOrderId();

                        //获取资源
                        orderItemSemaphore.acquire();
                        GetOrderItemsResponse tokenResponse = spaManager.orderItems(amazonId, null);
                        //网络问题引起数据无法获取
                        if (tokenResponse == null) {
                            continue;
                        }

                        parseOrderItem(tokenResponse.getPayload().getOrderItems(), amazonId);

                        String nextToken = tokenResponse.getPayload().getNextToken();
                        while (!StringUtils.isEmpty(nextToken)) {

                            //获取资源
                            orderItemSemaphore.acquire();
                            tokenResponse = spaManager.orderItems(amazonId, nextToken);
                            nextToken = tokenResponse.getPayload().getNextToken();
                            parseOrderItem(tokenResponse.getPayload().getOrderItems(), amazonId);
                        }

                        //最后处理，保证订单商品更新完成
                        AmazonOrderDo old = orderMap.get(amazonId);
                        AmazonOrderDo update = new AmazonOrderDo();
                        update.setId(old.getId());
                        update.setOtherConfig(old.getOtherConfig());
                        ConvertUtil.convertToAmazonOrderDo(update, order);
                        amazonOrderDao.updateOrder(update);

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

    private void doSpiderOrder() throws Exception {
        log.info("爬取订单任务开始");
        long startTime = System.currentTimeMillis();
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);

        String strBeginDate = awsSpiderTaskDo.getSpiderDepend();
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

        GetOrdersResponse r = spaManager.orderList(strBeginDate, strEndDate);
        if (r == null) {
            log.info("爬取订单任务异常结束，耗时：{}，爬取时间范围：{}--{}, 爬取总数：{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate, total);
            return;
        }
        List<String> amazonIds = parseOrderResp(awsSpiderTaskDo.getUserMarketId(), r.getPayload().getOrders());
        log.info("时间段【{}】---【{}】爬取订单数量：{}", strBeginDate, strEndDate, amazonIds.size());
        getOrderItems(amazonIds);
        total += amazonIds.size();

        String nextToken = r.getPayload().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            orderSemaphore.acquire();
            r = spaManager.orderListByNextToken(nextToken);
            nextToken = r.getPayload().getNextToken();

            List<String> tmpIds = parseOrderResp(awsSpiderTaskDo.getUserMarketId(), r.getPayload().getOrders());
            log.info("时间段【{}】---【{}】nextToken 爬取订单数量：{}", strBeginDate, strEndDate, amazonIds.size());
            getOrderItems(tmpIds);
            total += tmpIds.size();
        }

        //重新读取数据库
        awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);
        awsSpiderTaskDo.setSpiderDepend(strEndDate);
        awsSpiderTaskDao.update(awsSpiderTaskDo);
        log.info("爬取订单任务结束，耗时：{}，爬取时间范围：{}--{}, 爬取总数：{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate, total);
    }

    /**
     * 解析order为本数据库所用
     *
     * @param orders
     * @return
     */
    private List<String> parseOrderResp(Integer userMarketId, OrderList orders) throws ParseException {
        if (orders ==null) {
            return Lists.newArrayList();
        }

        List<String> amazonIds = Lists.newArrayList();
        for (Order order : orders) {
            amazonIds.add(order.getAmazonOrderId());

            AmazonOrderDo old = amazonOrderDao.getOrderByAmazonId(userMarketId, order.getAmazonOrderId());
            if (old == null) {
                amazonOrderDao.createOrder(ConvertUtil.convertToAmazonOrderDo(new AmazonOrderDo(), order));
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
    private void getOrderItems(List<String> amazonIds) throws Exception {
        for (String amazonId : amazonIds) {
            //获取资源
            orderItemSemaphore.acquire();
            GetOrderItemsResponse itemsResponse = spaManager.orderItems(amazonId, null);
            parseOrderItem(itemsResponse.getPayload().getOrderItems(), amazonId);

            String nextToken = itemsResponse.getPayload().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                //获取资源
                orderItemSemaphore.acquire();
                itemsResponse = spaManager.orderItems(amazonId, nextToken);
                nextToken = itemsResponse.getPayload().getNextToken();
                parseOrderItem(itemsResponse.getPayload().getOrderItems(), amazonId);
            }
        }
    }

    private void parseOrderItem(List<OrderItem> list, String amazonId) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (OrderItem orderItem : list) {
            AmazonOrderItemDo old = amazonOrderItemDao.getOrderItemByOrderItemId(orderItem.getOrderItemId());
            AmazonOrderItemDo update = new AmazonOrderItemDo();
            if (old != null) {
                //已经处理过订单，不做任何操作
                update.setId(old.getId());
                update.setOtherConfig(old.getOtherConfig());
                amazonOrderItemDao.updateOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));
            } else {
                amazonOrderItemDao.createOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));

                //爬取商品信息
                itemService.processSync(orderItem.getSellerSKU(), spaManager.getAwsUserId(), spaManager.getMarketId());

                //刷新库存
                log.info("amazonOrderId【{}】 sku【{}】刷新库存", amazonId, orderItem.getSellerSKU());
                itemService.dealSkuInventory(orderItem.getSellerSKU(), spaManager.getAwsUserId(), spaManager.getMarketId(), "refresh", 0);
            }
        }
    }

    private void orderFinanceSpider() {
        while (true) {
            try {
                List<AmazonOrderDo> orders = amazonOrderDao.getOrdersByOrderStatusAndFinanceStatus(spaManager.getAwsUserMarketId(),
                        ContextConst.AMAZON_STATUS_SHIPPED, 0, 5000);
                if (!CollectionUtils.isEmpty(orders)) {
                    Set<String> needFixSaleInfoDay = Sets.newHashSet();
                    doSpiderOrderFinance(needFixSaleInfoDay, orders);

                    if (!CollectionUtils.isEmpty(needFixSaleInfoDay)) {
                        dailyStatProcessor.statSaleInfoByMulchDate(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
                        saleInfoCache.refreshDailySaleInfo(spaManager.getAwsUserMarketId(), needFixSaleInfoDay);
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

    private void doSpiderOrderFinance(Set<String> needFixSaleInfoDay, List<AmazonOrderDo> totalOrders) {
        log.info("数据库中需要爬取订单财务信息数量：{}", totalOrders.size());

        int dbOrderNum = totalOrders.size();
        int limit = 50;
        int dealTimes = dbOrderNum / 50;
        long startTime = System.currentTimeMillis();

        List<Callable<UpdateRecordFuture>> callables = Lists.newArrayList();
        for (int curNum = 0; curNum <= dealTimes; curNum++) {
            int start = curNum * limit;
            int end = Math.min(dbOrderNum, (curNum + 1) * limit);
            List<AmazonOrderDo> subOrders = totalOrders.subList(start, end);
            callables.add(() -> {
                //获取资源
                log.info("处理更新财务数据 start【{}】 end【{}】", start, end);
                Set<String> threadFixSaleInfoDay = Sets.newHashSet();
                int updateCount = 0;

                for (AmazonOrderDo order : subOrders) {
                    try {
                        orderFinanceSemaphore.acquire();
                        ListFinancialEventsResponse response = spaManager.getFinanceByAwsOrderId(order.getAmazonOrderId());
                        if (response == null) {
                            continue;
                        }

                        if (response.getPayload().getFinancialEvents().getShipmentEventList() == null) {
                            log.info("订单财务信息 amazonOrderId【{}】purchaseTime:【{}】为空", order.getAmazonOrderId(), order.getPurchaseDate());
                            continue;
                        }

                        //设置null
                        response.getPayload().getFinancialEvents().getShipmentEventList()
                                .forEach(se -> se.getShipmentItemList().forEach(si -> si.setPromotionList(null)));

                        Boolean result;
                        AmazonOrderFinanceDo orderFinanceDO = amazonOrderFinanceDao.getOrderFinanceByAmazonId(order.getAmazonOrderId());
                        if (orderFinanceDO == null) {
                            JSONObject jo = (JSONObject) JSONObject.toJSON(response.getPayload().getFinancialEvents());
                            orderFinanceDO = JSONObject.toJavaObject(jo, AmazonOrderFinanceDo.class);
                            jo.remove("shipmentEventList");
                            orderFinanceDO.setOtherEventList(jo.toJSONString());
                            orderFinanceDO.setAmazonOrderId(order.getAmazonOrderId());
                            result = amazonOrderFinanceDao.createOrderFinance(orderFinanceDO);
                        } else {
                            JSONObject jo = (JSONObject) JSONObject.toJSON(response.getPayload().getFinancialEvents());
                            AmazonOrderFinanceDo newOrderFinanceDO = JSONObject.toJavaObject(jo, AmazonOrderFinanceDo.class);
                            jo.remove("shipmentEventList");
                            newOrderFinanceDO.setOtherEventList(jo.toJSONString());
                            newOrderFinanceDO.setAmazonOrderId(order.getAmazonOrderId());
                            newOrderFinanceDO.setId(orderFinanceDO.getId());
                            newOrderFinanceDO.setCtime(orderFinanceDO.getCtime());
                            result = amazonOrderFinanceDao.updateOrderFinance(orderFinanceDO);
                        }

                        if (result) {
                            log.info("更新订单财务信息 amazonOrderId【{}】purchaseTime:【{}】", order.getAmazonOrderId(), order.getPurchaseDate());
                            threadFixSaleInfoDay.add(TimeUtil.getSimpleFormat(order.getPurchaseDate()));
                            updateCount++;

                            //更新订单
                            order.setIsFinance(1);
                            amazonOrderDao.updateOrder(order);
                        }
                    } catch (Exception e) {
                        log.error("更新订单【{}】财务错误：{}", order.getAmazonOrderId(), e.getMessage(), e);
                    }
                }
                return new UpdateRecordFuture(threadFixSaleInfoDay, updateCount);
            });
        }

        try {
            List<Future<UpdateRecordFuture>> returnFutures = updateFinanceThreadExecutor.invokeAll(callables);
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
