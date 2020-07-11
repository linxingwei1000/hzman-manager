package com.cn.hzm.core.util;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.constant.ContextConst;
import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 11:00 上午
 */
public class ToolUtil {

    public static String createStrForSign(JSONObject jo) {
        StringBuilder sb = new StringBuilder();
        sb.append("POST\n");
        sb.append(ContextConst.SIGN_URL).append("\n");
        sb.append("/").append(jo.get("apiSection")).append("/").append(jo.get("Version")).append("\n");
        return createParamStr(jo, sb);
    }

    public static String createRequestUrl(JSONObject jo) {
        StringBuilder sb = new StringBuilder();
        sb.append(ContextConst.AWS_URL).append("/").append(jo.get("apiSection")).append("/").append(jo.get("Version")).append("?");
        return createParamStr(jo, sb);
    }

    private static String createParamStr(JSONObject jo, StringBuilder sb){
        jo.remove("apiSection");
        Map<String, String> sorted = Maps.newTreeMap();
        jo.forEach((key1, value1) -> sorted.put(key1, String.valueOf(value1)));
        sorted.entrySet().stream().filter(entry -> !"null".equals(entry.getValue()))
                .forEach(entry -> sb.append(entry.getKey()).append("=").append(urlEncode(entry.getValue())).append("&"));
        return sb.substring(0, sb.length() - 1);
    }

    private static String urlEncode(String rawValue) {
        String value = (rawValue == null) ? "" : rawValue;
        String encoded = null;

        try {
            encoded = URLEncoder.encode(value, ContextConst.CHARACTER_ENCODING)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unknown encoding: " + ContextConst.CHARACTER_ENCODING);
            e.printStackTrace();
        }

        return encoded;
    }
}
