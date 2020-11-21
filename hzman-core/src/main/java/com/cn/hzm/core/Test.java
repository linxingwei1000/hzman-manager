package com.cn.hzm.core;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.Encrypt;
import com.cn.hzm.core.aws.domain.order.Order;
import com.cn.hzm.core.aws.domain.order.Orders;
import com.cn.hzm.core.aws.request.BaseRequest;
import com.cn.hzm.core.aws.request.inventory.ListInventoryRequest;
import com.cn.hzm.core.aws.request.order.*;
import com.cn.hzm.core.aws.request.product.GetMatchProductRequest;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.aws.resp.order.*;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.aws.resp.product.ListMatchingProductsResponse;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.HttpUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.core.util.ToolUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 12:19 下午
 */
public class Test {

    public static void main(String[] args) throws Exception {

//        GetMatchProductRequest getMatchProductRequest = new GetMatchProductRequest();
//        getMatchProductRequest.setAction("GetMatchingProductForId");
//        getMatchProductRequest.setIdType("ASIN");
//        getMatchProductRequest.setIds(Lists.newArrayList("B07FK5GRVP"));
//        getMatchProductRequest.setTimestamp(TimeUtil.getUTC());
//        GetMatchingProductForIdResponse abc = doPost(getMatchProductRequest, GetMatchingProductForIdResponse.class);
//
        Map<Integer, String> map = Maps.newHashMap();

        int i = 0;
//        ListInventoryRequest listInventoryRequest = new ListInventoryRequest();
//        listInventoryRequest.setApiSection("FulfillmentInventory");
//        listInventoryRequest.setAction("ListInventorySupply");
//        listInventoryRequest.setResponseGroup("Basic");
//        listInventoryRequest.setSkus(Lists.newArrayList("XL7907-20T"));
//        listInventoryRequest.setTimestamp(TimeUtil.getUTC());
//        ListInventorySupplyResponse r = doPost(listInventoryRequest, ListInventorySupplyResponse.class);
//        System.out.println(JSONObject.toJSONString(r));

        ListOrderRequest orderRequest = new ListOrderRequest();
        orderRequest.setAction("ListOrders");
        orderRequest.setTimestamp(TimeUtil.getUTC());
        orderRequest.setMarketplaceIds(Lists.newArrayList("ATVPDKIKX0DER"));
        orderRequest.setCreatedAfter("2020-11-18T00:00:00Z");
        orderRequest.setCreatedBefore("2020-11-18T23:59:59Z");
        ListOrdersResponse r = doPost(orderRequest, ListOrdersResponse.class);

        String nextToken = r.getListOrdersResult().getNextToken();
        map.put(i, nextToken);

        List<String> amazonIds = r.getListOrdersResult().getOrders().getList().stream().map(Order::getAmazonOrderId).collect(Collectors.toList());
        //getOrders(amazonIds);
        getOrderItems(amazonIds);


        int requestTimes = 1;
        while (!StringUtils.isEmpty(nextToken)) {
            ListOrderByTokenRequest tokenRequest = new ListOrderByTokenRequest();
            tokenRequest.setAction("ListOrdersByNextToken");
            tokenRequest.setNextToken(nextToken);
            tokenRequest.setTimestamp(TimeUtil.getUTC());
            ListOrdersByNextTokenResponse tokenResponse = doPost(tokenRequest, ListOrdersByNextTokenResponse.class);

            nextToken = tokenResponse.getListOrdersByNextTokenResult().getNextToken();
            map.put(++i, nextToken);
            requestTimes++;

            if (requestTimes >= 4) {
                Thread.sleep(60000);
                requestTimes = 0;
            }

        }

        System.out.println(map);
    }

    private static void getOrders(List<String> amazonIds) {
        int begin = 0;
        int end = 50;
        while (true) {
            int next = Math.min(amazonIds.size(), end);

            List<String> tmp = amazonIds.subList(begin, next);

            GetOrderRequest getOrderRequest = new GetOrderRequest();
            getOrderRequest.setAction("GetOrder");
            getOrderRequest.setAmazonOrderId(tmp);
            getOrderRequest.setTimestamp(TimeUtil.getUTC());
            GetOrderResponse tokenResponse = doPost(getOrderRequest, GetOrderResponse.class);
            System.out.println(tokenResponse.getGetOrderResult().getOrders().getList().size());

            if (next != end) {
                break;
            }

            begin += 50;
            end += 50;
        }
    }

    private static void getOrderItems(List<String> amazonIds) throws InterruptedException {
        for (String amazonId : amazonIds) {
            ListOrderItemsRequest getOrderRequest = new ListOrderItemsRequest();
            getOrderRequest.setAction("ListOrderItems");
            getOrderRequest.setAmazonOrderId(amazonId);
            getOrderRequest.setTimestamp(TimeUtil.getUTC());
            ListOrderItemsResponse tokenResponse = doPost(getOrderRequest, ListOrderItemsResponse.class);
            System.out.println(JSONObject.toJSONString(tokenResponse));

            String nextToken = tokenResponse.getListOrderItemsResult().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                ListOrderItemsByTokenRequest tokenRequest = new ListOrderItemsByTokenRequest();
                tokenRequest.setAction("ListOrderItemsByNextToken");
                tokenRequest.setNextToken(nextToken);
                tokenRequest.setTimestamp(TimeUtil.getUTC());
                try {
                    ListOrderItemsByNextTokenResponse response = doPost(getOrderRequest, ListOrderItemsByNextTokenResponse.class);
                    System.out.println(JSONObject.toJSONString(response));
                    nextToken = response.getListOrderItemsByNextTokenResult().getNextToken();
                } catch (Exception e) {
                    System.out.println("~~~~~~amazonId:" + amazonId + " nextToken:" + nextToken);
                }
            }

            Thread.sleep(2000);
        }

    }

    private static <T> T doPost(BaseRequest baseRequest, Class<T> tClass) {
        String strForSign = ToolUtil.createStrForSign(baseRequest.installJsonStr());

        String sign = Encrypt.sign(strForSign);
        if (StringUtils.isEmpty(sign)) {
            return null;
        }

        baseRequest.setSignature(sign);
        String url = ToolUtil.createRequestUrl(baseRequest.installJsonStr());

        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "text/xml");
        String resp = HttpUtil.postV2(headers, url);
        System.out.println(resp);
        if (StringUtils.isEmpty(resp)) {
            return null;
        }

        return ConvertUtil.toBean(tClass, resp);
    }
}
