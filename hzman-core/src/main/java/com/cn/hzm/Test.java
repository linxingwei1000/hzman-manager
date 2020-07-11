package com.cn.hzm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.aws.Encrypt;
import com.cn.hzm.aws.request.BaseRequest;
import com.cn.hzm.aws.request.OrderRequest;
import com.cn.hzm.util.ConvertUtil;
import com.cn.hzm.util.HttpUtil;
import com.cn.hzm.util.TimeUtil;
import com.cn.hzm.aws.request.ProductRequest;
import com.cn.hzm.aws.resp.ListMatchingProductsResponse;
import com.cn.hzm.util.ToolUtil;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 12:19 下午
 */
public class Test {

    public static void main(String[] args) throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setAction("ListMatchingProducts");
        productRequest.setQuery("hzman");
        productRequest.setTimestamp(TimeUtil.getUTC());
        doPost(productRequest, ListMatchingProductsResponse.class);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setAction("ListOrders");
        orderRequest.setTimestamp(TimeUtil.getUTC());
        orderRequest.setMarketplaceIdOne("ATVPDKIKX0DER");
        orderRequest.setCreatedAfter("2020-07-09T16:00:00Z");
        orderRequest.setCreatedBefore("2020-07-10T16:00:00Z");
        doPost(orderRequest, ListMatchingProductsResponse.class);

    }

    private static<T> void doPost(BaseRequest baseRequest, Class<T> tClass) throws Exception {
        String strForSign = ToolUtil.createStrForSign((JSONObject) JSON.toJSON(baseRequest));
        baseRequest.setSignature(Encrypt.sign(strForSign));
        String url = ToolUtil.createRequestUrl((JSONObject) JSON.toJSON(baseRequest));

        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "text/xml");
        String resp = HttpUtil.postV2(headers, url);

        System.out.println(resp);
        //T t = ConvertUtil.toBean(tClass, resp);
        //return t;
    }
}
