package com.cn.hzm.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 9:33 下午
 */
public class TimeUtil {

    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String getUTC(){
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_FORMAT.format(new Date());
    }
}
