package com.cn.hzm.server.cache;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.cache.comparator.*;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.service.ItemDealService;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/13 10:38 上午
 */
@Slf4j
@Component
public class ItemDetailCache {

    private LoadingCache<String, ItemDTO> cache;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemDealService itemDealService;

    private Map<String, ItemDTO> oldDTOMAp;

    private TreeSet<ItemDTO> todaySaleDesc;
    private TreeSet<ItemDTO> todaySaleAsc;
    private TreeSet<ItemDTO> yesterdaySaleDesc;
    private TreeSet<ItemDTO> yesterdaySaleAsc;
    private TreeSet<ItemDTO> lastWeekSaleDesc;
    private TreeSet<ItemDTO> lastWeekSaleAsc;

    @PostConstruct
    public void installCacheConfig() {

        oldDTOMAp = Maps.newHashMap();

        todaySaleDesc = Sets.newTreeSet(new TodaySaleDescComparator());
        todaySaleAsc = Sets.newTreeSet(new TodaySaleAscComparator());
        yesterdaySaleDesc = Sets.newTreeSet(new YesterdaySaleDescComparator());
        yesterdaySaleAsc = Sets.newTreeSet(new YesterdaySaleAscComparator());
        lastWeekSaleDesc = Sets.newTreeSet(new LastWeekSaleDescComparator());
        lastWeekSaleAsc = Sets.newTreeSet(new LastWeekSaleAscComparator());

        cache = Caffeine.newBuilder()
                .maximumSize(10000)
                // 对象超过设定时间没有访问就会过期
                //.expireAfterAccess(60, TimeUnit.MINUTES)
                // 定时刷新
                .refreshAfterWrite(5, TimeUnit.MINUTES)
                // 初始化容量
                .initialCapacity(10000 / 10)
                .build(this::installItemDTO);

        //异步加载数据
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("商品详情缓存线程");
            return t;
        });

        executor.submit(() -> {
            List<ItemDO> items = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"});

            long startTime = System.currentTimeMillis();
            log.info("商品详情缓存加载流程开始，需缓存个数：{}", items.size());
            items.forEach(itemDO -> cache.put(itemDO.getSku(), installItemDTO(itemDO.getSku())));
            log.info("商品详情缓存加载流程结束，耗时：{}", System.currentTimeMillis() - startTime);
        });
    }

    /**
     * 根据排序获取缓存
     * @param sortType
     * @return
     */
    public List<ItemDTO> getCacheBySort(Integer sortType){
        //默认按今天倒排
        if(sortType ==null){
            sortType = ContextConst.ITEM_SORT_TODAY_DESC;
        }

        switch (sortType){
            case ContextConst.ITEM_SORT_TODAY_DESC:
                return Lists.newArrayList(todaySaleDesc);
            case ContextConst.ITEM_SORT_TODAY_ASC:
                return Lists.newArrayList(todaySaleAsc);
            case ContextConst.ITEM_SORT_YESTERDAY_DESC:
                return Lists.newArrayList(yesterdaySaleDesc);
            case ContextConst.ITEM_SORT_YESTERDAY_ASC:
                return Lists.newArrayList(yesterdaySaleAsc);
            case ContextConst.ITEM_SORT_LAST_WEEK_DESC:
                return Lists.newArrayList(lastWeekSaleDesc);
            case ContextConst.ITEM_SORT_LAST_WEEK_ASC:
                return Lists.newArrayList(lastWeekSaleAsc);
            default:
                throw new IllegalStateException("Unexpected value: " + sortType);
        }
    }

    /**
     * 根据sku获取缓存
     * @param skus
     * @return
     */
    public List<ItemDTO> getCache(List<String> skus) {
        Map<String, ItemDTO> cacheMap = cache.getAll(skus);
        return Lists.newArrayList(cacheMap.values());
    }

    public void refreshCaches(List<String> skus) {
        skus.forEach(sku -> cache.refresh(sku));
    }

    public void refreshCache(String sku) {
        cache.refresh(sku);
    }


    public ItemDTO installItemDTO(String sku) {
        ItemDO itemDO = itemService.getItemDOBySku(sku);
        ItemDTO itemDTO = itemDealService.buildItemDTO(itemDO);

        ItemDTO old = oldDTOMAp.get(sku);
        //删除老的
        if(old!=null){
            todaySaleDesc.remove(old);
            todaySaleAsc.remove(old);
            yesterdaySaleDesc.remove(old);
            yesterdaySaleAsc.remove(old);
            lastWeekSaleDesc.remove(old);
            lastWeekSaleAsc.remove(old);
            oldDTOMAp.put(sku, itemDTO);
        }

        //添加新的
        todaySaleDesc.add(itemDTO);
        todaySaleAsc.add(itemDTO);
        yesterdaySaleDesc.add(itemDTO);
        yesterdaySaleAsc.add(itemDTO);
        lastWeekSaleDesc.add(itemDTO);
        lastWeekSaleAsc.add(itemDTO);

        return itemDTO;
    }
}
