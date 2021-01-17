package com.cn.hzm.core.constant;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:54 上午
 */
public class ContextConst {

    public static final String SIGN_URL = "mws.amazonservices.com";

    public static final String AWS_URL = "https://mws.amazonservices.com";

    public static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * 爬取新建订单
     */
    public static final String OPERATE_SPIDER_CREATE_ORDER = "spider_create_order";


    /**
     * 爬取货物入库
     */
    public static final String OPERATE_SHIPMENT_INFO = "spider_shipment_info";

    /**
     * 今日销量降序
     */
    public static final int ITEM_SORT_TODAY_DESC = 0;

    /**
     * 今日销量升序
     */
    public static final int ITEM_SORT_TODAY_ASC = 1;

    /**
     * 昨日销量降序
     */
    public static final int ITEM_SORT_YESTERDAY_DESC = 2;

    /**
     * 昨日销量升序
     */
    public static final int ITEM_SORT_YESTERDAY_ASC = 3;

    /**
     * 上周同日销量降序
     */
    public static final int ITEM_SORT_LAST_WEEK_DESC = 4;

    /**
     * 上周同日销量升序
     */
    public static final int ITEM_SORT_LAST_WEEK_ASC = 5;
}
