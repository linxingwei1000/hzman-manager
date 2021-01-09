package com.cn.hzm.server.task;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.order.Order;
import com.cn.hzm.core.aws.domain.order.OrderItem;
import com.cn.hzm.core.aws.resp.order.*;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmanException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.service.OperateDependService;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    private static final Integer UTC_BETWEEN_DATE_SECOND = 8 * 60 * 60 * 1000;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;

    /**
     * 线程任务：无限爬取远端订单
     *
     * @throws Exception
     */
    @PostConstruct
    public void initTask() throws Exception {

        ExecutorService createOrderTask = Executors.newSingleThreadExecutor();
        createOrderTask.execute(this::createOrderSpider);

//        ExecutorService updateOrderTask = Executors.newSingleThreadExecutor();
//        updateOrderTask.execute(this::updateOrderSpider);
    }

    /**
     * 单个amazonId爬取任务
     *
     * @param amazonId
     * @throws ParseException
     * @throws InterruptedException
     */
    public void amazonIdSpiderTask(String amazonId) throws ParseException, InterruptedException {
        GetOrderResponse orderResponse = awsClient.getListOrderByAmazonIds(Lists.newArrayList(amazonId));
        List<String> amazonIds = parseOrderResp(orderResponse.getGetOrderResult().getOrders().getList(), true);
        getOrderItems(amazonIds);
    }

    private void createOrderSpider() {
        while (true) {
            try {
                doSpiderOrder(ContextConst.OPERATE_SPIDER_CREATE_ORDER, true);
            } catch (HzmanException e) {
                if(e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)){
                    try {
                        log.info("爬虫任务触发限流 暂停5分钟");
                        Thread.sleep(5 * 60 * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void updateOrderSpider() {
        while (true) {
            try {
                doSpiderOrder(ContextConst.OPERATE_SPIDER_UPDATE_ORDER, false);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void doSpiderOrder(String sign, boolean isCreate) throws ParseException, InterruptedException {
        String strBeginDate = operateDependService.getValueByKey(sign);

        Date beginDate = TimeUtil.transformUTCToDate(strBeginDate);
        Date endDate = TimeUtil.dateFixByDay(beginDate, 0, 0, 30);
        String strEndDate = TimeUtil.dateToUTC(endDate);

        //如果是同一天的请求，endDate为空
        boolean needUpdateDate = true;
        if (System.currentTimeMillis() - beginDate.getTime() - UTC_BETWEEN_DATE_SECOND < DURATION_SECOND) {
            strEndDate = null;
            needUpdateDate = false;
        }

        ListOrdersResponse r = awsClient.getListOrder(strBeginDate, strEndDate, isCreate);

        ListOrdersResult listOrdersResult = r.getListOrdersResult();
        if (listOrdersResult == null) {
            return;
        }

        List<String> amazonIds = parseOrderResp(listOrdersResult.getOrders().getList(), isCreate);
        getOrderItems(amazonIds);

        String nextToken = listOrdersResult.getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            ListOrdersByNextTokenResponse tokenResponse = awsClient.getListOrderByToken(nextToken);
            nextToken = tokenResponse.getListOrdersByNextTokenResult().getNextToken();

            List<String> tmpIds = parseOrderResp(tokenResponse.getListOrdersByNextTokenResult().getOrders().getList(), isCreate);
            getOrderItems(tmpIds);
        }

        if (needUpdateDate) {
            operateDependService.updateValueByKey(sign, strEndDate);
            Thread.sleep(1000);
            return;
        }

        //如果拉取当天数据，每半小时执行一次
        Thread.sleep(30 * 60 * 1000);
    }

    /**
     * 解析order为本数据库所用
     *
     * @param list
     * @param isCreate
     * @return
     * @throws InterruptedException
     */
    private List<String> parseOrderResp(List<Order> list, boolean isCreate) throws InterruptedException, ParseException {
        if (CollectionUtils.isEmpty(list)) {
            Thread.sleep(60 * 1000);
            return Lists.newArrayList();
        }

        List<String> amazonIds = Lists.newArrayList();
        for (Order order : list) {
            amazonIds.add(order.getAmazonOrderId());

            OrderDO old = orderService.getOrderByAmazonId(order.getAmazonOrderId());
            if (isCreate) {
                if (old == null) {
                    orderService.createOrder(ConvertUtil.convertToOrderDO(new OrderDO(), order));
                }
            } else {
                if (old != null) {
                    OrderDO update = new OrderDO();
                    update.setId(old.getId());
                    update.setOtherConfig(old.getOtherConfig());
                    orderService.updateOrder(ConvertUtil.convertToOrderDO(update, order));
                }
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
            ListOrderItemsResponse tokenResponse = awsClient.getListOrderItemsByAmazonId(amazonId);
            System.out.println(JSONObject.toJSONString(tokenResponse));

            parseOrderItem(tokenResponse.getListOrderItemsResult().getOrderItems().getList(), amazonId);

            String nextToken = tokenResponse.getListOrderItemsResult().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                ListOrderItemsByNextTokenResponse response = awsClient.getListOrderItemByAmazonIds(nextToken);

                System.out.println(JSONObject.toJSONString(response));
                nextToken = response.getListOrderItemsByNextTokenResult().getNextToken();
                parseOrderItem(response.getListOrderItemsByNextTokenResult().getOrderItems().getList(), amazonId);
            }
            Thread.sleep(2000);
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
            InventoryDO inventoryDO = inventoryService.getInventoryBySku(orderItem.getSellerSKU());
            if (inventoryDO != null) {
                ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(orderItem.getSellerSKU()), inventoryDO);
                inventoryService.updateInventory(inventoryDO);
            } else {
                inventoryDO = new InventoryDO();
                ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(orderItem.getSellerSKU()), inventoryDO);
                inventoryService.createInventory(inventoryDO);
            }
        }
    }


}
