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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spider.switch:false}")
    private Boolean spiderSwitch;

    private static final int FAIL_LIMIT = 5;

    public void init() {
        if (!spiderSwitch) {
            log.info("测试环境关闭商品刷新任务");
            return;
        }

        ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
        orderScheduledTask.scheduleWithFixedDelay(() -> {
            List<String> skus = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"}).stream().map(ItemDO::getSku).collect(Collectors.toList());
            log.info("=============刷新商品任务，本次任务需刷新商品个数：{}", skus.size());
            long curTime = System.currentTimeMillis();

            Map<String, Integer> failTimes = Maps.newHashMap();
            refreshItem(skus, 1, failTimes);
            log.info("=============刷新商品任务结束，任务耗时：{} ", System.currentTimeMillis() - curTime);
        }, 10, 30 * 60, TimeUnit.SECONDS);

        //类目刷新线程
        ScheduledThreadPoolExecutor itemCategoryScheduledTask = new ScheduledThreadPoolExecutor(1);
        itemCategoryScheduledTask.scheduleWithFixedDelay(() -> {
            List<String> skus = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"}).stream().map(ItemDO::getSku).collect(Collectors.toList());
            log.info("=============刷新商品类目信息任务，本次任务需刷新商品个数：{}", skus.size());
            long curTime = System.currentTimeMillis();

            Map<String, Integer> failTimes = Maps.newHashMap();
            refreshItemCategory(skus, 1, failTimes);
            log.info("=============刷新商品类目信息任务，任务耗时：{} ", System.currentTimeMillis() - curTime);
        }, 10, 30 * 60, TimeUnit.SECONDS);
    }

    private void refreshItem(List<String> skus, Integer epoch, Map<String, Integer> failTimes) {
        List<String> failSkus = Lists.newArrayList();
        for (String sku : skus) {
            try {
                itemDealService.processSync(sku);
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("刷新商品错误, {}：", sku, e);

                //亚马逊后台删除商品
                if (e.getMessage().contains("invalid SellerSKU")) {
                    itemDealService.deleteItem(sku);
                    continue;
                }

                failSkus.add(sku);
                Integer times = failTimes.getOrDefault(sku, 0);
                if (times == FAIL_LIMIT) {
                    failTimes.remove(sku);
                    failSkus.remove(sku);
                    log.info("sku【{}】刷新失败次数超限，请查看商品状态", sku);
                } else {
                    failTimes.put(sku, ++times);
                }
            }
        }
        log.info("=============第{}轮刷新商品，失败需要重新刷新个数：{}", epoch, failSkus.size());

        if (!CollectionUtils.isEmpty(failSkus)) {
            refreshItem(failSkus, ++epoch, failTimes);
        }
    }

    private void refreshItemCategory(List<String> skus, Integer epoch, Map<String, Integer> failTimes) {
        List<String> failSkus = Lists.newArrayList();
        for (String sku : skus) {
            try {
                ItemDO itemDO = itemService.getItemDOBySku(sku);
                itemDealService.processSaleRankInfo(itemDO);
                Thread.sleep(8000);
            } catch (Exception e) {
                log.error("刷新商品类目错误, {}：", sku, e);

                failSkus.add(sku);
                Integer times = failTimes.getOrDefault(sku, 0);
                if (times == FAIL_LIMIT) {
                    failTimes.remove(sku);
                    failSkus.remove(sku);
                    log.info("sku【{}】刷新失败次数超限，请查看商品状态", sku);
                } else {
                    failTimes.put(sku, ++times);
                }
            }
        }
        log.info("=============第{}轮刷新商品，失败需要重新刷新个数：{}", epoch, failSkus.size());

        if (!CollectionUtils.isEmpty(failSkus)) {
            refreshItemCategory(failSkus, ++epoch, failTimes);
        }
    }
}
