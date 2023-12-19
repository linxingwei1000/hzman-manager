package com.cn.hzm.core.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.ItemDto;
import com.cn.hzm.core.cache.ItemDetailCache;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.repository.dao.*;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.spa.finance.model.ChargeComponent;
import com.cn.hzm.core.spa.finance.model.FeeComponent;
import com.cn.hzm.core.spa.finance.model.ShipmentEventList;
import com.cn.hzm.core.spa.finance.model.ShipmentItem;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/6 11:26 上午
 */
@Slf4j
@Component
public class DailyStatProcessor {

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private AmazonOrderDao amazonOrderDao;

    @Autowired
    private AmazonOrderItemDao amazonOrderItemDao;

    @Autowired
    private SaleInfoDao saleInfoDao;

    @Autowired
    private AmazonOrderFinanceDao amazonOrderFinanceDao;

    @Autowired
    private ItemDetailCache itemDetailCache;


    /**
     * 统计指定日期之后n天销量数据
     *
     * @param strDate
     */
    public void statSaleInfoDurationDay(Integer awsUserMarketId, String strDate, Integer num) {
        Date date = TimeUtil.getDateBySimple(strDate);

        while (num > 0) {
            commonDealDate(awsUserMarketId, date);
            date = TimeUtil.dateFixByDay(date, 1, 0, 0);
            num--;
        }
    }

    /**
     * 统计指定多个日期销量数据
     *
     * @param strDates
     */
    public void statSaleInfoByMulchDate(Integer awsUserMarketId, Set<String> strDates) {
        strDates.forEach(strDate -> statSaleInfoChooseDate(awsUserMarketId, strDate));
    }

    /**
     * 统计指定日期销量数据
     *
     * @param awsUserMarketId
     * @param strDate
     */
    public void statSaleInfoChooseDate(Integer awsUserMarketId, String strDate) {
        Date date = TimeUtil.getDateBySimple(strDate);

        long startTime = System.currentTimeMillis();
        commonDealDate(awsUserMarketId, date);
        log.info("【{}-{}】销量信息修复结束，耗时:{}", strDate, awsUserMarketId, System.currentTimeMillis() - startTime);
    }

    @Scheduled(cron = "0 0 17 * * ?")
    public void statDailySaleData() {
        List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.all();
        awsUserMarketDos.forEach(awsUserMarketDo -> commonDealDate(awsUserMarketDo.getId(), TimeUtil.getZeroUTCDateByDay(-1)));
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void statTodaySaleData() {
        List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.all();
        awsUserMarketDos.forEach(awsUserMarketDo -> commonDealDate(awsUserMarketDo.getId(), TimeUtil.transformNowToUsDate()));
    }

    private void commonDealDate(Integer awsUserMarketId, Date date) {
        String strDailyDate = TimeUtil.getDateFormat(date);
        Boolean isSummer = TimeUtil.isSummer(strDailyDate);
        Date startDate = TimeUtil.dateFixByDay(date, 0, isSummer ? 15 : 16, 0);

        Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
        strDailyDate = TimeUtil.getDateFormat(nextDate);
        isSummer = TimeUtil.isSummer(strDailyDate);
        Date endDate = TimeUtil.dateFixByDay(nextDate, 0, isSummer ? 15 : 16, 0);

        statDailySaleInfoByDate(awsUserMarketId, startDate, endDate);
    }

    private void statDailySaleInfoByDate(Integer awsUserMarketId, Date startDate, Date endDate) {
        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(awsUserMarketId);
        String statDate = TimeUtil.getSimpleFormat(startDate);
        List<AmazonOrderDo> orders = amazonOrderDao.getOrdersByPurchaseDate(awsUserMarketId, startDate, endDate, null, new String[]{"amazon_order_id", "order_status"});
        log.info("[{}-{}]销量数据统计时间范围：{}----{}, 初始数据库订单数量：{}", awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId(), startDate, endDate, orders.size());

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
        log.info("[{}-{}]销量数据统计时间范围：{}----{}, 订单数量：{}", awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId(), startDate, endDate, orders.size());

        List<String> amazonOrderIds = orders.stream().map(AmazonOrderDo::getAmazonOrderId).collect(Collectors.toList());

        List<AmazonOrderItemDo> itemList = amazonOrderItemDao.getOrderByBathAmazonId(amazonOrderIds);
        Map<String, List<AmazonOrderItemDo>> itemListMap = itemList.stream().collect(Collectors.groupingBy(AmazonOrderItemDo::getAmazonOrderId));

        List<AmazonOrderFinanceDo> amazonOrderFinanceDOS = amazonOrderFinanceDao.getOrderFinanceByBathAmazonId(amazonOrderIds);
        Map<String, AmazonOrderFinanceDo> orderFinanceDOMap = amazonOrderFinanceDOS.stream().collect(Collectors.toMap(AmazonOrderFinanceDo::getAmazonOrderId, v -> v));

        Map<String, Double> itemPriceMap = Maps.newHashMap();
        Map<String, SaleInfoDo> saleInfoMap = Maps.newHashMap();

        Map<String, Map<String, SaleInfoDo>> saleDetailInfoMap = Maps.newHashMap();

        itemListMap.forEach((amazonOrderId, orderItems) -> {
            //处理商品信息
            orderItems.forEach(orderItem -> {
                //应老板要求，销量统计数量
                if (orderItem.getQuantityOrdered() == 0) {
                    return;
                }

                Double itemPrice = getItemPrice(awsUserMarketId, orderItem, itemPriceMap);

                SaleInfoDo saleInfoDO = saleInfoMap.get(orderItem.getSku());
                if (saleInfoDO == null) {
                    saleInfoDO = new SaleInfoDo();
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
                Map<String, SaleInfoDo> orderSaleInfoMap = saleDetailInfoMap.computeIfAbsent(orderItem.getSku(), k -> Maps.newHashMap());
                SaleInfoDo saleDetailInfoDO = orderSaleInfoMap.computeIfAbsent(amazonOrderId, k -> new SaleInfoDo());
                saleDetailInfoDO.setSaleNum(orderItem.getQuantityOrdered());
                saleDetailInfoDO.setOrderNum(1);
                saleDetailInfoDO.setSaleVolume(itemPrice);
                saleDetailInfoDO.setStatDate(statDate);
                saleDetailInfoDO.setConfig("");
            });

            //处理订单财务信息
            AmazonOrderFinanceDo amazonOrderFinanceDO = orderFinanceDOMap.get(amazonOrderId);
            if (amazonOrderFinanceDO == null) {
                return;
            }

            if (amazonOrderFinanceDO.getShipmentEventList().startsWith("{\"list\"")) {
                oldFinance(amazonOrderFinanceDO, saleDetailInfoMap, amazonOrderId);
            } else {
                newFinance(amazonOrderFinanceDO, saleDetailInfoMap, amazonOrderId);
            }
        });

        saleInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().getSaleNum() != null && entry.getValue().getSaleNum() > 0)
                .forEach(entry -> {
                    SaleInfoDo saleInfoDo = entry.getValue();
                    saleInfoDo.setSku(entry.getKey());
                    saleInfoDo.setUnitPrice(saleInfoDo.getSaleVolume() / Double.valueOf(saleInfoDo.getSaleNum()));

                    //统计税费
                    Map<String, SaleInfoDo> orderSaleInfoMap = saleDetailInfoMap.get(entry.getKey());
                    JSONArray ja = new JSONArray();
                    double taxFee = 0;
                    double fbaFulfillmentFee = 0;
                    double commission = 0;

                    for (Map.Entry<String, SaleInfoDo> orderEntry : orderSaleInfoMap.entrySet()) {
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

                    saleInfoDo.setSaleTax(taxFee);
                    saleInfoDo.setFbaFulfillmentFee(fbaFulfillmentFee);
                    saleInfoDo.setCommission(commission);
                    saleInfoDo.setConfig(ja.toJSONString());
                    saleInfoDo.setUserMarketId(awsUserMarketId);

                    SaleInfoDo old = saleInfoDao.getSaleInfoDOByDate(statDate, awsUserMarketId, entry.getKey());
                    if (old != null) {
                        saleInfoDo.setId(old.getId());
                        saleInfoDao.updateSaleInfo(saleInfoDo);
                    } else {
                        saleInfoDao.createSaleInfo(saleInfoDo);
                    }
                });

        //刷新本地缓存

        itemDetailCache.refreshCaches(awsUserMarketId, Lists.newArrayList(saleInfoMap.keySet()));
        log.info("【{}-{}-{}】销量数据统计完成 共计{}条数据", awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId(), statDate, saleInfoMap.size());
    }

    private Double getItemPrice(Integer awsUserMarketId, AmazonOrderItemDo orderItem, Map<String, Double> itemPriceMap) {
        Double itemPrice;
        if (orderItem.getItemPriceAmount() == null) {
            itemPrice = itemPriceMap.get(orderItem.getSku());
            if (itemPrice == null) {
                ItemDto itemDTO = itemDetailCache.getSingleCache(awsUserMarketId, orderItem.getSku());
                itemPrice = itemDTO == null ? 0.0 : itemDTO.getItemPrice();
                itemPriceMap.put(orderItem.getSku(), itemPrice);
            }
        } else {
            itemPrice = orderItem.getItemPriceAmount();
        }
        return itemPrice;
    }

    private void oldFinance(AmazonOrderFinanceDo amazonOrderFinanceDO, Map<String, Map<String, SaleInfoDo>> saleDetailInfoMap, String amazonOrderId) {
        JSONObject eventJo = JSONObject.parseObject(amazonOrderFinanceDO.getShipmentEventList());
        JSONArray listJa = eventJo.getJSONArray("list");
        JSONObject itemJo = listJa.getJSONObject(0).getJSONObject("shipmentItemList").getJSONArray("list").getJSONObject(0);

        SaleInfoDo saleInfoDO = saleDetailInfoMap.get(itemJo.getString("sellerSKU")).get(amazonOrderId);

        if(itemJo.containsKey("itemChargeList")) {
            if (itemJo.getJSONObject("itemChargeList").containsKey("chargeComponents")) {
                JSONArray chargeJa = itemJo.getJSONObject("itemChargeList").getJSONArray("chargeComponents");
                for (int i = 0; i < chargeJa.size(); i++) {
                    JSONObject chargeJo = chargeJa.getJSONObject(0);
                    //设置税费
                    if (chargeJo.getString("chargeType").equals("Tax")) {
                        saleInfoDO.setSaleTax(chargeJo.getJSONObject("chargeAmount").getDouble("currencyAmount"));
                        break;
                    }
                }
            }
        }

        if(itemJo.containsKey("itemFeeList")){
            if(itemJo.getJSONObject("itemFeeList").containsKey("feeComponents")){
                JSONArray itemFeeJa = itemJo.getJSONObject("itemFeeList").getJSONArray("feeComponents");
                for (int i = 0; i < itemFeeJa.size(); i++) {
                    JSONObject feeJo = itemFeeJa.getJSONObject(0);

                    //设置仓储管理费
                    if (feeJo.getString("feeType").equals("FBAPerUnitFulfillmentFee")) {
                        saleInfoDO.setFbaFulfillmentFee(Math.abs(feeJo.getJSONObject("feeAmount").getDouble("currencyAmount")));
                    }

                    //设置佣金
                    if (feeJo.getString("feeType").equals("Commission")) {
                        saleInfoDO.setCommission(Math.abs(feeJo.getJSONObject("feeAmount").getDouble("currencyAmount")));
                    }
                }
            }
        }
    }

    private void newFinance(AmazonOrderFinanceDo amazonOrderFinanceDO, Map<String, Map<String, SaleInfoDo>> saleDetailInfoMap, String amazonOrderId) {
        ShipmentEventList shipmentEventList = JSONObject.parseObject(amazonOrderFinanceDO.getShipmentEventList(), ShipmentEventList.class);
        if (CollectionUtils.isEmpty(shipmentEventList)) {
            return;
        }
        List<ShipmentItem> shipmentItems = shipmentEventList.get(0).getShipmentItemList();
        shipmentItems.forEach(shipmentItem -> {
            SaleInfoDo saleInfoDO = saleDetailInfoMap.get(shipmentItem.getSellerSKU()).get(amazonOrderId);

            for (ChargeComponent chargeComponent : shipmentItem.getItemChargeList()) {
                //设置税费
                if (chargeComponent.getChargeType().equals("Tax")) {
                    saleInfoDO.setSaleTax(chargeComponent.getChargeAmount().getCurrencyAmount().doubleValue());
                    break;
                }
            }

            if (shipmentItem.getItemFeeList() != null) {
                for (FeeComponent feeComponent : shipmentItem.getItemFeeList()) {
                    //设置仓储管理费
                    if (feeComponent.getFeeType().equals("FBAPerUnitFulfillmentFee")) {
                        saleInfoDO.setFbaFulfillmentFee(Math.abs(feeComponent.getFeeAmount().getCurrencyAmount().doubleValue()));
                    }

                    //设置佣金
                    if (feeComponent.getFeeType().equals("Commission")) {
                        saleInfoDO.setCommission(Math.abs(feeComponent.getFeeAmount().getCurrencyAmount().doubleValue()));
                    }
                }
            }
        });
    }
}
