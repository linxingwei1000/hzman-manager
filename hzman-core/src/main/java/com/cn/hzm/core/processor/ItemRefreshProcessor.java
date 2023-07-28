package com.cn.hzm.core.processor;

import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.repository.dao.AwsUserMarketDao;
import com.cn.hzm.core.repository.dao.ItemDao;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.repository.entity.ItemDo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/6 11:26 上午
 */
@Slf4j
@Component
public class ItemRefreshProcessor {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private ItemService itemService;

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
            List<ItemDo> skus = itemDao.getListByCondition(Maps.newHashMap(), new String[]{"sku","user_market_id"});
            log.info("=============刷新商品任务，本次任务需刷新商品个数：{}", skus.size());
            long curTime = System.currentTimeMillis();

            Map<String, Integer> failTimes = Maps.newHashMap();
            refreshItem(skus, 1, failTimes);
            log.info("=============刷新商品任务结束，任务耗时：{} ", System.currentTimeMillis() - curTime);
        }, 5, 30 * 60, TimeUnit.SECONDS);
    }

    private void refreshItem(List<ItemDo> itemDos, Integer epoch, Map<String, Integer> failTimes) {
        List<ItemDo> failSkus = Lists.newArrayList();
        for (ItemDo itemDo : itemDos) {
            try {
                AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(itemDo.getUserMarketId());
                itemService.processSync(itemDo.getSku(), awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId());
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("刷新商品错误, {}：", itemDo.getSku(), e);

                //亚马逊后台删除商品
                if (e.getMessage().contains("invalid SellerSKU")) {
                    itemService.deleteItem(itemDo.getSku());
                    continue;
                }

                failSkus.add(itemDo);
                Integer times = failTimes.getOrDefault(itemDo.getSku(), 0);
                if (times == FAIL_LIMIT) {
                    failTimes.remove(itemDo.getSku());
                    failSkus.remove(itemDo);
                    log.info("sku【{}】刷新失败次数超限，请查看商品状态", itemDo.getSku());
                } else {
                    failTimes.put(itemDo.getSku(), ++times);
                }
            }
        }
        log.info("=============第{}轮刷新商品，失败需要重新刷新个数：{}", epoch, failSkus.size());

        if (!CollectionUtils.isEmpty(failSkus)) {
            refreshItem(failSkus, ++epoch, failTimes);
        }
    }
}
