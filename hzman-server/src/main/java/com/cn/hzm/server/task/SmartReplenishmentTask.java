package com.cn.hzm.server.task;

import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.dto.SmartReplenishmentDTO;
import com.cn.hzm.stock.enums.ReplenishmentEnum;
import com.cn.hzm.stock.service.InventoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class SmartReplenishmentTask {

    @Autowired
    private ItemService itemService;

    @Autowired
    private SaleInfoService saleInfoService;

    @Autowired
    private InventoryService inventoryService;

    private Map<String, SmartReplenishmentDTO> replenishmentMap;

    @Value("${spider.switch:false}")
    private Boolean spiderSwitch;

    private List<String> shipSkus;

    private List<String> orderSkus;

    public SmartReplenishmentDTO getSmartReplenishment(String sku) {
        return replenishmentMap.get(sku);
    }

    public List<SmartReplenishmentDTO> getSmartReplenishmentDTOList(List<String> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return Lists.newArrayList(replenishmentMap.values());
        }
        return skus.stream().map(sku -> replenishmentMap.getOrDefault(sku, null)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<String> getShipSkus(){
        return this.shipSkus;
    }

    public List<String> getOrderSkus(){
        return this.orderSkus;
    }

    public Set<String> init() {
        replenishmentMap = Maps.newHashMap();
        shipSkus = Lists.newArrayList();
        orderSkus = Lists.newArrayList();

        statDailySaleData();

        //todo 测试数据
        if (!spiderSwitch) {
            testData();
        }


        return replenishmentMap.keySet();
    }


    private void testData() {
        SmartReplenishmentDTO smartReplenishmentDTO = createReplenishmentInfo("JZ1903033A-10", 100L, ReplenishmentEnum.REPLENISHMENT_SHIP);
        replenishmentMap.put("JZ1903033A-10", smartReplenishmentDTO);
        shipSkus.add("JZ1903033A-10");

        smartReplenishmentDTO = createReplenishmentInfo("JZ1903033A-8", 200L, ReplenishmentEnum.REPLENISHMENT_SHIP);
        replenishmentMap.put("JZ1903033A-8", smartReplenishmentDTO);
        shipSkus.add("JZ1903033A-8");

        smartReplenishmentDTO = createReplenishmentInfo("SZ7602B", 400L, ReplenishmentEnum.REPLENISHMENT_ORDER);
        replenishmentMap.put("SZ7602B", smartReplenishmentDTO);
        orderSkus.add("SZ7602B");

        smartReplenishmentDTO = createReplenishmentInfo("XL7907-18", 500L, ReplenishmentEnum.REPLENISHMENT_ORDER);
        replenishmentMap.put("XL7907-18", smartReplenishmentDTO);
        orderSkus.add("XL7907-18");
    }


    @Scheduled(cron = "0 0 17 * * ?")
    public void statDailySaleData() {
        Date curDate = TimeUtil.transformNowToUsDate();
        //以昨天日期开始计算
        Date compareEndDate = TimeUtil.dateFixByDay(curDate, -1, 0, 0);
        Map<String, List<SaleInfoDO>> compareMap = dealSaleInfoByDate(compareEndDate, -30);

        Date lastYearCompareEndDate = TimeUtil.dateFixByYear(compareEndDate, -1);
        Map<String, List<SaleInfoDO>> lastYearCompareMap = dealSaleInfoByDate(lastYearCompareEndDate, -30);

        Date lastYearPredictEndDate = TimeUtil.dateFixByDay(lastYearCompareEndDate, 60, 0, 0);
        Map<String, List<SaleInfoDO>> lastYearPredictMap = dealSaleInfoByDate(lastYearPredictEndDate, -30);

        List<ItemDO> itemList = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"});

        Map<String, SmartReplenishmentDTO> tmpMap = Maps.newHashMap();
        List<String> tmpShipList = Lists.newArrayList();
        List<String> tmpOrderList = Lists.newArrayList();
        //计算智能补货
        itemList.forEach(item -> {
            List<SaleInfoDO> saleInfos = lastYearCompareMap.get(item.getSku());

            //新品无序智能补货
            if (CollectionUtils.isEmpty(saleInfos)) {
                return;
            }

            long lastYearSaleNum = saleInfos.stream().map(SaleInfoDO::getSaleNum).count();
            long curYearSaleNum = 0;
            if (!CollectionUtils.isEmpty(compareMap.get(item.getSku()))) {
                curYearSaleNum = compareMap.get(item.getSku()).stream().map(SaleInfoDO::getSaleNum).count();
            }

            long lastYearPredictNum = 0;
            if (!CollectionUtils.isEmpty(lastYearPredictMap.get(item.getSku()))) {
                lastYearPredictNum = lastYearPredictMap.get(item.getSku()).stream().map(SaleInfoDO::getSaleNum).count();
            }

            double percentSale = ((double) curYearSaleNum - (double) lastYearSaleNum) / (double) lastYearSaleNum;

            long predictNum = (long) (lastYearPredictNum == 0 ? percentSale * lastYearSaleNum : lastYearPredictNum * (1 + percentSale));

            InventoryDO inventoryDO = inventoryService.getInventoryBySku(item.getSku());

            if (inventoryDO.getTotalQuantity() >= predictNum) {
                if (inventoryDO.getAmazonQuantity() < predictNum) {
                    tmpMap.put(item.getSku(), createReplenishmentInfo(item.getSku(), predictNum - inventoryDO.getAmazonQuantity(), ReplenishmentEnum.REPLENISHMENT_SHIP));
                    tmpShipList.add(item.getSku());
                }
            } else {
                tmpMap.put(item.getSku(), createReplenishmentInfo(item.getSku(), predictNum - inventoryDO.getTotalQuantity(), ReplenishmentEnum.REPLENISHMENT_ORDER));
                tmpOrderList.add(item.getSku());
            }
        });
        replenishmentMap = tmpMap;
        shipSkus = tmpShipList;
        orderSkus = tmpOrderList;

        //刷新商品,依赖cache组件定时刷新
        //itemDetailCache.getCache(Lists.newArrayList(replenishmentMap.keySet()));
    }

    private Map<String, List<SaleInfoDO>> dealSaleInfoByDate(Date endDate, Integer duration) {
        Date beginDate = TimeUtil.dateFixByDay(endDate, duration, 0, 0);
        String strEndDate = TimeUtil.getSimpleFormat(endDate);
        String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
        List<SaleInfoDO> compareList = saleInfoService.getSaleInfoByDurationDate(null, strBeginDate, strEndDate);
        return compareList.stream().collect(Collectors.groupingBy(SaleInfoDO::getSku, HashMap::new, Collectors.toCollection(ArrayList::new)));
    }

    private SmartReplenishmentDTO createReplenishmentInfo(String sku, Long needNum, ReplenishmentEnum replenishmentEnum) {
        SmartReplenishmentDTO smartReplenishmentDTO = new SmartReplenishmentDTO();
        smartReplenishmentDTO.setSku(sku);
        smartReplenishmentDTO.setNeedNum(needNum);
        smartReplenishmentDTO.setReplenishmentCode(replenishmentEnum.getCode());
        smartReplenishmentDTO.setReplenishmentDesc(replenishmentEnum.getDesc());
        return smartReplenishmentDTO;
    }
}
