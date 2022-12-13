package com.cn.hzm.server.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.domain.finance.event.ChargeComponent;
import com.cn.hzm.core.aws.domain.finance.event.FeeComponent;
import com.cn.hzm.core.aws.domain.finance.event.ShipmentEventList;
import com.cn.hzm.core.aws.domain.finance.event.ShipmentItem;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderFinanceDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.OrderFinanceService;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.cn.hzm.server.dto.ItemDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
    private OrderFinanceService orderFinanceService;

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
        commonDealDate(TimeUtil.getZeroUTCDateByDay(-1));
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void statTodaySaleData() {
        commonDealDate(TimeUtil.transformNowToUsDate());
    }

    private void commonDealDate(Date date) {
        String strDailyDate = TimeUtil.getDateFormat(date);
        Boolean isSummer = TimeUtil.isSummer(strDailyDate);
        Date startDate = TimeUtil.dateFixByDay(date, 0, isSummer ? 15 : 16, 0);

        Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
        strDailyDate = TimeUtil.getDateFormat(nextDate);
        isSummer = TimeUtil.isSummer(strDailyDate);
        Date endDate = TimeUtil.dateFixByDay(nextDate, 0, isSummer ? 15 : 16, 0);

        statDailySaleInfoByDate(startDate, endDate);
    }

    private void statDailySaleInfoByDate(Date startDate, Date endDate) {
        String statDate = TimeUtil.getSimpleFormat(startDate);
        List<OrderDO> orders = orderService.getOrdersByPurchaseDate(startDate, endDate, null, new String[]{"amazon_order_id", "order_status"});

        //空判断，直接返回
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        orders = orders.stream().filter(orderDO -> !ContextConst.AMAZON_STATUS_CANCELED.equals(orderDO.getOrderStatus())
                && !ContextConst.AMAZON_STATUS_DELETE.equals(orderDO.getOrderStatus())).collect(Collectors.toList());
        //防止mybatis in 搜索优化功能：mybatis使用in搜索时，如果入仓为空，删除in条件，改为全表搜索
        //全表搜索，数据库所有数据加入内存，导致OOM
        if (orders.size() == 0) {
            return;
        }
        log.info("销量数据统计时间范围：{}----{}, 订单数量：{}", startDate, endDate, orders.size());

        List<String> amazonOrderIds = orders.stream().map(OrderDO::getAmazonOrderId).collect(Collectors.toList());

        List<OrderItemDO> itemList = orderItemService.getOrderByBathAmazonId(amazonOrderIds);
        Map<String, List<OrderItemDO>> itemListMap = itemList.stream().collect(Collectors.groupingBy(OrderItemDO::getAmazonOrderId));

        List<OrderFinanceDO> orderFinanceDOS = orderFinanceService.getOrderFinanceByBathAmazonId(amazonOrderIds);
        Map<String, OrderFinanceDO> orderFinanceDOMap = orderFinanceDOS.stream().collect(Collectors.toMap(OrderFinanceDO::getAmazonOrderId, v -> v));

        Map<String, Double> itemPriceMap = Maps.newHashMap();
        Map<String, SaleInfoDO> saleInfoMap = Maps.newHashMap();

        Map<String, Map<String, SaleInfoDO>> saleDetailInfoMap = Maps.newHashMap();

        itemListMap.forEach((amazonOrderId, orderItems) -> {
            //处理商品信息
            orderItems.forEach(orderItem -> {
                //应老板要求，销量统计数量
                if (orderItem.getQuantityOrdered() == 0) {
                    return;
                }

                Double itemPrice = getItemPrice(orderItem, itemPriceMap);

                SaleInfoDO saleInfoDO = saleInfoMap.get(orderItem.getSku());
                if (saleInfoDO == null) {
                    saleInfoDO = new SaleInfoDO();
                    saleInfoDO.setSaleNum(orderItem.getQuantityOrdered());
                    saleInfoDO.setOrderNum(1);
                    saleInfoDO.setSaleVolume(itemPrice);
                    saleInfoDO.setStatDate(statDate);
                    saleInfoDO.setConfig("");
                } else {
                    saleInfoDO.setSaleNum(saleInfoDO.getSaleNum() + orderItem.getQuantityOrdered());
                    saleInfoDO.setOrderNum(saleInfoDO.getOrderNum() + 1);
                    saleInfoDO.setSaleVolume(saleInfoDO.getSaleVolume() + itemPrice);
                }
                saleInfoMap.put(orderItem.getSku(), saleInfoDO);

                //处理销量数据
                Map<String, SaleInfoDO> orderSaleInfoMap = saleDetailInfoMap.computeIfAbsent(orderItem.getSku(), k -> Maps.newHashMap());
                SaleInfoDO saleDetailInfoDO = orderSaleInfoMap.computeIfAbsent(amazonOrderId, k -> new SaleInfoDO());
                saleDetailInfoDO.setSaleNum(orderItem.getQuantityOrdered());
                saleDetailInfoDO.setOrderNum(1);
                saleDetailInfoDO.setSaleVolume(itemPrice);
                saleDetailInfoDO.setStatDate(statDate);
                saleDetailInfoDO.setConfig("");
            });

            //处理订单财务信息
            OrderFinanceDO orderFinanceDO = orderFinanceDOMap.get(amazonOrderId);
            if (orderFinanceDO == null) {
                return;
            }

            ShipmentEventList shipmentEventList = JSONObject.parseObject(orderFinanceDO.getShipmentEventList(), ShipmentEventList.class);
            List<ShipmentItem> shipmentItems = shipmentEventList.getList().get(0).getShipmentItemList().getList();
            shipmentItems.forEach(shipmentItem -> {
                SaleInfoDO saleInfoDO = saleDetailInfoMap.get(shipmentItem.getSellerSKU()).get(amazonOrderId);

                for (ChargeComponent chargeComponent : shipmentItem.getItemChargeList().getChargeComponents()) {
                    //设置税费
                    if (chargeComponent.getChargeType().equals("Tax")) {
                        saleInfoDO.setSaleTax(chargeComponent.getChargeAmount().getCurrencyAmount());
                        break;
                    }
                }

                if (shipmentItem.getItemFeeList() != null && shipmentItem.getItemFeeList().getFeeComponents() != null) {
                    for (FeeComponent feeComponent : shipmentItem.getItemFeeList().getFeeComponents()) {
                        //设置仓储管理费
                        if (feeComponent.getFeeType().equals("FBAPerUnitFulfillmentFee")) {
                            saleInfoDO.setFbaFulfillmentFee(Math.abs(feeComponent.getFeeAmount().getCurrencyAmount()));
                        }

                        //设置佣金
                        if (feeComponent.getFeeType().equals("Commission")) {
                            saleInfoDO.setCommission(Math.abs(feeComponent.getFeeAmount().getCurrencyAmount()));
                        }
                    }
                }
            });
        });

        saleInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().getSaleNum() != null && entry.getValue().getSaleNum() > 0)
                .forEach(entry -> {
                    SaleInfoDO saleInfoDO = entry.getValue();
                    saleInfoDO.setSku(entry.getKey());
                    saleInfoDO.setUnitPrice(saleInfoDO.getSaleVolume() / Double.valueOf(saleInfoDO.getSaleNum()));

                    //统计税费
                    Map<String, SaleInfoDO> orderSaleInfoMap = saleDetailInfoMap.get(entry.getKey());
                    JSONArray ja = new JSONArray();
                    double taxFee = 0;
                    double fbaFulfillmentFee = 0;
                    double commission = 0;

                    for (Map.Entry<String, SaleInfoDO> orderEntry : orderSaleInfoMap.entrySet()) {
                        JSONObject jo = new JSONObject();
                        jo.put("amazonOrderId", orderEntry.getKey());
                        jo.put("taxFee", orderEntry.getValue().getSaleTax());
                        jo.put("fbaFulfillmentFee", orderEntry.getValue().getFbaFulfillmentFee());
                        jo.put("commission", orderEntry.getValue().getCommission());
                        ja.add(jo);

                        taxFee += orderEntry.getValue().getSaleTax() == null ? 0.0 : orderEntry.getValue().getSaleTax();
                        fbaFulfillmentFee += orderEntry.getValue().getFbaFulfillmentFee() == null ? 0.0 : orderEntry.getValue().getFbaFulfillmentFee();
                        commission += orderEntry.getValue().getCommission() == null ? 0.0 : orderEntry.getValue().getCommission();
                    }

                    saleInfoDO.setSaleTax(taxFee);
                    saleInfoDO.setFbaFulfillmentFee(fbaFulfillmentFee);
                    saleInfoDO.setCommission(commission);
                    saleInfoDO.setConfig(ja.toJSONString());

                    SaleInfoDO old = saleInfoService.getSaleInfoDOByDate(statDate, entry.getKey());
                    if (old != null) {
                        saleInfoDO.setId(old.getId());
                        saleInfoService.updateSaleInfo(saleInfoDO);
                    } else {
                        saleInfoService.createSaleInfo(saleInfoDO);
                    }
                });

        //刷新本地缓存
        itemDetailCache.refreshCaches(Lists.newArrayList(saleInfoMap.keySet()));
        log.info("【{}】销量数据统计完成 共计{}条数据", statDate, saleInfoMap.size());
    }

    private Double getItemPrice(OrderItemDO orderItem, Map<String, Double> itemPriceMap) {
        Double itemPrice;
        if (orderItem.getItemPriceAmount() == null) {
            itemPrice = itemPriceMap.get(orderItem.getSku());
            if (itemPrice == null) {
                ItemDTO itemDTO = itemDetailCache.getSingleCache(orderItem.getSku());
                itemPrice = itemDTO == null ? 0.0 : itemDTO.getItemPrice();
                itemPriceMap.put(orderItem.getSku(), itemPrice);
            }
        } else {
            itemPrice = orderItem.getItemPriceAmount();
        }
        return itemPrice;
    }
}
