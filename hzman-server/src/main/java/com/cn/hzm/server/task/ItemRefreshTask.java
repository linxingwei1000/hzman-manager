package com.cn.hzm.server.task;

import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.cn.hzm.server.service.ItemDealService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/6 11:26 上午
 */
@Slf4j
@Component
public class ItemRefreshTask {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemDealService itemDealService;

    private static final int FAIL_LIMIT = 5;

    public void init() {
        ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderScheduledTask.scheduleWithFixedDelay(()->{
            List<String> skus = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"}).stream().map(ItemDO::getSku).collect(Collectors.toList());
            log.info("=============刷新商品任务，本次任务需刷新商品个数：{}", skus.size());
            long curTime = System.currentTimeMillis();

            Map<String, Integer> failTimes = Maps.newHashMap();
            refreshItem(skus, 1, failTimes);
            log.info("=============刷新商品任务结束，任务耗时：{} ", System.currentTimeMillis() - curTime);
                }, 10, 30 * 60, TimeUnit.SECONDS);
    }

    private void refreshItem(List<String> skus, Integer epoch, Map<String, Integer> failTimes) {
        List<String> failSkus = Lists.newArrayList();
        for (String sku : skus) {
            try {
                itemDealService.processSync(sku);
                Thread.sleep(100);
            } catch (Exception ignored) {
                failSkus.add(sku);
                Integer times = failTimes.getOrDefault(sku, 0);
                if(times == FAIL_LIMIT){
                    log.info("sku【{}】刷新失败次数超限", sku);
                    failTimes.remove(sku);
                    failSkus.remove(sku);
                }else{
                    failTimes.put(sku, ++times);
                }
            }
        }
        log.info("=============第{}轮刷新商品，失败需要重新刷新个数：{}", epoch, failSkus.size());

        if(!CollectionUtils.isEmpty(skus)){
            refreshItem(failSkus, ++epoch, failTimes);
        }
    }
}
