package com.cn.hzm.server.task;

import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.order.service.SaleInfoService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
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

    /**
     * 统计指定日期之后n天销量数据
     *
     * @param strDate
     */
    public void statSaleInfoDurationDay(String strDate, Integer num) {
        Date date = TimeUtil.getDateBySimple(strDate);

        while (num > 0) {
            Date startDate = TimeUtil.transformTimeToUTC(date);
            Date endDate = TimeUtil.getUTCDayEndTime(startDate);
            statDailySaleInfoByDate(startDate, endDate);

            date = TimeUtil.dateFixByDay(date, 1, 0, 0);
            num--;
        }
    }

    /**
     * 统计指定日期销量数据
     *
     * @param strDate
     */
    public void statSaleInfoChooseDate(String strDate) {
        Date date = TimeUtil.getDateBySimple(strDate);

        Date startDate = TimeUtil.transformTimeToUTC(date);
        Date endDate = TimeUtil.getUTCDayEndTime(startDate);

        statDailySaleInfoByDate(startDate, endDate);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Scheduled(cron = "0 53 20 * * ?")
    public void statDailySaleData() {
        Date startDate = TimeUtil.getYesterdayZeroUTCDate();
        Date endDate = TimeUtil.getUTCDayEndTime(startDate);

        statDailySaleInfoByDate(startDate, endDate);
    }

//    @Scheduled(cron = "0 */5 * * * ?")
//    public void statTodaySaleData() {
//        Date startDate = TimeUtil.getYesterdayZeroUTCDate();
//        Date endDate = TimeUtil.getUTCDayEndTime(startDate);
//
//        statDailySaleInfoByDate(startDate, endDate);
//    }

    private void statDailySaleInfoByDate(Date startDate, Date endDate) {
        String statDate = TimeUtil.getSimpleFormat(startDate);

        List<OrderDO> orders = orderService.getOrdersByPurchaseDate(startDate, endDate);

        Map<String, SaleInfoDO> saleInfoMap = Maps.newHashMap();
        for (OrderDO order : orders) {
            List<OrderItemDO> itemList = orderItemService.getOrderByAmazonId(order.getAmazonOrderId());
            itemList.forEach(orderItem -> {
                SaleInfoDO saleInfoDO = saleInfoMap.get(orderItem.getSku());
                if (saleInfoDO == null) {
                    saleInfoDO = new SaleInfoDO();
                    saleInfoDO.setSaleNum(orderItem.getQuantityOrdered());
                    saleInfoDO.setSaleVolume(orderItem.getItemPriceAmount());
                    saleInfoDO.setStatDate(statDate);
                    saleInfoDO.setConfig("");
                } else {
                    saleInfoDO.setSaleNum(saleInfoDO.getSaleNum() + orderItem.getQuantityOrdered());
                    saleInfoDO.setSaleVolume(saleInfoDO.getSaleVolume() + orderItem.getItemPriceAmount());
                }
                saleInfoMap.put(orderItem.getSku(), saleInfoDO);
            });
        }


        saleInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().getSaleNum() != null && entry.getValue().getSaleNum() > 0)
                .forEach(entry -> {
                    SaleInfoDO saleInfoDO = entry.getValue();
                    saleInfoDO.setSku(entry.getKey());
                    saleInfoDO.setUnitPrice(saleInfoDO.getSaleVolume() / Double.valueOf(saleInfoDO.getSaleNum()));

                    SaleInfoDO old = saleInfoService.getSaleInfoDOByDate(statDate, entry.getKey());
                    if(old!=null){
                        saleInfoDO.setId(old.getId());
                        saleInfoDO.setConfig(old.getConfig());
                        saleInfoService.updateSaleInfo(saleInfoDO);
                    }else{
                        saleInfoService.createSaleInfo(saleInfoDO);
                    }
                });

        log.info("【{}】销量数据统计完成 共计{}条数据", statDate, saleInfoMap.size());
    }
}
