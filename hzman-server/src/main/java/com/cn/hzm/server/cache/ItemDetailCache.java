package com.cn.hzm.server.cache;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.FactoryItemDO;
import com.cn.hzm.core.entity.FatherChildRelationDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.factory.service.FactoryItemService;
import com.cn.hzm.item.service.FatherChildRelationService;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.cache.comparator.*;
import com.cn.hzm.server.dto.InventoryDTO;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.dto.SaleInfoDTO;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.task.ItemRefreshTask;
import com.cn.hzm.server.task.SmartReplenishmentTask;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    private Map<String, ItemDTO> relationCacheMap;

    private Map<String, List<ItemDTO>> childrenCacheMap;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemDealService itemDealService;

    @Autowired
    private FatherChildRelationService fatherChildRelationService;

    @Autowired
    private FactoryItemService factoryItemService;

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
                .build(new CacheLoader<String, ItemDTO>() {
                    @Override
                    public @Nullable ItemDTO load(@NonNull String sku) {
                        Date usDate = TimeUtil.transformNowToUsDate();
                        return installItemDTO(sku, usDate);
                    }
                });

        relationCacheMap = Maps.newHashMap();
        childrenCacheMap = Maps.newHashMap();

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
            Date usDate = TimeUtil.transformNowToUsDate();
            for (int i = 0; i < threadNum; i++) {
                int beginIndex = i * 500;
                int endIndex = (i + 1) * 500;
                if (endIndex > items.size()) {
                    endIndex = items.size();
                }
                List<ItemDO> subItems = items.subList(beginIndex, endIndex);
                executorService.execute(() -> {
                    subItems.forEach(itemDO -> {
                        ItemDTO itemDTO = installItemDTO(itemDO.getSku(), usDate);
                        if (itemDTO != null) {
                            cache.put(itemDO.getSku(), itemDTO);
                        }
                    });
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

            //5分钟定时刷新父子关系
            ScheduledThreadPoolExecutor orderScheduledTask = new ScheduledThreadPoolExecutor(1);
            orderScheduledTask.scheduleWithFixedDelay(this::refreshRelationCache, 10, 300, TimeUnit.SECONDS);
        });


    }

    /**
     * 根据排序获取缓存
     *
     * @param sortType
     * @return
     */
    public List<ItemDTO> getCacheBySort(Integer showType, Integer itemStatusType, Integer factoryId,
                                        String key, String title, String itemType, Integer sortType) {
        Collection<ItemDTO> temp;
        if (showType == 2) {
            //展示父子体
            temp = relationCacheMap.values();

            //父子体asin过滤
            if (!StringUtils.isEmpty(key)) {
                temp = temp.stream().filter(item -> item.getAsin().contains(key)).collect(Collectors.toList());
            }

            //title过滤
            if (!StringUtils.isEmpty(title)) {
                String[] keys = key.split(" ");
                temp = temp.stream().filter(item -> {
                    for (String subKey : keys) {
                        if (!item.getTitle().toLowerCase().contains(subKey.toLowerCase())) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
            }
        } else {
            //展示全商品
            //dataType:0.全部商品，1.补货，2.订货
            if (itemStatusType == null || itemStatusType == 0) {
                temp = cache.asMap().values();
            } else if (itemStatusType == 1) {
                temp = getCache(smartReplenishmentTask.getShipSkus());
            } else {
                temp = getCache(smartReplenishmentTask.getOrderSkus());
            }

            //厂家过滤
            if (factoryId != null) {
                List<FactoryItemDO> factoryItemDOS = factoryItemService.getInfoByFactoryId(factoryId);
                List<String> ItemDTOs = factoryItemDOS.stream().map(FactoryItemDO::getSku).collect(Collectors.toList());
                temp = temp.stream().filter(itemDTO -> ItemDTOs.contains(itemDTO.getSku())).collect(Collectors.toList());
            }

            //sku过滤
            if (!StringUtils.isEmpty(key)) {
                temp = temp.stream().filter(item -> item.getSku().contains(key)).collect(Collectors.toList());
            }

            //title过滤
            if (!StringUtils.isEmpty(title)) {
                String[] keys = key.split(" ");
                temp = temp.stream().filter(item -> {
                    for (String subKey : keys) {
                        if (!item.getTitle().toLowerCase().contains(subKey.toLowerCase())) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            //商品类型过滤
            if (!StringUtils.isEmpty(itemType)) {
                temp = temp.stream().filter(item -> item.getItemType().equals(itemType)).collect(Collectors.toList());
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

    /**
     * 获取子体商品
     *
     * @param asin
     * @return
     */
    public List<ItemDTO> getChildrenCache(String asin) {
        return childrenCacheMap.get(asin);
    }

    public void refreshCaches(List<String> skus) {
        skus.forEach(sku -> cache.refresh(sku));
    }

    public void refreshCache(String sku) {
        cache.refresh(sku);
    }


    public ItemDTO installItemDTO(String sku, Date usDate) {
        ItemDTO itemDTO = null;
        try {
            ItemDO itemDO = itemService.getItemDOBySku(sku);
            if (itemDO == null) {
                return null;
            }
            itemDTO = itemDealService.buildItemDTO(itemDO, usDate);
        } catch (Exception e) {
            log.error("item缓存对象创建失败，sku:{} e:", sku, e);
        }
        return itemDTO;
    }

    //父类用asin做
    public ItemDTO installRelationItemDTO(String asin, Date usDate) {
        ItemDO fatherItem = itemService.getItemDOByAsin(asin, 1);
        ItemDTO relationItem = itemDealService.buildItemDTO(fatherItem, usDate);
        List<ItemDTO> childItems = getChildrenItem(asin);

        SaleInfoDTO today = new SaleInfoDTO();
        SaleInfoDTO yesterday = new SaleInfoDTO();
        SaleInfoDTO duration30Day = new SaleInfoDTO();
        SaleInfoDTO duration3060Day = new SaleInfoDTO();
        SaleInfoDTO setLastYearDuration30Day = new SaleInfoDTO();
        Integer amazonStockQuantity = 0;
        Integer localQuantity = 0;
        Integer amazonTransferQuantity = 0;
        Integer amazonInboundQuantity = 0;
        for (ItemDTO item : childItems) {
            addData(today, item.getToday());
            addData(yesterday, item.getYesterday());
            addData(duration30Day, item.getDuration30Day());
            addData(duration3060Day, item.getDuration3060Day());
            addData(setLastYearDuration30Day, item.getLastYearDuration30Day());

            amazonStockQuantity += item.getInventoryDTO().getAmazonStockQuantity();
            localQuantity += item.getInventoryDTO().getLocalQuantity();
            amazonTransferQuantity += item.getInventoryDTO().getAmazonTransferQuantity();
            amazonInboundQuantity += item.getInventoryDTO().getAmazonInboundQuantity();
        }
        relationItem.setToday(today);
        relationItem.setYesterday(yesterday);
        relationItem.setDuration30Day(duration30Day);
        relationItem.setDuration3060Day(duration3060Day);
        relationItem.setLastYearDuration30Day(setLastYearDuration30Day);
        relationItem.setChildrenNum(childItems.size());
        relationItem.setHaveChildren(!CollectionUtils.isEmpty(childItems));

        //库存信息合并
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setAmazonStockQuantity(amazonStockQuantity);
        inventoryDTO.setLocalQuantity(localQuantity);
        inventoryDTO.setAmazonTransferQuantity(amazonTransferQuantity);
        inventoryDTO.setAmazonInboundQuantity(amazonInboundQuantity);
        relationItem.setInventoryDTO(inventoryDTO);
        return relationItem;
    }

    //获取父类子体
    public List<ItemDTO> getChildrenItem(String asin) {
        List<FatherChildRelationDO> relations = fatherChildRelationService.getAllRelation(asin);
        List<ItemDTO> childItems = getCache(relations.stream().map(FatherChildRelationDO::getChildSku).collect(Collectors.toList()));
        childrenCacheMap.put(asin, childItems);
        return childItems;
    }

    private void refreshRelationCache() {
        List<FatherChildRelationDO> relations = fatherChildRelationService.getAllRelation(null);
        Map<String, List<FatherChildRelationDO>> relationMap = relations.stream().collect(Collectors.groupingBy(FatherChildRelationDO::getFatherAsin));

        Date usDate = TimeUtil.transformNowToUsDate();
        long startTime = System.currentTimeMillis();
        Map<String, ItemDTO> tmpMap = Maps.newHashMap();
        relationMap.keySet().forEach(fatherAsin -> {
            ItemDTO itemDTO = null;
            try {
                itemDTO = installRelationItemDTO(fatherAsin, usDate);
            } catch (Exception e) {
                log.error("{} 刷新父子关系缓存错误：", fatherAsin, e);
            }
            if (itemDTO != null) {
                tmpMap.put(fatherAsin, itemDTO);
                //relationCacheMap.put(fatherAsin, itemDTO);
            }
        });
        long endTime = System.currentTimeMillis();
        log.info("==================缓冲父子关系对象结构对：{} 耗时：{}", relationMap.size(), endTime - startTime);

        List<ItemDO> itemDOS = itemService.getItemByParentType(2, new String[]{"sku", "asin"});
        itemDOS.forEach(itemDO -> {
            ItemDTO relationItem = getSingleCache(itemDO.getSku());
            if (relationItem != null) {
                tmpMap.put(itemDO.getAsin(), relationItem);
                //relationCacheMap.put(itemDO.getAsin(), relationItem);
            }
        });
        startTime = System.currentTimeMillis();
        log.info("==================创建虚拟父体对象：{} 耗时：{}", itemDOS.size(), startTime - endTime);

        //全局替换
        relationCacheMap.clear();
        relationCacheMap.putAll(tmpMap);
    }

    private void addData(SaleInfoDTO saleInfo, SaleInfoDTO addSaleInfo) {
        Integer orderNum = saleInfo.getOrderNum() == null ? 0 : saleInfo.getOrderNum();
        Integer saleNum = saleInfo.getSaleNum() == null ? 0 : saleInfo.getSaleNum();
        Double saleVolume = saleInfo.getSaleVolume() == null ? 0.0 : saleInfo.getSaleVolume();

        saleInfo.setOrderNum(orderNum + addSaleInfo.getOrderNum());
        saleInfo.setSaleNum(saleNum + addSaleInfo.getSaleNum());
        saleInfo.setSaleVolume(saleVolume + addSaleInfo.getSaleVolume());
        if (saleInfo.getSaleNum() == 0) {
            saleInfo.setUnitPrice(saleInfo.getSaleVolume());
        } else {
            saleInfo.setUnitPrice(saleInfo.getSaleVolume() / (double) saleInfo.getSaleNum());
        }
    }
}
