package com.cn.hzm.server.cache;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.cache.comparator.*;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.task.SmartReplenishmentTask;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private SmartReplenishmentTask smartReplenishmentTask;

    private Map<String, ItemDTO> skuMap;

    private Map<Integer, Comparator<ItemDTO>> comparatorMap;

    private Queue<String> newItemSku;

    @PostConstruct
    public void installCacheConfig() {

        skuMap = Maps.newHashMap();

        newItemSku = Lists.newLinkedList();

        comparatorMap = Maps.newHashMap();
        comparatorMap.put(ContextConst.ITEM_SORT_TODAY_DESC, new TodaySaleDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_TODAY_ASC, new TodaySaleAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_YESTERDAY_DESC, new YesterdaySaleDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_YESTERDAY_ASC, new YesterdaySaleAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_SALE_INVENTORY_DESC, new SaleInventoryDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_SALE_INVENTORY_ASC, new SaleInventoryAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_LOCAL_INVENTORY_DESC, new LocalInventoryDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_LOCAL_INVENTORY_ASC, new LocalInventoryAscComparator());

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

        //启动程序计算智能补货
        Set<String> dealSku = smartReplenishmentTask.init();

        executor.submit(() -> {
            List<ItemDO> items = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"});

            long startTime = System.currentTimeMillis();
            log.info("商品详情缓存加载流程开始，需缓存个数：{}", items.size());
            items.forEach(itemDO -> {
                if (!dealSku.contains(itemDO.getSku())) {
                    cache.put(itemDO.getSku(), installItemDTO(itemDO.getSku()));
                }
            });
            log.info("商品详情缓存加载流程结束，耗时：{}", System.currentTimeMillis() - startTime);
        });

        //新品爬去线程
        ExecutorService itemExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("异常商品信息爬取线程");
            return t;
        });

        itemExecutor.submit(()->{
            while(true){
                String sku = newItemSku.poll();
                if(StringUtils.isEmpty(sku)){
                    Thread.sleep(60 * 1000);
                    continue;
                }
                try{
                    log.info("异常导致商品数据未获取sku【{}】，{}获取商品详情", sku, Thread.currentThread().getName());
                    itemDealService.processSync(sku);
                }catch (Exception e){
                    newItemSku.offer(sku);
                }

            }
        });
    }

    /**
     * 根据排序获取缓存
     *
     * @param sortType
     * @return
     */
    public List<ItemDTO> getCacheBySort(Integer searchType, String key, Integer sortType) {

        Collection<ItemDTO> temp = skuMap.values();
        if (!StringUtils.isEmpty(key)) {
            switch (searchType) {
                //sku 过滤
                case 1:
                    temp = temp.stream().filter(item -> item.getSku().contains(key)).collect(Collectors.toList());
                    break;
                //title 过滤
                case 2:
                    String[] keys = key.split(" ");
                    temp = temp.stream().filter(item -> {
                        for (String subKey : keys) {
                            if (!item.getTitle().toLowerCase().contains(subKey.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
                    break;
                default:
            }
        }

        Comparator<ItemDTO> comparator = comparatorMap.getOrDefault(sortType, new TodaySaleDescComparator());
        Set<ItemDTO> list = Sets.newTreeSet(comparator);
        list.addAll(temp);
        return Lists.newArrayList(list);
    }

    /**
     * 根据sku获取缓存
     *
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
        if (itemDO == null) {
            newItemSku.offer(sku);
            return null;
        }

        ItemDTO itemDTO = itemDealService.buildItemDTO(itemDO);

        //添加或者覆盖
        skuMap.put(sku, itemDTO);

        return itemDTO;
    }
}
