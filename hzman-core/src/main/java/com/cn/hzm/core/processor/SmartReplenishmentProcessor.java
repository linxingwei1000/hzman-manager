package com.cn.hzm.core.processor;

import com.cn.hzm.api.dto.SmartReplenishmentDto;
import com.cn.hzm.core.repository.dao.ItemDao;
import com.cn.hzm.core.repository.dao.ItemInventoryDao;
import com.cn.hzm.core.repository.dao.SaleInfoDao;
import com.cn.hzm.core.repository.entity.ItemDo;
import com.cn.hzm.core.repository.entity.ItemInventoryDo;
import com.cn.hzm.core.repository.entity.SaleInfoDo;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.api.enums.ReplenishmentEnum;
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
 * @date 2021/3/2 10:23 上午
 */
@Slf4j
@Component
public class SmartReplenishmentProcessor {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SaleInfoDao saleInfoDao;

    @Autowired
    private ItemInventoryDao itemInventoryDao;

    private Map<Integer, List<SmartReplenishmentDto>> replenishmentMap;

    //<userMarketId, skus>
    private Map<Integer, List<String>> shipSkus;

    //<userMarketId, skus>
    private Map<Integer, List<String>> orderSkus;

    public SmartReplenishmentDto getSmartReplenishment(Integer userMarketId, String sku) {
        List<SmartReplenishmentDto> tmpList = replenishmentMap.get(userMarketId);
        if (!CollectionUtils.isEmpty(tmpList)) {
            for (SmartReplenishmentDto tmp : tmpList) {
                if (tmp.getSku().equals(sku)) {
                    return tmp;
                }
            }
        }
        return null;
    }

    public List<SmartReplenishmentDto> getSmartReplenishmentDTOList(Integer userMarketId, List<String> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return replenishmentMap.get(userMarketId);
        }
        return replenishmentMap.getOrDefault(userMarketId, Lists.newArrayList())
                .stream().filter(tmp -> skus.contains(tmp.getSku())).collect(Collectors.toList());
    }

    public List<String> getShipSkus(Integer userMarketId) {
        return this.shipSkus.get(userMarketId);
    }

    public List<String> getOrderSkus(Integer userMarketId) {
        return this.orderSkus.get(userMarketId);
    }

    public void init() {
        replenishmentMap = Maps.newHashMap();
        shipSkus = Maps.newHashMap();
        orderSkus = Maps.newHashMap();

        statDailySaleData();
    }


    @Scheduled(cron = "0 0 * * * ?")
    public void statDailySaleData() {
        Date curDate = TimeUtil.transformNowToUsDate();
        //以昨天日期开始计算
        Date compareEndDate = TimeUtil.dateFixByDay(curDate, -1, 0, 0);
        Map<String, List<SaleInfoDo>> compareMap = dealSaleInfoByDate(compareEndDate, -30);

//        Date lastYearCompareEndDate = TimeUtil.dateFixByYear(compareEndDate, -1);
//        Map<String, List<SaleInfoDO>> lastYearCompareMap = dealSaleInfoByDate(lastYearCompareEndDate, -30);
//
//        Date lastYearPredictEndDate = TimeUtil.dateFixByDay(lastYearCompareEndDate, 60, 0, 0);
//        Map<String, List<SaleInfoDO>> lastYearPredictMap = dealSaleInfoByDate(lastYearPredictEndDate, -30);

        List<ItemDo> itemList = itemDao.getListByCondition(Maps.newHashMap(), new String[]{"sku", "user_market_id"});

        Map<Integer, List<SmartReplenishmentDto>> tmpMap = Maps.newHashMap();
        Map<Integer, List<String>> tmpShips = Maps.newHashMap();
        Map<Integer, List<String>> tmpOrders = Maps.newHashMap();
        //计算智能补货
        itemList.forEach(item -> {
            List<SaleInfoDo> saleInfos = compareMap.get(item.getSku() + item.getUserMarketId());

            //新品无销量数据，跳过智能补货
            if (CollectionUtils.isEmpty(saleInfos)) {
                return;
            }

            long last30DaySaleNum = saleInfos.stream().map(SaleInfoDo::getSaleNum).count();
//            long curYearSaleNum = 0;
//            if (!CollectionUtils.isEmpty(compareMap.get(item.getSku()))) {
//                curYearSaleNum = compareMap.get(item.getSku()).stream().map(SaleInfoDO::getSaleNum).count();
//            }

//            long lastYearPredictNum = 0;
//            if (!CollectionUtils.isEmpty(lastYearPredictMap.get(item.getSku()))) {
//                lastYearPredictNum = lastYearPredictMap.get(item.getSku()).stream().map(SaleInfoDO::getSaleNum).count();
//            }
//            double percentSale = ((double) curYearSaleNum - (double) lastYearSaleNum) / (double) lastYearSaleNum;
//            long predictNum = (long) (lastYearPredictNum == 0 ? percentSale * lastYearSaleNum : lastYearPredictNum * (1 + percentSale));

            ItemInventoryDo inventoryDO = itemInventoryDao.getInventoryBySku(item.getSku(), item.getUserMarketId());
            if (inventoryDO != null) {
                if (inventoryDO.getTotalQuantity() >= last30DaySaleNum) {
                    if (inventoryDO.getAmazonQuantity() < last30DaySaleNum) {
                        addData(tmpMap, item.getUserMarketId(), createReplenishmentInfo(item.getSku(), last30DaySaleNum - inventoryDO.getAmazonQuantity(), ReplenishmentEnum.REPLENISHMENT_SHIP));
                        addData(tmpShips, item.getUserMarketId(), item.getSku());
                    }
                } else {
                    addData(tmpMap, item.getUserMarketId(), createReplenishmentInfo(item.getSku(), last30DaySaleNum - inventoryDO.getAmazonQuantity(), ReplenishmentEnum.REPLENISHMENT_SHIP));
                    addData(tmpOrders, item.getUserMarketId(), item.getSku());
                }
            }
        });
        replenishmentMap = tmpMap;
        shipSkus = tmpShips;
        orderSkus = tmpOrders;

        //刷新商品,依赖cache组件定时刷新
        //itemDetailCache.getCache(Lists.newArrayList(replenishmentMap.keySet()));
    }

    private Map<String, List<SaleInfoDo>> dealSaleInfoByDate(Date endDate, Integer duration) {
        Date beginDate = TimeUtil.dateFixByDay(endDate, duration, 0, 0);
        String strEndDate = TimeUtil.getSimpleFormat(endDate);
        String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
        List<SaleInfoDo> compareList = saleInfoDao.getSaleInfoByDurationDate(null, null, strBeginDate, strEndDate);
        return compareList.stream().collect(Collectors.groupingBy(saleInfoDo -> saleInfoDo.getSku() + saleInfoDo.getUserMarketId(), HashMap::new, Collectors.toCollection(ArrayList::new)));
    }

    private SmartReplenishmentDto createReplenishmentInfo(String sku, Long needNum, ReplenishmentEnum replenishmentEnum) {
        SmartReplenishmentDto smartReplenishmentDTO = new SmartReplenishmentDto();
        smartReplenishmentDTO.setSku(sku);
        smartReplenishmentDTO.setNeedNum(needNum);
        smartReplenishmentDTO.setReplenishmentCode(replenishmentEnum.getCode());
        smartReplenishmentDTO.setReplenishmentDesc(replenishmentEnum.getDesc());
        return smartReplenishmentDTO;
    }

    private <T> void addData(Map<Integer, List<T>> tmp, Integer userMarketId, T data) {
        List<T> dtos = tmp.getOrDefault(userMarketId, Lists.newArrayList());
        dtos.add(data);
        tmp.put(userMarketId, dtos);
    }
}
