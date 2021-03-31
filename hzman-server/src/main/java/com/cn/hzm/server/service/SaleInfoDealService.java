package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.server.cache.SaleInfoCache;
import com.cn.hzm.server.dto.SaleInfoDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private SaleInfoDTO dealDurationSaleInfoDTO(Date beginDate, Integer dayNum) {

        SaleInfoDTO saleInfoDTO = saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(beginDate));
        Date nextDate = TimeUtil.dateFixByDay(beginDate, 1, 0, 0);

        for (int i = 2; i <= dayNum; i++) {
            SaleInfoDTO nextSale = saleInfoCache.getDailySaleInfo(TimeUtil.getSimpleFormat(nextDate));
            saleInfoDTO.setOrderNum(nextSale.getOrderNum() + saleInfoDTO.getOrderNum());
            saleInfoDTO.setSaleNum(nextSale.getSaleNum() + saleInfoDTO.getSaleNum());
            saleInfoDTO.setSaleVolume(nextSale.getSaleVolume() + saleInfoDTO.getSaleVolume());

            nextDate = TimeUtil.dateFixByDay(nextDate, 1, 0, 0);
        }

        if (saleInfoDTO.getSaleNum() == 0) {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume());
        } else {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume() / saleInfoDTO.getSaleNum());
        }

        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(saleInfoDTO.getSaleVolume()));
        saleInfoDTO.setUnitPrice(RandomUtil.saveDefaultDecimal(saleInfoDTO.getUnitPrice()));
        return saleInfoDTO;
    }
}
