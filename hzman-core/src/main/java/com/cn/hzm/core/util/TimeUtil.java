package com.cn.hzm.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 9:33 下午
 */
public class TimeUtil {

    private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final SimpleDateFormat UTC_MILLISECOND_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    public static String getUTC() {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_FORMAT.format(new Date());
    }

    /**
     * Date转UTC时间
     *
     * @param date
     * @return
     */
    public static String dateToUTC(Date date) {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_FORMAT.format(date);
    }

    public static Date transformUTCToDate(String utcDate) throws ParseException {
        return UTC_FORMAT.parse(utcDate);
    }

    public static Date transformMilliSecondUTCToDate(String utcDate) throws ParseException {
        return UTC_MILLISECOND_FORMAT.parse(utcDate);
    }

    public static Date getDateBySimple(String source) {
        try {
            return SIMPLE_FORMAT.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getSimpleFormat(Date date) {
        return SIMPLE_FORMAT.format(date);
    }


    /**
     * 当前时间加减天数
     *
     * @param date
     * @param num
     * @return
     */
    public static Date dateFixByDay(Date date, int num) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, num);
        return calendar.getTime();
    }

    public static void main(String[] args) throws ParseException {

        String strUtc = "2020-11-18T00:00:39.030Z";
        Date milliSecondDate = transformMilliSecondUTCToDate(strUtc);


        System.out.println(getUTC());
        String source = "2020-11-11";
        Date beginDate = getDateBySimple(source);
        Date endDate = dateFixByDay(beginDate, -1);

        System.out.println(String.format("begin:%s end:%s  endDate:%s", dateToUTC(beginDate), dateToUTC(endDate), getSimpleFormat(endDate)));


    }
}
