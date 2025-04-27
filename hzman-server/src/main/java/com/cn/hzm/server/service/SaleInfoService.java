package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.*;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.repository.dao.SaleInfoDao;
import com.cn.hzm.core.repository.entity.SaleInfoDo;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.core.cache.SaleInfoCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/29 11:25 上午
 */
@Slf4j
@Service
public class SaleInfoService {

    @Autowired
    private SaleInfoCache saleInfoCache;

    @Autowired
    private SaleInfoDao saleInfoDao;

    public JSONObject getCurSaleInfo() {
        Date usDate = TimeUtil.transformNowToUsDate();
        Date yesterday = TimeUtil.dateFixByDay(usDate, -1, 0, 0);
        Date recent30Day = TimeUtil.dateFixByDay(usDate, -30, 0, 0);
        Date latestYearBegin = TimeUtil.dateFixByYear(recent30Day, -1);


        JSONObject jo = new JSONObject();
        jo.put("today", saleInfoCache.getDailySaleInfo(ThreadLocalCache.getUser().getUserMarketId(), TimeUtil.getSimpleFormat(usDate)));
        jo.put("yesterday", saleInfoCache.getDailySaleInfo(ThreadLocalCache.getUser().getUserMarketId(), TimeUtil.getSimpleFormat(yesterday)));
        jo.put("recent", dealDurationSaleInfoDTO(recent30Day, 30));
        jo.put("latestYear", dealDurationSaleInfoDTO(latestYearBegin, 30));

        return jo;
    }

    public HisSaleInfoAggrDto getSkuHisSaleInfo(String sku, Integer userMarketId){
        Date usDate = TimeUtil.transformNowToUsDate();
        HisSaleInfoAggrDto hisSaleInfoAggrDto = new HisSaleInfoAggrDto();
        hisSaleInfoAggrDto.setDuration30Day(getSaleInfoByDurationDate(usDate, sku, userMarketId));
        hisSaleInfoAggrDto.setDuration3060Day(getSaleInfoByDurationDate(TimeUtil.dateFixByDay(usDate, -30, 0, 0), sku, userMarketId));
        hisSaleInfoAggrDto.setLastYearDuration30Day(getSaleInfoByDurationDate(TimeUtil.dateFixByYear(usDate, -1), sku, userMarketId));
        return hisSaleInfoAggrDto;
    }

    public List<SaleInfoDurationDto> getSaleInfo(SaleConditionDto saleConditionDTO) {
        if(StringUtils.isEmpty(saleConditionDTO.getBeginDate())){
            throw new HzmException(ExceptionCode.SALE_INFO_BEGIN_MUST);
        }

        if(StringUtils.isEmpty(saleConditionDTO.getEndDate())){
            throw new HzmException(ExceptionCode.SALE_INFO_END_MUST);
        }

        List<SaleInfoDo> resultList;
        List<String> dates;
        if(saleConditionDTO.getType().equals(1)){
            dates = TimeUtil.getDailyDateByDuration(saleConditionDTO.getBeginDate(), saleConditionDTO.getEndDate());
            resultList = saleInfoDao.getSaleInfoByDurationDate(saleConditionDTO.getSku(),
                    saleConditionDTO.getUserMarketId(), saleConditionDTO.getBeginDate(), saleConditionDTO.getEndDate());
        }else{
            dates = TimeUtil.getMonthDateByDuration(saleConditionDTO.getBeginDate(), saleConditionDTO.getEndDate());
            resultList = saleInfoDao.getMonthSaleInfoByDurationDate(saleConditionDTO.getSku(),
                    saleConditionDTO.getUserMarketId(), saleConditionDTO.getBeginDate(), saleConditionDTO.getEndDate());
        }

        Map<String, SaleInfoDo> resultMap = CollectionUtils.isEmpty(resultList) ?
                Maps.newHashMap() : resultList.stream().collect(Collectors.toMap(SaleInfoDo::getStatDate, a -> a));

        List<SaleInfoDurationDto> saleInfoDurationDtos = Lists.newArrayList();
        dates.forEach(statDate ->{
            SaleInfoDo saleInfoDo = resultMap.get(statDate);
            SaleInfoDescDto saleInfoDTO;
            if(saleInfoDo == null){
                saleInfoDTO = new SaleInfoDescDto();
                saleInfoDTO.setSaleNum(0);
                saleInfoDTO.setOrderNum(0);
                saleInfoDTO.setSaleVolume(0.0);
                saleInfoDTO.setUnitPrice(0.0);
                saleInfoDTO.setSaleTax(0.0);
                saleInfoDTO.setFbaFulfillmentFee(0.0);
                saleInfoDTO.setCommission(0.0);
                saleInfoDTO.setIncome(0.0);
            }else{
                saleInfoDTO = new SaleInfoDescDto();
                saleInfoDTO.setSaleNum(saleInfoDo.getSaleNum());
                saleInfoDTO.setOrderNum(saleInfoDo.getOrderNum());
                saleInfoDTO.setSaleVolume(saleInfoDo.getSaleVolume());
                if (saleInfoDo.getSaleNum() == 0) {
                    saleInfoDTO.setUnitPrice(0.0);
                } else {
                    saleInfoDTO.setUnitPrice(saleInfoDo.getSaleVolume() / (double) saleInfoDo.getSaleNum());
                }
                saleInfoDTO.setSaleTax(saleInfoDo.getSaleTax());
                saleInfoDTO.setFbaFulfillmentFee(saleInfoDo.getFbaFulfillmentFee());
                saleInfoDTO.setCommission(saleInfoDo.getCommission());
                double income = saleInfoDTO.getSaleVolume() - saleInfoDTO.getSaleTax() - saleInfoDTO.getFbaFulfillmentFee() - saleInfoDTO.getCommission();
                saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
            }
            saleInfoDTO.setSaleDate(statDate);
            saleInfoDTO.setSku(saleConditionDTO.getSku());

            SaleInfoDurationDto saleInfoDurationDto = new SaleInfoDurationDto();
            saleInfoDurationDto.setDate(saleInfoDTO.getSaleDate());
            saleInfoDurationDto.setNum(saleInfoDTO.getSaleNum());
            saleInfoDurationDto.setDetailInfo(saleInfoDTO);
            saleInfoDurationDtos.add(saleInfoDurationDto);
        });
        return saleInfoDurationDtos;
    }

    private SaleInfoDto getSaleInfoByDurationDate(Date date, String sku, Integer userMarketId) {
        Date beginDate = TimeUtil.dateFixByDay(date, -30, 0, 0);
        String strEndDate = TimeUtil.getSimpleFormat(date);
        String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
        List<SaleInfoDo> compareList = saleInfoDao.getSaleInfoByDurationDate(sku, userMarketId, strBeginDate, strEndDate);

        int saleNum = 0;
        int orderNum = 0;
        double saleVolume = 0.0;
        double taxFee = 0.0;
        double fbaFulfillmentFee = 0.0;
        double commission = 0.0;
        if (!CollectionUtils.isEmpty(compareList)) {
            for (SaleInfoDo saleInfo : compareList) {
                saleNum += saleInfo.getSaleNum();
                orderNum += saleInfo.getOrderNum();
                saleVolume += saleInfo.getSaleVolume();
                taxFee += saleInfo.getSaleTax();
                fbaFulfillmentFee += saleInfo.getFbaFulfillmentFee();
                commission += saleInfo.getCommission();
            }
        }
        SaleInfoDto saleInfoDTO = new SaleInfoDto();
        saleInfoDTO.setSaleNum(saleNum);
        saleInfoDTO.setOrderNum(orderNum);
        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(saleVolume));
        if (saleNum == 0) {
            saleInfoDTO.setUnitPrice(0.0);
        } else {
            saleInfoDTO.setUnitPrice(saleVolume / (double) saleNum);
        }

        saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(taxFee));
        saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(fbaFulfillmentFee));
        saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(commission));
        //计算净收入
        double income = saleVolume - taxFee - fbaFulfillmentFee - commission;
        saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
        return saleInfoDTO;
    }

    private SaleInfoDto dealDurationSaleInfoDTO(Date beginDate, Integer dayNum) {
        SaleInfoDto saleInfoDTO = saleInfoCache.getDailySaleInfo(ThreadLocalCache.getUser().getUserMarketId(), TimeUtil.getSimpleFormat(beginDate));
        Date nextDate = TimeUtil.dateFixByDay(beginDate, 1, 0, 0);

        for (int i = 2; i <= dayNum; i++) {
            SaleInfoDto nextSale = saleInfoCache.getDailySaleInfo(ThreadLocalCache.getUser().getUserMarketId(), TimeUtil.getSimpleFormat(nextDate));
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
