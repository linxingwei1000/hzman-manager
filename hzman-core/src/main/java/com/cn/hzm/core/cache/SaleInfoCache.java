package com.cn.hzm.core.cache;

import com.cn.hzm.api.dto.SaleInfoDto;
import com.cn.hzm.core.repository.dao.AwsUserMarketDao;
import com.cn.hzm.core.repository.dao.SaleInfoDao;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.repository.entity.SaleInfoDo;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/29 10:46 上午
 */
@Slf4j
@Component
public class SaleInfoCache {

    @Autowired
    private SaleInfoDao saleInfoDao;

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Value("${cache.switch:false}")
    private Boolean cacheSwitch;

    private Map<String, SaleInfoDto> dailySaleInfoMap;

    private static final String BEGIN_DATE = "2020-01-01";

    /**
     * 获取每日销量数据
     */
    public SaleInfoDto getDailySaleInfo(Integer userMarketId, String strDate) {
        SaleInfoDto saleInfoDTO = dailySaleInfoMap.get(installCacheKey(userMarketId, strDate));
        if (saleInfoDTO == null) {
            saleInfoDTO = new SaleInfoDto();
            saleInfoDTO.setOrderNum(0);
            saleInfoDTO.setSaleNum(0);
            saleInfoDTO.setSaleVolume(0.0);
            saleInfoDTO.setUnitPrice(0.0);
        }
        return saleInfoDTO;
    }

    /**
     * 刷新每日销量缓存数据
     */
    public void refreshDailySaleInfo(Integer userMarketId, Set<String> strDates) {
        strDates.forEach(strDate -> dealDailySaleInfo(userMarketId, strDate));
    }

    @PostConstruct
    public void installCacheConfig() {
        if (!cacheSwitch) {
            log.info("开发环境关闭商品缓存任务");
            return;
        }
        dailySaleInfoMap = Maps.newHashMap();
        List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.all();

        Date usDate = TimeUtil.transformNowToUsDate();

        //init latest 30 day saleInfo
        awsUserMarketDos.forEach(awsUserMarketDo -> initLatestDaysInfo(awsUserMarketDo.getId(), usDate));
        log.info("===================销量数据加载完成");

        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread();
            t.setName("当日销量刷新线程");
            return t;
        });

        //定时刷新当日销量
        executor.scheduleAtFixedRate(() -> {
            Date curDate = TimeUtil.transformNowToUsDate();
            String strDate = TimeUtil.getSimpleFormat(curDate);

            awsUserMarketDos.forEach(awsUserMarketDo -> dealDailySaleInfo(awsUserMarketDo.getId(), strDate));
            log.info("{} 当日销量刷新完成", strDate);
        }, 10, 5 * 60, TimeUnit.SECONDS);
    }

    private void initLatestDaysInfo(Integer userMarketId, Date usDate) {
        for (; ; ) {
            String strDate = TimeUtil.getSimpleFormat(usDate);
            dealDailySaleInfo(userMarketId, strDate);

            if (strDate.equals(BEGIN_DATE)) {
                break;
            }
            usDate = TimeUtil.dateFixByDay(usDate, -1, 0, 0);
        }
    }

    private void dealDailySaleInfo(Integer userMarketId, String strDate) {
        List<SaleInfoDo> list = saleInfoDao.getSaleInfoDOByDate(userMarketId, strDate);
        int totalOrderNum = 0;
        int totalSaleNum = 0;
        double totalSaleVolume = 0.0;
        double totalTaxFee = 0.0;
        double totalFbaFulfillmentFee = 0.0;
        double totalCommission = 0.0;

        for (SaleInfoDo saleInfo : list) {
            int orderNum = saleInfo.getOrderNum() == null ? 0 : saleInfo.getOrderNum();
            int saleNum = saleInfo.getSaleNum() == null ? 0 : saleInfo.getSaleNum();
            double saleVolume = saleInfo.getSaleVolume() == null ? 0.0 : saleInfo.getSaleVolume();
            double taxFee = saleInfo.getSaleTax() == null ? 0.0 : saleInfo.getSaleTax();
            double fbaFulfillmentFee = saleInfo.getFbaFulfillmentFee() == null ? 0.0 : saleInfo.getFbaFulfillmentFee();
            double commission = saleInfo.getCommission() == null ? 0.0 : saleInfo.getCommission();

            totalOrderNum += orderNum;
            totalSaleNum += saleNum;
            totalSaleVolume += saleVolume;
            totalTaxFee += taxFee;
            totalFbaFulfillmentFee += fbaFulfillmentFee;
            totalCommission += commission;
        }

        double unitPrice = totalSaleVolume;
        if (totalSaleNum != 0) {
            unitPrice = totalSaleVolume / totalSaleNum;
        }

        SaleInfoDto saleInfoDTO = new SaleInfoDto();
        saleInfoDTO.setUserMarketId(userMarketId);
        saleInfoDTO.setOrderNum(totalOrderNum);
        saleInfoDTO.setSaleNum(totalSaleNum);
        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(totalSaleVolume));
        saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(totalTaxFee));
        saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(totalFbaFulfillmentFee));
        saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(totalCommission));
        saleInfoDTO.setUnitPrice(RandomUtil.saveDefaultDecimal(unitPrice));

        //计算净收入
        double income = totalSaleVolume - totalTaxFee - totalFbaFulfillmentFee - totalCommission;
        saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
        dailySaleInfoMap.put(installCacheKey(userMarketId, strDate), saleInfoDTO);
    }

    private String installCacheKey(Integer userMarketId, String strDate) {
        return strDate + "|" + userMarketId;
    }
}
