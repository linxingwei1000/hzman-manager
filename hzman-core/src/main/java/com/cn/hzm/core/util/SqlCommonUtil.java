package com.cn.hzm.core.util;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:41 下午
 */
public class SqlCommonUtil {

    public static String limitOffsetSql(Integer offset, Integer limit){
        StringBuilder sb = new StringBuilder();
        sb.append("limit ").append(offset).append(",").append(limit);
        return sb.toString();
    }
}
