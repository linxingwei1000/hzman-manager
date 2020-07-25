package com.cn.hzm.core;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.Encrypt;
import com.cn.hzm.core.aws.request.BaseRequest;
import com.cn.hzm.core.aws.request.inventory.ListInventoryRequest;
import com.cn.hzm.core.aws.request.order.ListOrderRequest;
import com.cn.hzm.core.aws.request.product.GetMatchProductRequest;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.aws.resp.product.ListMatchingProductsResponse;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.HttpUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.core.util.ToolUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 12:19 下午
 */
public class Test {

    public static void main(String[] args) throws Exception {
//        ListMatchingProductRequest productRequest = new ListMatchingProductRequest();
//        productRequest.setAction("ListMatchingProducts");
//        productRequest.setQuery("hzman");
//        productRequest.setTimestamp(TimeUtil.getUTC());
//        doPost(productRequest, ListMatchingProductsResponse.class);

//        GetMatchProductRequest getMatchProductRequest = new GetMatchProductRequest();
//        getMatchProductRequest.setAction("GetMatchingProductForId");
//        getMatchProductRequest.setIdType("ASIN");
//        getMatchProductRequest.setIds(Lists.newArrayList("B07BGY7HWK"));
//        getMatchProductRequest.setTimestamp(TimeUtil.getUTC());
//        GetMatchingProductForIdResponse resp = doPost(getMatchProductRequest, GetMatchingProductForIdResponse.class);
//        System.out.println(JSONObject.toJSONString(resp));

        ListInventoryRequest listInventoryRequest = new ListInventoryRequest();
        listInventoryRequest.setApiSection("FulfillmentInventory");
        listInventoryRequest.setAction("ListInventorySupply");
        listInventoryRequest.setResponseGroup("Basic");
        listInventoryRequest.setSkus(Lists.newArrayList("XL7907-20T"));
        listInventoryRequest.setTimestamp(TimeUtil.getUTC());
        ListInventorySupplyResponse r = doPost(listInventoryRequest, ListInventorySupplyResponse.class);
        System.out.println(JSONObject.toJSONString(r));

//        ListOrderRequest orderRequest = new ListOrderRequest();
//        orderRequest.setAction("ListOrders");
//        orderRequest.setTimestamp(TimeUtil.getUTC());
//        orderRequest.setMarketplaceIds(Lists.newArrayList("ATVPDKIKX0DER"));
//        orderRequest.setCreatedAfter("2020-07-09T16:00:00Z");
//        orderRequest.setCreatedBefore("2020-07-10T16:00:00Z");
//        doPost(orderRequest, ListMatchingProductsResponse.class);

    }

    private static<T> T doPost(BaseRequest baseRequest, Class<T> tClass){
        String strForSign = ToolUtil.createStrForSign(baseRequest.installJsonStr());

        String sign = Encrypt.sign(strForSign);
        if(StringUtils.isEmpty(sign)){
            return null;
        }

        baseRequest.setSignature(sign);
        String url = ToolUtil.createRequestUrl(baseRequest.installJsonStr());

        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "text/xml");
        String resp = HttpUtil.postV2(headers, url);
        System.out.println(resp);
        if(StringUtils.isEmpty(resp)){
            return null;
        }

        return ConvertUtil.toBean(tClass, resp);
    }
}
