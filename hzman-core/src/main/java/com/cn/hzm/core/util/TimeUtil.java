package com.cn.hzm.core.util;

import org.threeten.bp.OffsetDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    private static final String SIMPLE_FORMAT = "yyyy-MM-dd";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final String UTC_SIMPLE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

    private static final String UTC_MILLISECOND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final DateTimeFormatter DATE_DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getUTC() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date());
    }

    public static String getSimpleUTC() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_SIMPLE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date());
    }

    /**
     * Date转UTC时间
     *
     * @param date
     * @return
     */
    public static String dateToUTC(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }


    public static Date transform(String utcDate) throws ParseException {
        if (utcDate.length() == 20) {
            return transformUTCToDate(utcDate);
        } else {
            return transformMilliSecondUTCToDate(utcDate);
        }
    }

    public static Date transformUTCToDate(String utcDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(utcDate);
    }

    public static Date transformMilliSecondUTCToDate(String utcDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_MILLISECOND_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(utcDate);
    }

    public static Date getDateBySimple(String source) {
        try {
            return new SimpleDateFormat(SIMPLE_FORMAT).parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static Date getDateByDateFormat(String source) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getSimpleFormat(Date date) {
        return new SimpleDateFormat(SIMPLE_FORMAT).format(date);
    }

    public static String getDateFormat(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
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
        Date now = new Date();
        String nowStrDate = getDateFormat(now);
        Date date = getDateBySimple(nowStrDate.substring(0, 10));
        String strDailyDate = nowStrDate.substring(0, 10) + " 00:00:00";
        Boolean isSummer = isSummer(strDailyDate);
        Date judge = dateFixByDay(date, 0, isSummer ? 15 : 16, 0);
        return now.getTime() < judge.getTime() ? dateFixByDay(date, -1, 0, 0) : date;
    }

    /**
     * 根据购买时间获取对应日期
     *
     * @return
     */
    public static String transformNowToUsDate(Date purchaseDate) {
        String strPurchaseTime = getDateFormat(purchaseDate);
        Date date = getDateBySimple(strPurchaseTime.substring(0, 10));
        String strDailyDate = strPurchaseTime.substring(0, 10) + " 00:00:00";
        Boolean isSummer = isSummer(strDailyDate);
        Date judge = dateFixByDay(date, 0, isSummer ? 15 : 16, 0);
        purchaseDate = purchaseDate.getTime() < judge.getTime() ? dateFixByDay(date, -1, 0, 0) : date;
        return getSimpleFormat(purchaseDate);
    }

    public static Long daysBetweenTwoDate(Date date1, Date date2){
        String dateStr = getSimpleFormat(date1);
        String nextDateStr = getSimpleFormat(date2);

        LocalDate localDate = LocalDate.parse(dateStr);
        LocalDate nextLocalDate = LocalDate.parse(nextDateStr);

        return ChronoUnit.DAYS.between(localDate, nextLocalDate);
    }

    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    public static String transformOffsetToDate(OffsetDateTime offsetDateTime) {
        org.threeten.bp.format.DateTimeFormatter formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern(UTC_MILLISECOND_FORMAT);
        return offsetDateTime.format(formatter);
    }

    public static void main(String[] args) throws ParseException {
        Date date = new Date();
        System.out.println(daysBetweenTwoDate(date, dateFixByYear(date, 1)));

    }
}
