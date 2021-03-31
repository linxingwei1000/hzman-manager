package com.cn.hzm.server.cache;

import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.dto.SaleInfoDTO;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SaleInfoService saleInfoService;


    private Map<String, SaleInfoDTO> dailySaleInfoMap;

    private static final Integer DEFAULT_DEAL_DAY_NUM = 30;

    /**
     * 获取每日销量数据
     */
    public SaleInfoDTO getDailySaleInfo(String strDate){
        SaleInfoDTO saleInfoDTO = dailySaleInfoMap.get(strDate);
        if(saleInfoDTO ==null){
            saleInfoDTO = new SaleInfoDTO();
            saleInfoDTO.setOrderNum(0);
            saleInfoDTO.setSaleNum(0);
            saleInfoDTO.setSaleVolume(0.0);
            saleInfoDTO.setUnitPrice(0.0);
        }
        return saleInfoDTO;
    }

    public List<SaleInfoDTO> getSaleInfoByDays(List<String> strDates){
        return strDates.stream().filter(strDate -> dailySaleInfoMap.containsKey(strDate))
                .map(strDate -> dailySaleInfoMap.get(strDate)).collect(Collectors.toList());
    }



    /**
     * 刷新每日销量缓存数据
     */
    public void refreshDailySaleInfo(Set<String> strDates){
        strDates.forEach(this::dealDailySaleInfo);
    }

    @PostConstruct
    public void installCacheConfig() {
        dailySaleInfoMap = Maps.newHashMap();

        Date usDate = TimeUtil.transformNowToUsDate();

        //init latest 30 day saleInfo
        initLatestDaysInfo(usDate);

        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread();
            t.setName("当日销量刷新线程");
            return t;
        });

        //定时刷新当日销量
        executor.scheduleAtFixedRate(() -> {
            Date curDate = TimeUtil.transformNowToUsDate();
            String strDate = TimeUtil.getSimpleFormat(curDate);
            dealDailySaleInfo(strDate);
            log.info("{} 当日销量刷新完成", strDate);
        }, 10, 5 * 60, TimeUnit.SECONDS);
    }

    private void initLatestDaysInfo(Date usDate) {
        int i = 1;
        while (i > DEFAULT_DEAL_DAY_NUM) {
            String strDate = TimeUtil.getSimpleFormat(usDate);
            dealDailySaleInfo(strDate);

            usDate = TimeUtil.dateFixByDay(usDate, -1, 0, 0);
            i++;
            log.info("{} 当日销量缓存完成", strDate);
        }
    }

    private void dealDailySaleInfo(String strDate) {
        List<SaleInfoDO> list = saleInfoService.getSaleInfoDOByDate(strDate);

        SaleInfoDTO saleInfoDTO = new SaleInfoDTO();
        saleInfoDTO.setOrderNum(0);
        saleInfoDTO.setSaleNum(0);
        saleInfoDTO.setSaleVolume(0.0);
        list.forEach(saleInfo -> {
            Integer orderNum = saleInfo.getOrderNum() == null ? 0 : saleInfo.getOrderNum();
            Integer saleNum = saleInfo.getSaleNum() == null ? 0 : saleInfo.getSaleNum();
            Double saleVolume = saleInfo.getSaleVolume() == null ? 0.0 : saleInfo.getSaleVolume();

            saleInfoDTO.setOrderNum(orderNum + saleInfoDTO.getOrderNum());
            saleInfoDTO.setSaleNum(saleNum + saleInfoDTO.getSaleNum());
            saleInfoDTO.setSaleVolume(saleVolume + saleInfoDTO.getSaleVolume());
        });

        if (saleInfoDTO.getSaleNum() == 0) {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume());
        } else {
            saleInfoDTO.setUnitPrice(saleInfoDTO.getSaleVolume() / saleInfoDTO.getSaleNum());
        }
        dailySaleInfoMap.put(strDate, saleInfoDTO);
    }
}
