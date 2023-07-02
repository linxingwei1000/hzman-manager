package com.cn.hzm.core.constant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:54 上午
 */
public class ContextConst {

    public static final String TRACE_ID = "traceId";

    public static final String EXCEPTION_LOG_KEY = "_exception";

    public static final String AUTH_HEADER_ACCOUNT_ID = "accountId";

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
     * 可售库存降序
     */
    public static final int ITEM_SORT_SALE_INVENTORY_DESC = 4;

    /**
     * 可售库存升序
     */
    public static final int ITEM_SORT_SALE_INVENTORY_ASC = 5;

    /**
     * 本地库存降序
     */
    public static final int ITEM_SORT_LOCAL_INVENTORY_DESC = 6;

    /**
     * 本地库存升序
     */
    public static final int ITEM_SORT_LOCAL_INVENTORY_ASC = 7;

    /**
     * 30天销量降序
     */
    public static final int ITEM_SORT_30_DAY_DESC = 8;

    /**
     * 30天销量升序
     */
    public static final int ITEM_SORT_30_DAY_ASC = 9;

    /**
     * amazon订单状态：Pending
     */
    public static final String AMAZON_STATUS_PENDING = "Pending";

    /**
     * amazon订单状态：Canceled
     */
    public static final String AMAZON_STATUS_CANCELED = "Canceled";

    /**
     * amazon订单状态：Shipped
     */
    public static final String AMAZON_STATUS_SHIPPED = "Shipped";

    /**
     * amazon订单状态：localDelete，本地删除状态，防止僵尸订单影响正常订单更新流程
     */
    public static final String AMAZON_STATUS_DELETE = "localDelete";

    /**
     * 收货地址列表
     */
    public static final List<String> RECEIVE_ADDRESS = Lists.newArrayList("杭州市滨江区江虹南路316号京安创业园3幢3楼305，李裕平收，15381111258");


    public static final Map<String, String> REGION_MAP = Maps.newHashMap();
    static{
        REGION_MAP.put("us-east-1", "北美");
        REGION_MAP.put("eu-west-1", "欧洲");
        REGION_MAP.put("us-west-2", "亚洲");
    }

}
