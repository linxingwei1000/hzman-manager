package com.cn.hzm.core.util;

import com.cn.hzm.core.constant.ContextConst;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/6/28 10:56 下午
 */
public class HttpUtil {

    /**
     * @param headers 请求头
     * @param url     请求url
     * @return
     */
    public static String postV2(Map<String, String> headers, String url) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建httpPost
        HttpPost httpPost = new HttpPost(url);

        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                httpPost.setHeader(key, headers.get(key));
            }
        }
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                @SuppressWarnings("deprecation")
                String respCharset = EntityUtils.getContentCharSet(entity) == null ? ContextConst.CHARACTER_ENCODING
                        : EntityUtils.getContentCharSet(entity);
                return new String(
                        EntityUtils.toByteArray(entity),
                        respCharset);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集.
     *
     * @param paramsMap 参数集, 键/值对
     * @return NameValuePair参数集
     */
    private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return null;
        }
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> map : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
        }
        return params;
    }
}
