package com.cn.hzm.core.aws;

import com.cn.hzm.core.aws.request.BaseRequest;
import com.cn.hzm.core.aws.request.product.GetMatchProductRequest;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.HttpUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.core.util.ToolUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:50 下午
 */
@Component
public class AwsClient {

    public GetMatchingProductForIdResponse getProductInfoByAsin(String asin){
        GetMatchProductRequest getMatchProductRequest = new GetMatchProductRequest();
        getMatchProductRequest.setAction("GetMatchingProductForId");
        getMatchProductRequest.setIdType("ASIN");
        getMatchProductRequest.setIds(Lists.newArrayList("B07BGY7HWK"));
        getMatchProductRequest.setTimestamp(TimeUtil.getUTC());
        return doPost(getMatchProductRequest, GetMatchingProductForIdResponse.class);
    }


    private<T> T doPost(BaseRequest baseRequest, Class<T> tClass){
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
        if(StringUtils.isEmpty(resp)){
            return null;
        }

        return ConvertUtil.toBean(tClass, resp);
    }
}
