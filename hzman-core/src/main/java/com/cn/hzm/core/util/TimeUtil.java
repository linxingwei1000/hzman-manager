package com.cn.hzm.core.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final SimpleDateFormat UTC_SIMPLE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

    private static final SimpleDateFormat UTC_MILLISECOND_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    public static String getUTC() {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_FORMAT.format(new Date());
    }

    public static String getSimpleUTC() {
        UTC_SIMPLE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_SIMPLE_FORMAT.format(new Date());
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


    public static Date transform(String utcDate) throws ParseException {
        if (utcDate.length() == 20) {
            return transformUTCToDate(utcDate);
        } else {
            return transformMilliSecondUTCToDate(utcDate);
        }
    }

    public static Date transformUTCToDate(String utcDate) throws ParseException {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC_FORMAT.parse(utcDate);
    }

    public static Date transformMilliSecondUTCToDate(String utcDate) throws ParseException {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    public static Date getDateByDailyTime(String source) {
        try {
            return DATE_FORMAT.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getSimpleFormat(Date date) {
        return SIMPLE_FORMAT.format(date);
    }

    public static String getDateFormat(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date transformTimeToUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getZeroUTCDateByDay(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    public static Date getUTCDayEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.add(Calendar.SECOND, -1);
        return calendar.getTime();
    }

    /**
     * 当前时间加减天数
     *
     * @param date
     * @param dayNum
     * @param hourNum
     * @param minuteNum
     * @return
     */
    public static Date dateFixByDay(Date date, int dayNum, int hourNum, int minuteNum) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, dayNum);
        calendar.add(Calendar.HOUR, hourNum);
        calendar.add(Calendar.MINUTE, minuteNum);
        return calendar.getTime();
    }

    /**
     * 当前时间加减年
     *
     * @param date
     * @param yearNum
     * @return
     */
    public static Date dateFixByYear(Date date, int yearNum) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, yearNum);
        return calendar.getTime();
    }

    /**
     * 判断是否夏令时间
     */
    public static Boolean isSummer(String dailyDate) {
        ZoneId zoneId = ZoneId.of("America/Los_Angeles");
        ZoneRules rules = zoneId.getRules();
        LocalDateTime ldt = LocalDateTime.parse(dailyDate, DATE_DAILY_FORMAT);
        ZonedDateTime zonedDateTime = ldt.atZone(zoneId);
        return rules.isDaylightSavings(zonedDateTime.toInstant());
    }

    /**
     * 当天美国结束时间对应中国时间
     *
     * @return
     */
    public static Date transformNowToUsDate() {
//        Calendar calendar = new GregorianCalendar();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        Date date = calendar.getTime();
//        String strDailyDate = getDateFormat(date);
//        Boolean isSummer = isSummer(strDailyDate);
//
//        calendar.set(Calendar.HOUR_OF_DAY, isSummer? 15 : 16);
//        Date judge =calendar.getTime();
//        if(new Date().getTime() < judge.getTime()){
//            calendar.set(Calendar.DATE, -1);
//        }
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        return calendar.getTime();

//        Date now = new Date();
//        Date date = getDateBySimple(getSimpleFormat(now));
//        String strDailyDate = getDateFormat(date);
//        Boolean isSummer = isSummer(strDailyDate);
//        Date judge = dateFixByDay(date, 0, isSummer ? 15 : 16, 0);
//        return now.getTime() < judge.getTime() ? dateFixByDay(date, -1, 0, 0) : date;

        Date now = new Date();
        String nowStrDate = getDateFormat(now);
        Date date = getDateBySimple(nowStrDate.substring(0, 10));
        String strDailyDate = nowStrDate.substring(0, 10) + " 00:00:00";
        Boolean isSummer = isSummer(strDailyDate);
        Date judge = dateFixByDay(date, 0, isSummer ? 15 : 16, 0);
        return now.getTime() < judge.getTime() ? dateFixByDay(date, -1, 0, 0) : date;
    }

    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) throws ParseException {
//        String strTime = "2022-09-23T06:30:00Z";
//        System.out.println(transformUTCToDate(strTime));
//        System.out.println(transformNowToUsDate());

//        Date now = new Date();
//        Date date = getDateBySimple(getSimpleFormat(now));
//        String strDailyDate = getDateFormat(date);
//        System.out.println("date :" + date + "str: "+ strDailyDate + "now:" + getDateFormat(now));

        Date now = new Date();
        String nowStrDate = getDateFormat(now);
        Date date = getDateBySimple(nowStrDate.substring(0, 10));
        String strDailyDate = nowStrDate.substring(0, 10) + " 00:00:00";
        System.out.println("date :" + date + "str: "+ strDailyDate + "now:" + getDateFormat(now));

//
//                String strUtc = "2020-11-18T00:00:39.030Z";
//        Date milliSecondDate = transformMilliSecondUTCToDate(strUtc);
//
//        TimeZone pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // just date, you might want something else
//        formatter.setTimeZone(pstTimeZone);
//        String formattedDate = formatter.format(getDateBySimple("2021-03-14"));
//        System.out.println(formattedDate);
//
//        formattedDate = formatter.format(getDateBySimple("2021-03-11"));
//        System.out.println(formattedDate);
//
//        formattedDate = formatter.format(getDateBySimple("2020-08-11"));
//        System.out.println(formattedDate);
//
//        formattedDate = formatter.format(getDateBySimple("2020-02-11"));
//        System.out.println(formattedDate);
//
//
//        ZoneId zoneId = ZoneId.of("America/Los_Angeles");
//        ZoneRules rules = zoneId.getRules();
//
//        Date today = getDateBySimple("2021-03-14");
//        Date yester = dateFixByDay(today, -1, 0, 0);
//        Date tormorrow = dateFixByDay(today, 1, 0, 0);
//
//        String strToday = getDateFormat(today);
//        String strYester = getDateFormat(yester);
//        String strTormorrow = getDateFormat(tormorrow);
//        System.out.println(isSummer(strToday));
//        System.out.println(isSummer(strYester));
//        System.out.println(isSummer(strTormorrow));
    }
}
