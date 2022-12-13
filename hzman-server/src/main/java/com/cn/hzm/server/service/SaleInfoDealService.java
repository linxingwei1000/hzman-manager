package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.SaleInfoCache;
import com.cn.hzm.server.dto.SaleConditionDTO;
import com.cn.hzm.server.dto.SaleInfoDTO;
import com.cn.hzm.server.dto.SaleInfoDescDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/29 11:25 上午
 */
@Slf4j
@Service
public class SaleInfoDealService {

    @Autowired
    private SaleInfoCache saleInfoCache;

    @Autowired
    private SaleInfoService saleInfoService;

    public JSONObject getCurSaleInfo() {
        Date usDate = TimeUtil.transformNowToUsDate();
        Date yesterday = TimeUtil.dateFixByDay(usDate, -1, 0, 0);
        Date recent30Day = TimeUtil.dateFixByDay(usDate, -30, 0, 0);
        Date latestYearBegin = TimeUtil.dateFixByYear(recent30Day, -1);


        JSONObject jo = new JSONObject();
        jo.put("today", saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(usDate)));
        jo.put("yesterday", saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(yesterday)));
        jo.put("recent", dealDurationSaleInfoDTO(recent30Day, 30));
        jo.put("latestYear", dealDurationSaleInfoDTO(latestYearBegin, 30));

        return jo;
    }

    public JSONObject getSaleInfo(SaleConditionDTO saleConditionDTO) {
        String endDate;
        Date eDate;
        if (StringUtils.isEmpty(saleConditionDTO.getEndDate())) {
            eDate = TimeUtil.transformNowToUsDate();
            endDate = TimeUtil.getSimpleFormat(eDate);
        } else {
            endDate = saleConditionDTO.getEndDate();
            eDate = TimeUtil.getDateBySimple(endDate);
        }

        String beginDate;
        Date bDate;
        if (StringUtils.isEmpty(saleConditionDTO.getBeginDate())) {
            bDate = TimeUtil.dateFixByDay(eDate, -30, 0, 0);
            beginDate = TimeUtil.getSimpleFormat(bDate);
        } else {
            beginDate = saleConditionDTO.getBeginDate();
            bDate = TimeUtil.getDateBySimple(beginDate);
        }

        Date nextDate = TimeUtil.dateFixByDay(bDate, 1, 0, 0);
        List<SaleInfoDescDTO> saleInfos = Lists.newArrayList();
        if (StringUtils.isEmpty(saleConditionDTO.getSku())) {
            while (true) {
                SaleInfoDTO saleInfoDTO = saleInfoCache.getDailySaleInfo(beginDate);
                SaleInfoDescDTO saleInfoDescDTO = new SaleInfoDescDTO();
                saleInfoDescDTO.setSaleNum(saleInfoDTO.getSaleNum());
                saleInfoDescDTO.setOrderNum(saleInfoDTO.getOrderNum());
                saleInfoDescDTO.setSaleVolume(saleInfoDTO.getSaleVolume());
                saleInfoDescDTO.setUnitPrice(saleInfoDTO.getUnitPrice());
                saleInfoDescDTO.setSaleTax(saleInfoDTO.getSaleTax());
                saleInfoDescDTO.setFbaFulfillmentFee(saleInfoDTO.getFbaFulfillmentFee());
                saleInfoDescDTO.setCommission(saleInfoDTO.getCommission());
                saleInfoDescDTO.setSaleDate(beginDate);

                //计算净收入
                double income = saleInfoDescDTO.getSaleVolume() - saleInfoDescDTO.getSaleTax() - saleInfoDescDTO.getFbaFulfillmentFee() - saleInfoDescDTO.getCommission();
                saleInfoDescDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
                saleInfoDescDTO.setSku("all");
                saleInfos.add(saleInfoDescDTO);
                if (beginDate.equals(endDate)) {
                    break;
                }
                beginDate = TimeUtil.getSimpleFormat(nextDate);
                nextDate = TimeUtil.dateFixByDay(nextDate, 1, 0, 0);
            }
        } else {
            List<SaleInfoDO> compareList = saleInfoService.getSaleInfoByDurationDate(saleConditionDTO.getSku(), beginDate, endDate);
            if (!CollectionUtils.isEmpty(compareList)) {
                compareList.forEach(saleInfoDO -> {
                    SaleInfoDescDTO saleInfoDTO = new SaleInfoDescDTO();
                    saleInfoDTO.setSaleNum(saleInfoDO.getSaleNum());
                    saleInfoDTO.setOrderNum(saleInfoDO.getOrderNum());
                    saleInfoDTO.setSaleVolume(saleInfoDO.getSaleVolume());
                    if (saleInfoDO.getSaleNum() == 0) {
                        saleInfoDTO.setUnitPrice(0.0);
                    } else {
                        saleInfoDTO.setUnitPrice(saleInfoDO.getSaleVolume() / (double) saleInfoDO.getSaleNum());
                    }
                    saleInfoDTO.setSaleDate(saleInfoDO.getStatDate());
                    saleInfoDTO.setSku(saleInfoDO.getSku());
                    saleInfoDTO.setSaleTax(saleInfoDO.getSaleTax());
                    saleInfoDTO.setFbaFulfillmentFee(saleInfoDO.getFbaFulfillmentFee());
                    saleInfoDTO.setCommission(saleInfoDO.getCommission());
                    double income = saleInfoDTO.getSaleVolume() - saleInfoDTO.getSaleTax() - saleInfoDTO.getFbaFulfillmentFee() - saleInfoDTO.getCommission();
                    saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));

                    saleInfos.add(saleInfoDTO);
                });
            }
        }
        JSONObject jo = new JSONObject();
        jo.put("num", saleInfos.size());
        jo.put("info", saleInfos);
        return jo;
    }

    private SaleInfoDTO dealDurationSaleInfoDTO(Date beginDate, Integer dayNum) {

        SaleInfoDTO saleInfoDTO = saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(beginDate));
        Date nextDate = TimeUtil.dateFixByDay(beginDate, 1, 0, 0);

        for (int i = 2; i <= dayNum; i++) {
            SaleInfoDTO nextSale = saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(nextDate));
            saleInfoDTO.setOrderNum(nextSale.getOrderNum() + saleInfoDTO.getOrderNum());
            saleInfoDTO.setSaleNum(nextSale.getSaleNum() + saleInfoDTO.getSaleNum());
            saleInfoDTO.setSaleVolume(nextSale.getSaleVolume() + saleInfoDTO.getSaleVolume());
            saleInfoDTO.setSaleTax(nextSale.getSaleTax() + saleInfoDTO.getSaleTax());
            saleInfoDTO.setFbaFulfillmentFee(nextSale.getFbaFulfillmentFee() + saleInfoDTO.getFbaFulfillmentFee());
            saleInfoDTO.setCommission(nextSale.getCommission() + saleInfoDTO.getCommission());

            nextDate = TimeUtil.dateFixByDay(nextDate, 1, 0, 0);
        }

        if (saleInfoDTO.getSaleNum() == 0) {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume());
        } else {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume() / saleInfoDTO.getSaleNum());
        }

        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(saleInfoDTO.getSaleVolume()));
        saleInfoDTO.setUnitPrice(RandomUtil.saveDefaultDecimal(saleInfoDTO.getUnitPrice()));
        saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(saleInfoDTO.getSaleTax()));
        saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(saleInfoDTO.getFbaFulfillmentFee()));
        saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(saleInfoDTO.getCommission()));

        //计算净收入
        double income = saleInfoDTO.getSaleVolume() - saleInfoDTO.getSaleTax() - saleInfoDTO.getFbaFulfillmentFee() - saleInfoDTO.getCommission();
        saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
        return saleInfoDTO;
    }
}
