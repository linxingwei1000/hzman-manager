package com.cn.hzm.server.cache;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.cache.comparator.*;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.task.ItemRefreshTask;
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
import java.util.concurrent.*;
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

    @Autowired
    private ItemRefreshTask itemRefreshTask;

    private Map<Integer, Comparator<ItemDTO>> comparatorMap;

    @PostConstruct
    public void installCacheConfig() {

        comparatorMap = Maps.newHashMap();
        comparatorMap.put(ContextConst.ITEM_SORT_TODAY_DESC, new TodaySaleDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_TODAY_ASC, new TodaySaleAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_YESTERDAY_DESC, new YesterdaySaleDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_YESTERDAY_ASC, new YesterdaySaleAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_SALE_INVENTORY_DESC, new SaleInventoryDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_SALE_INVENTORY_ASC, new SaleInventoryAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_LOCAL_INVENTORY_DESC, new LocalInventoryDescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_LOCAL_INVENTORY_ASC, new LocalInventoryAscComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_30_DAY_DESC, new Sale30DescComparator());
        comparatorMap.put(ContextConst.ITEM_SORT_30_DAY_ASC, new Sale30AscComparator());

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
            t.setName("商品缓存线程");
            return t;
        });

        //启动程序计算智能补货
        smartReplenishmentTask.init();

        executor.submit(() -> {
            List<ItemDO> items = itemService.getListByCondition(Maps.newHashMap(), new String[]{"sku"});

            long startTime = System.currentTimeMillis();
            int threadNum = items.size() / 500 + (items.size() % 500 == 0 ? 0 : 1);
            log.info("商品详情缓存加载流程开始，需缓存个数：{} 开启线程数：{}", items.size(), threadNum);
            ExecutorService executorService = Executors.newFixedThreadPool(threadNum, r -> {
                Thread t = new Thread(r);
                t.setName("商品缓存子线程");
                return t;
            });
            CountDownLatch countDownLatch = new CountDownLatch(threadNum);

            for (int i = 0; i < threadNum; i++) {
                int beginIndex = i * 500;
                int endIndex = (i + 1) * 500;
                if (endIndex > items.size()) {
                    endIndex = items.size();
                }
                List<ItemDO> subItems = items.subList(beginIndex, endIndex);
                executorService.execute(()->{
                    subItems.forEach(itemDO -> cache.put(itemDO.getSku(), installItemDTO(itemDO.getSku())));
                    countDownLatch.countDown();
                });
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("商品详情缓存加载流程结束，耗时：{}", System.currentTimeMillis() - startTime);

            //本地商品缓存结束，开启商品刷新任务
            itemRefreshTask.init();
        });
    }

    /**
     * 根据排序获取缓存
     *
     * @param sortType
     * @return
     */
    public List<ItemDTO> getCacheBySort(Integer searchType, String key, Integer sortType) {

        Collection<ItemDTO> temp = cache.asMap().values();
        switch (searchType) {
            //sku 过滤
            case 1:
                if (!StringUtils.isEmpty(key)) {
                    temp = temp.stream().filter(item -> item.getSku().contains(key)).collect(Collectors.toList());
                }
                break;
            //title 过滤
            case 2:
                //关键字段为空，直接跳过筛选
                if (StringUtils.isEmpty(key)) {
                    break;
                }
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
            //订货过滤
            case 3:
                temp = getCache(smartReplenishmentTask.getOrderSkus());
                break;
            //发货过滤
            case 4:
                temp = getCache(smartReplenishmentTask.getShipSkus());
                break;
            default:
        }

        Comparator<ItemDTO> comparator = comparatorMap.getOrDefault(sortType, new TodaySaleDescComparator());
        Set<ItemDTO> list = Sets.newTreeSet(comparator);
        list.addAll(temp);
        return Lists.newArrayList(list);
    }

    /**
     * 根据sku获取缓存
     *
     * @param sku
     * @return
     */
    public ItemDTO getSingleCache(String sku) {
        return cache.get(sku);
    }

    /**
     * 删除缓存
     *
     * @param sku
     */
    public void deleteCache(String sku) {
        cache.invalidate(sku);
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
        ItemDTO itemDTO = null;
        try {
            ItemDO itemDO = itemService.getItemDOBySku(sku);
            if (itemDO == null) {
                return null;
            }
            itemDTO = itemDealService.buildItemDTO(itemDO);
        } catch (Exception e) {
            log.error("item缓存对象创建失败，sku:{} e:{}", sku, e.getMessage());
            e.printStackTrace();
        }
        return itemDTO;
    }
}
