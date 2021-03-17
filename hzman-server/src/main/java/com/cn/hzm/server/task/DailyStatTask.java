package com.cn.hzm.server.task;

import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/6 11:26 上午
 */
@Slf4j
@Component
public class DailyStatTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private SaleInfoService saleInfoService;

    @Autowired
    private ItemDetailCache itemDetailCache;


    /**
     * 统计指定日期之后n天销量数据
     *
     * @param strDate
     */
    public void statSaleInfoDurationDay(String strDate, Integer num) {
        Date date = TimeUtil.getDateBySimple(strDate);

        while (num > 0) {
            commonDealDate(date);
            date = TimeUtil.dateFixByDay(date, 1, 0, 0);
            num--;
        }
    }

    /**
     * 统计指定多个日期销量数据
     *
     * @param strDates
     */
    public void statSaleInfoByMulchDate(Set<String> strDates) {
        strDates.forEach(this::statSaleInfoChooseDate);
    }

    /**
     * 统计指定日期销量数据
     *
     * @param strDate
     */
    public void statSaleInfoChooseDate(String strDate) {
        Date date = TimeUtil.getDateBySimple(strDate);

        long startTime = System.currentTimeMillis();
        commonDealDate(date);
        log.info("【{}】销量信息修复结束，耗时:{}", strDate, System.currentTimeMillis() - startTime);
    }

    @Scheduled(cron = "0 0 17 * * ?")
    public void statDailySaleData() {
        commonDealDate(TimeUtil.getZeroUTCDateByDay(-2));
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void statTodaySaleData() {
        commonDealDate(TimeUtil.transformNowToUsDate());
    }

    private void commonDealDate(Date date) {
        String strDailyDate = TimeUtil.getDateFormat(date);
        Boolean isSummer = TimeUtil.isSummer(strDailyDate);
        Date startDate = TimeUtil.dateFixByDay(date, 0, isSummer ? 7 : 8, 0);

        Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
        strDailyDate = TimeUtil.getDateFormat(nextDate);
        isSummer = TimeUtil.isSummer(strDailyDate);
        Date endDate = TimeUtil.dateFixByDay(nextDate, 0, isSummer ? 7 : 8, 0);

        statDailySaleInfoByDate(startDate, endDate);
    }

    private void statDailySaleInfoByDate(Date startDate, Date endDate) {
        String statDate = TimeUtil.getSimpleFormat(startDate);
        List<OrderDO> orders = orderService.getOrdersByPurchaseDate(startDate, endDate, null, new String[]{"amazon_order_id"});

        //防止mybatis in 搜索优化功能：mybatis使用in搜索时，如果入仓为空，删除in条件，改为全表搜索
        //全表搜索，数据库所有数据加入内存，导致OOM
        if (orders.size() == 0) {
            return;
        }
        log.info("销量数据统计时间范围：{}----{}, 订单数量：{}", startDate, endDate, orders.size());

        List<String> amazonOrderIds = orders.stream().map(OrderDO::getAmazonOrderId).collect(Collectors.toList());
        List<OrderItemDO> itemList = orderItemService.getOrderByBathAmazonId(amazonOrderIds);

        Map<String, SaleInfoDO> saleInfoMap = Maps.newHashMap();
        itemList.forEach(orderItem -> {
            //应老板要求，销量统计数量
            if (orderItem.getQuantityOrdered() == 0) {
                return;
            }

            double itemPrice = orderItem.getItemPriceAmount() == null ? 0.0 : orderItem.getItemPriceAmount();
            SaleInfoDO saleInfoDO = saleInfoMap.get(orderItem.getSku());
            if (saleInfoDO == null) {
                saleInfoDO = new SaleInfoDO();
                saleInfoDO.setSaleNum(orderItem.getQuantityOrdered());
                saleInfoDO.setSaleVolume(itemPrice);
                saleInfoDO.setStatDate(statDate);
                saleInfoDO.setConfig("");
            } else {
                saleInfoDO.setSaleNum(saleInfoDO.getSaleNum() + orderItem.getQuantityOrdered());
                saleInfoDO.setSaleVolume(saleInfoDO.getSaleVolume() + itemPrice);
            }
            saleInfoMap.put(orderItem.getSku(), saleInfoDO);
        });

        saleInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().getSaleNum() != null && entry.getValue().getSaleNum() > 0)
                .forEach(entry -> {
                    SaleInfoDO saleInfoDO = entry.getValue();
                    saleInfoDO.setSku(entry.getKey());
                    saleInfoDO.setUnitPrice(saleInfoDO.getSaleVolume() / Double.valueOf(saleInfoDO.getSaleNum()));

                    SaleInfoDO old = saleInfoService.getSaleInfoDOByDate(statDate, entry.getKey());
                    if (old != null) {
                        saleInfoDO.setId(old.getId());
                        saleInfoDO.setConfig(old.getConfig());
                        saleInfoService.updateSaleInfo(saleInfoDO);
                    } else {
                        saleInfoService.createSaleInfo(saleInfoDO);
                    }
                });

        //刷新本地缓存
        itemDetailCache.refreshCaches(Lists.newArrayList(saleInfoMap.keySet()));
        log.info("【{}】销量数据统计完成 共计{}条数据", statDate, saleInfoMap.size());
    }
}
