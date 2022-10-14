package com.cn.hzm.core.aws;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.request.BaseRequest;
import com.cn.hzm.core.aws.request.fulfilment.ShipmentInfoByNextTokenRequest;
import com.cn.hzm.core.aws.request.fulfilment.ShipmentInfoRequest;
import com.cn.hzm.core.aws.request.fulfilment.ShipmentItemsByNextTokenRequest;
import com.cn.hzm.core.aws.request.fulfilment.ShipmentItemsRequest;
import com.cn.hzm.core.aws.request.inventory.ListInventoryRequest;
import com.cn.hzm.core.aws.request.order.*;
import com.cn.hzm.core.aws.request.product.GetMatchProductRequest;
import com.cn.hzm.core.aws.request.product.GetMyPriceForSkuRequest;
import com.cn.hzm.core.aws.request.product.GetProductCategoriesForSKURequest;
import com.cn.hzm.core.aws.resp.fulfilment.*;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.aws.resp.order.*;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.aws.resp.product.GetMyPriceForSkuResponse;
import com.cn.hzm.core.aws.resp.product.GetProductCategoriesForSKUResponse;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.HttpUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.core.util.ToolUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:50 下午
 */
@Component
@Slf4j
public class AwsClient {

    /**
     * 根据关键值获取商品属性
     *
     * @return
     */
    public GetMatchingProductForIdResponse getProductInfoByAsin(String key, String value) {
        GetMatchProductRequest getMatchProductRequest = new GetMatchProductRequest();
        getMatchProductRequest.setAction("GetMatchingProductForId");
        getMatchProductRequest.setIdType(key);
        getMatchProductRequest.setIds(Lists.newArrayList(value));
        getMatchProductRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getMatchProductRequest, GetMatchingProductForIdResponse.class);
    }

    /**
     * 根据sku获取商品价格
     *
     * @return
     */
    public GetMyPriceForSkuResponse getMyPriceForSku(String sku) {
        GetMyPriceForSkuRequest getMyPriceForSkuRequest = new GetMyPriceForSkuRequest();
        getMyPriceForSkuRequest.setAction("GetMyPriceForSKU");
        getMyPriceForSkuRequest.setSkus(Lists.newArrayList(sku));
        getMyPriceForSkuRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getMyPriceForSkuRequest, GetMyPriceForSkuResponse.class);
    }

    /**
     * 根据sku获取类目
     *
     * @return
     */
    public GetProductCategoriesForSKUResponse getProductCategoriesForSku(String sku) {
        GetProductCategoriesForSKURequest getProductCategoriesForSKURequest = new GetProductCategoriesForSKURequest();
        getProductCategoriesForSKURequest.setAction("GetProductCategoriesForSKU");
        getProductCategoriesForSKURequest.setSellerSKU(sku);
        getProductCategoriesForSKURequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getProductCategoriesForSKURequest, GetProductCategoriesForSKUResponse.class);
    }

    public ListInventorySupplyResponse getInventoryInfoBySku(String sku) {
        ListInventoryRequest listInventoryRequest = new ListInventoryRequest();
        listInventoryRequest.setApiSection("FulfillmentInventory");
        listInventoryRequest.setAction("ListInventorySupply");
        listInventoryRequest.setResponseGroup("Detailed");
        listInventoryRequest.setSkus(Lists.newArrayList(sku));
        listInventoryRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(listInventoryRequest, ListInventorySupplyResponse.class);
    }

    /**
     * 根据amazonOrderIds批量获取订单
     *
     * @param amazonOrderIds
     * @return
     */
    public GetOrderResponse getOrder(List<String> amazonOrderIds) {
        GetOrderRequest getOrderRequest = new GetOrderRequest();
        getOrderRequest.setAction("GetOrder");
        getOrderRequest.setAmazonOrderId(amazonOrderIds);
        getOrderRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getOrderRequest, GetOrderResponse.class);
    }

    /**
     * 根据日期批量获取订单
     *
     * @param beginDate
     * @param endDate
     * @param isCreate
     * @return
     */
    public ListOrdersResponse getListOrder(String beginDate, String endDate, boolean isCreate) {
        ListOrderRequest orderRequest = new ListOrderRequest();
        orderRequest.setAction("ListOrders");
        orderRequest.setTimestamp(TimeUtil.getUTC());
        orderRequest.setMarketplaceIds(Lists.newArrayList("ATVPDKIKX0DER"));
        if (isCreate) {
            orderRequest.setCreatedAfter(beginDate);
            if (!StringUtils.isEmpty(endDate)) {
                orderRequest.setCreatedBefore(endDate);
            }
        } else {
            orderRequest.setLastUpdatedAfter(beginDate);
            if (!StringUtils.isEmpty(endDate)) {
                orderRequest.setLastUpdatedBefore(endDate);
            }
        }
        return doPost(orderRequest, ListOrdersResponse.class);
    }

    /**
     * 根据token获取订单
     *
     * @param nextToken
     * @return
     */
    public ListOrdersByNextTokenResponse getListOrderByToken(String nextToken) {
        ListOrderByTokenRequest tokenRequest = new ListOrderByTokenRequest();
        tokenRequest.setAction("ListOrdersByNextToken");
        tokenRequest.setNextToken(nextToken);
        tokenRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(tokenRequest, ListOrdersByNextTokenResponse.class);
    }

    /**
     * 根据订单号获取订单
     *
     * @param amazonIds
     * @return
     */
    public GetOrderResponse getListOrderByAmazonIds(List<String> amazonIds) {
        GetOrderRequest getOrderRequest = new GetOrderRequest();
        getOrderRequest.setAction("GetOrder");
        getOrderRequest.setAmazonOrderId(amazonIds);
        getOrderRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getOrderRequest, GetOrderResponse.class);
    }

    /**
     * 根据订单号获取商品订单信息
     *
     * @param amazonId
     * @return
     */
    public ListOrderItemsResponse getListOrderItemsByAmazonId(String amazonId) {
        ListOrderItemsRequest getOrderRequest = new ListOrderItemsRequest();
        getOrderRequest.setAction("ListOrderItems");
        getOrderRequest.setAmazonOrderId(amazonId);
        getOrderRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getOrderRequest, ListOrderItemsResponse.class);
    }

    /**
     * 根据token获取商品订单信息
     *
     * @param nextToken
     * @return
     */
    public ListOrderItemsByNextTokenResponse getListOrderItemByAmazonIds(String nextToken) {
        ListOrderItemsByTokenRequest tokenRequest = new ListOrderItemsByTokenRequest();
        tokenRequest.setAction("ListOrderItemsByNextToken");
        tokenRequest.setNextToken(nextToken);
        tokenRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(tokenRequest, ListOrderItemsByNextTokenResponse.class);
    }

    /**
     * 获取货单信息
     *
     * @param shipmentIds
     * @param beginDate
     * @param endDate
     * @return
     */
    public ListInboundShipmentsResponse getShipmentInfo(List<String> shipmentStatus, List<String> shipmentIds, String beginDate, String endDate) {
        ShipmentInfoRequest shipmentInfoRequest = new ShipmentInfoRequest();
        shipmentInfoRequest.setAction("ListInboundShipments");
        shipmentInfoRequest.setTimestamp(TimeUtil.getUTC());

        if(!CollectionUtils.isEmpty(shipmentStatus)) {
            shipmentInfoRequest.setShipmentStatusList(shipmentStatus);
        }

        if (!CollectionUtils.isEmpty(shipmentIds)) {
            shipmentInfoRequest.setShipmentIds(shipmentIds);
        }

        if(!StringUtils.isEmpty(beginDate)) {
            shipmentInfoRequest.setLastUpdatedAfter(beginDate);
        }

        if(!StringUtils.isEmpty(endDate)){
            shipmentInfoRequest.setLastUpdatedBefore(endDate);
        }
        return doPost(shipmentInfoRequest, ListInboundShipmentsResponse.class);
    }

    /**
     * 获取货单信息nextToken
     *
     * @param nextToken
     * @return
     */
    public ListInboundShipmentsByNextTokenResponse getShipmentInfoNextToken(String nextToken) {
        ShipmentInfoByNextTokenRequest shipmentItemsRequest = new ShipmentInfoByNextTokenRequest();
        shipmentItemsRequest.setAction("ListInboundShipmentsByNextToken");
        shipmentItemsRequest.setTimestamp(TimeUtil.getUTC());
        shipmentItemsRequest.setNextToken(nextToken);
        return doPost(shipmentItemsRequest, ListInboundShipmentsByNextTokenResponse.class);
    }

    /**
     * 获取商品入库信息
     *
     * @param shipmentId
     * @param beginDate
     * @param endDate
     * @return
     */
    public ListInboundShipmentItemsResponse getShipmentItems(String shipmentId, String beginDate, String endDate) {
        ShipmentItemsRequest shipmentItemsRequest = new ShipmentItemsRequest();
        shipmentItemsRequest.setAction("ListInboundShipmentItems");
        shipmentItemsRequest.setTimestamp(TimeUtil.getUTC());

        if (StringUtils.isEmpty(shipmentId)) {
            shipmentItemsRequest.setLastUpdatedAfter(beginDate);
            shipmentItemsRequest.setLastUpdatedBefore(endDate);
        } else {
            shipmentItemsRequest.setShipmentId(shipmentId);
        }
        return doPost(shipmentItemsRequest, ListInboundShipmentItemsResponse.class);
    }

    /**
     * 获取商品入库信息
     *
     * @param nextToken
     * @return
     */
    public ListInboundShipmentItemsByNextTokenResponse getShipmentItemsByNextToken(String nextToken) {
        ShipmentItemsByNextTokenRequest shipmentItemsRequest = new ShipmentItemsByNextTokenRequest();
        shipmentItemsRequest.setAction("ListInboundShipmentItemsByNextToken");
        shipmentItemsRequest.setTimestamp(TimeUtil.getUTC());
        shipmentItemsRequest.setNextToken(nextToken);
        return doPost(shipmentItemsRequest, ListInboundShipmentItemsByNextTokenResponse.class);
    }


    private <T> T doPost(BaseRequest baseRequest, Class<T> tClass) {
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
        //log.info("aws resp:{}", resp);
        if (StringUtils.isEmpty(resp)) {
            return null;
        }

        //不同错误不同处理
        if (resp.contains("ErrorResponse")) {
            //触发amazon限流
            if (resp.contains("RequestThrottled")) {
                throw new HzmException(ExceptionCode.REQUEST_LIMIT);
            }

            //amazon入库货物单号不存在
            if(resp.contains("Shipment not found")){
                throw new HzmException(ExceptionCode.SHIPMENT_ID_NOT_EXIST);
            }
        }
        T t = null;
        try {
            t = ConvertUtil.toBean(tClass, resp);
        } catch (Exception e) {
            log.error("parse resp:{} error:{}", resp, e.toString());
            e.printStackTrace();
        }
        return t;
    }

    public static void main(String[] args) {
        AwsClient cliet = new AwsClient();
        GetMatchingProductForIdResponse response = cliet.getProductInfoByAsin("SellerSKU", "PHW671104");
        //GetProductCategoriesForSKUResponse response = cliet.getProductCategoriesForSku("XL7907B7-20");
        //ItemDO itemDO = ConvertUtil.convertToItemDO(new ItemDO(), response, "XL22-0521-02");
        System.out.println(JSONObject.toJSONString(response));
    }
}
