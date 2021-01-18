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
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmanException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.service.OperateDependService;
import com.cn.hzm.server.util.ConvertUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private DailyStatTask dailyStatTask;

    @Autowired
    private ItemDealService itemDealService;

    private Semaphore orderSemaphore;

    private Semaphore getOrderSemaphore;

    private Semaphore orderItemSemaphore;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;


    /**
     * 单个amazonId爬取任务
     *
     * @param amazonId
     * @throws ParseException
     * @throws InterruptedException
     */
    public void amazonIdSpiderTask(String amazonId) throws ParseException, InterruptedException {
        GetOrderResponse orderResponse = awsClient.getListOrderByAmazonIds(Lists.newArrayList(amazonId));
        List<String> amazonIds = parseOrderResp(orderResponse.getGetOrderResult().getOrders().getList());
        getOrderItems(amazonIds);
    }

    /**
     * 线程任务：无限爬取远端订单
     */
    @PostConstruct
    public void initTask() {

        orderSemaphore = new Semaphore(6);

        //订单商品爬取资源定时充能
        ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderScheduledTask.scheduleAtFixedRate(() -> {
            if (orderSemaphore.availablePermits() < 6) {
                orderSemaphore.release(1);
            }
        }, 60, 60, TimeUnit.SECONDS);

        getOrderSemaphore = new Semaphore(6);

        //订单商品爬取资源定时充能
        ScheduledThreadPoolExecutor getOrderScheduledTask = new ScheduledThreadPoolExecutor(1);
        getOrderScheduledTask.scheduleAtFixedRate(() -> {
            if (orderSemaphore.availablePermits() < 6) {
                orderSemaphore.release(1);
            }
        }, 60, 60, TimeUnit.SECONDS);

        orderItemSemaphore = new Semaphore(30);

        //订单商品爬取资源定时充能
        ScheduledThreadPoolExecutor scheduledTask = new ScheduledThreadPoolExecutor(1);
        scheduledTask.scheduleAtFixedRate(() -> {
            if (orderItemSemaphore.availablePermits() < 30) {
                orderItemSemaphore.release(1);
            }
        }, 60, 2, TimeUnit.SECONDS);


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
            } catch (HzmanException e) {
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
            Set<String> needFixSaleInfoDay = Sets.newHashSet();
            try {
                doUpdateOrder(needFixSaleInfoDay);
            } catch (HzmanException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("订单状态更新任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dailyStatTask.statSaleInfoByMulchDate(needFixSaleInfoDay);
        }
    }


    private void doUpdateOrder(Set<String> needFixSaleInfoDay) throws ParseException, InterruptedException {

        int offset = 0;
        int limit = 50;

        long startTime = System.currentTimeMillis();
        int changeTotal = 0;
        log.info("更新订单任务开始");

        while (true) {
            List<OrderDO> orders = orderService.getOrdersByOrderStatus(ContextConst.AMAZON_STATUS_PENDING, offset, limit);

            List<String> amazonOrderIds = orders.stream().map(OrderDO::getAmazonOrderId).collect(Collectors.toList());

            //获取资源
            getOrderSemaphore.acquire();
            GetOrderResponse orderResponse = awsClient.getOrder(amazonOrderIds);

            //剔除Pending状态订单
            List<Order> tmp = orderResponse.getGetOrderResult().getOrders().getList().stream()
                    .filter(order -> !ContextConst.AMAZON_STATUS_PENDING.equals(order.getOrderStatus()))
                    .collect(Collectors.toList());
            changeTotal += tmp.size();

            Map<String, OrderDO> orderMap = orders.stream().collect(Collectors.toMap(OrderDO::getAmazonOrderId, orderDO -> orderDO));
            for (Order order : tmp) {
                String amazonId = order.getAmazonOrderId();

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

                //最后处理，保证订单商品更新完成
                OrderDO old = orderMap.get(amazonId);
                OrderDO update = new OrderDO();
                update.setId(old.getId());
                update.setOtherConfig(old.getOtherConfig());
                ConvertUtil.convertToOrderDO(update, order);
                orderService.updateOrder(update);

                needFixSaleInfoDay.add(TimeUtil.getSimpleFormat(TimeUtil.transform(order.getPurchaseDate())));
            }

            //退出循环
            if (orders.size() < limit) {
                break;
            }
            offset += limit;
        }

        log.info("更新订单任务结束，本次任务耗时：{}, 共更新订单【{}】条", System.currentTimeMillis() - startTime, changeTotal);
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
        getOrderItems(amazonIds);
        total += amazonIds.size();

        String nextToken = r.getListOrdersResult().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            orderSemaphore.acquire();
            ListOrdersByNextTokenResponse tokenResponse = awsClient.getListOrderByToken(nextToken);
            nextToken = tokenResponse.getListOrdersByNextTokenResult().getNextToken();

            List<String> tmpIds = parseOrderResp(tokenResponse.getListOrdersByNextTokenResult().getOrders().getList());
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
                update.setId(old.getId());
                update.setOtherConfig(old.getOtherConfig());
                orderItemService.updateOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));
            } else {
                orderItemService.createOrderItem(ConvertUtil.convertToOrderItemDO(update, orderItem, amazonId));
            }

            //爬取商品信息
            ItemDO itemDO = itemService.getItemDOByASIN(orderItem.getAsin());
            if (itemDO == null) {
                GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin("ASIN", orderItem.getAsin());
                itemDO = ConvertUtil.convertToItemDO(new ItemDO(), resp, orderItem.getSellerSKU());
                itemService.createItem(itemDO);
            }

            //刷新库存
            itemDealService.dealSkuInventory(orderItem.getSellerSKU(), "refresh", 0);
        }
    }


}
