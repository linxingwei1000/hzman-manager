package com.cn.hzm.core.cache;

import com.cn.hzm.api.dto.InventoryDto;
import com.cn.hzm.api.dto.ItemDto;
import com.cn.hzm.api.dto.SaleInfoDto;
import com.cn.hzm.core.cache.comparator.*;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.repository.dao.FactoryItemDao;
import com.cn.hzm.core.repository.entity.FactoryItemDo;
import com.cn.hzm.core.processor.ItemRefreshProcessor;
import com.cn.hzm.core.processor.SmartReplenishmentProcessor;
import com.cn.hzm.core.repository.dao.FatherChildRelationDao;
import com.cn.hzm.core.repository.dao.ItemDao;
import com.cn.hzm.core.repository.entity.FatherChildRelationDo;
import com.cn.hzm.core.repository.entity.ItemDo;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
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
import org.springframework.beans.factory.annotation.Value;
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

    //<sku|userMarketId, item>
    private LoadingCache<String, ItemDto> cache;

    private Map<String, ItemDto> relationCacheMap;

    private Map<String, List<ItemDto>> childrenCacheMap;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private FatherChildRelationDao fatherChildRelationDao;

    @Autowired
    private FactoryItemDao factoryItemDao;

    @Autowired
    private SmartReplenishmentProcessor smartReplenishmentProcessor;

    @Autowired
    private ItemRefreshProcessor itemRefreshProcessor;

    @Value("${cache.switch:false}")
    private Boolean cacheSwitch;

    private Map<Integer, Comparator<ItemDto>> comparatorMap;

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
                .build(new CacheLoader<String, ItemDto>() {
                    @Override
                    public @Nullable ItemDto load(@NonNull String key) {
                        String[] params = key.split("\\|");
                        Date usDate = TimeUtil.transformNowToUsDate();
                        return installItemDTO(Integer.parseInt(params[1]), params[0], usDate);
                    }
                });

        relationCacheMap = Maps.newHashMap();
        childrenCacheMap = Maps.newHashMap();

        //启动程序计算智能补货
        smartReplenishmentProcessor.init();

        //异步加载数据
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("商品缓存线程");
            return t;
        });

        executor.submit(() -> {
            if (!cacheSwitch) {
                log.info("开发环境关闭商品缓存任务");
                return;
            }

            List<ItemDo> items = itemDao.getListByCondition(Maps.newHashMap(), new String[]{"sku", "user_market_id"});

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
                List<ItemDo> subItems = items.subList(beginIndex, endIndex);
                executorService.execute(() -> {
                    subItems.forEach(itemDO -> {
                        ItemDto itemDTO = installItemDTO(itemDO.getUserMarketId(), itemDO.getSku(), usDate);
                        if (itemDTO != null) {
                            cache.put(installCacheKey(itemDO.getUserMarketId(), itemDO.getSku()), itemDTO);
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
            itemRefreshProcessor.init();

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
    public List<ItemDto> getCacheBySort(Integer statusType, Integer factoryId,
                                        String key, String title, String itemType, Integer sortType,
                                        String startListingTime, String endListingTime, Integer listingDateSortType, Integer userMarketId) {
        Collection<ItemDto> temp;

        switch (statusType) {
            case 0:
                //全部商品
                temp = cache.asMap().values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());
                break;
            case 1:
                //补货商品
                temp = getCache(userMarketId, smartReplenishmentProcessor.getShipSkus(userMarketId));
                break;
            case 2:
                //订货商品
                temp = getCache(userMarketId, smartReplenishmentProcessor.getOrderSkus(userMarketId));
                break;
            case 3:
                //父体
                temp = relationCacheMap.values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());;
                break;
            case 4:
                //备注商品
                temp = cache.asMap().values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());
                temp = temp.stream().filter(item -> !CollectionUtils.isEmpty(item.getRemarkDtos())).collect(Collectors.toList());
                break;
            case 5:
                temp = cache.asMap().values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());
                temp = temp.stream().filter(item -> CollectionUtils.isEmpty(item.getRemarkDtos())).collect(Collectors.toList());
                break;
            default:
                temp = cache.asMap().values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());
        }


        //sku过滤
        if (!StringUtils.isEmpty(key)) {
            if(statusType.equals(3)){
                temp = temp.stream().filter(item -> item.getAsin().contains(key)).collect(Collectors.toList());
            }else{
                temp = temp.stream().filter(item -> item.getSku().contains(key)).collect(Collectors.toList());
            }

        }

        //title过滤
        if (!StringUtils.isEmpty(title)) {
            String[] keys = title.split(" ");
            temp = temp.stream().filter(item -> {
                for (String subKey : keys) {
                    if (!item.getTitle().toLowerCase().contains(subKey.toLowerCase())) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        //厂家过滤
        if (factoryId != null && factoryId != 0) {
            if (factoryId == -1) {
                //拉取未绑定厂家商品
                List<FactoryItemDo> factoryItemDOS = factoryItemDao.getAll();
                List<String> ItemDTOs = factoryItemDOS.stream().map(FactoryItemDo::getSku).collect(Collectors.toList());
                temp = temp.stream().filter(itemDTO -> !ItemDTOs.contains(itemDTO.getSku())).collect(Collectors.toList());
            } else {
                //拉取指定厂家商品
                List<FactoryItemDo> factoryItemDOS = factoryItemDao.getInfoByFactoryId(factoryId);
                List<String> ItemDTOs = factoryItemDOS.stream().map(FactoryItemDo::getSku).collect(Collectors.toList());
                temp = temp.stream().filter(itemDTO -> ItemDTOs.contains(itemDTO.getSku())).collect(Collectors.toList());
            }
        }


        //商品类型过滤
        if (!StringUtils.isEmpty(itemType)) {
            temp = temp.stream().filter(item -> item.getItemType().equals(itemType)).collect(Collectors.toList());
        }

        //上架时间过滤排序
        if (StringUtils.isNotEmpty(startListingTime)) {
            startListingTime += ":00";
            Date startLT = TimeUtil.getDateByDateFormat(startListingTime);
            temp = temp.stream().filter(itemDto -> itemDto.getDateListingTime().after(startLT)).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(endListingTime)) {
            endListingTime += ":00";
            Date endLT = TimeUtil.getDateByDateFormat(endListingTime);
            temp = temp.stream().filter(itemDto -> itemDto.getDateListingTime().before(endLT)).collect(Collectors.toList());
        }
        if (listingDateSortType == null || listingDateSortType == 0) {
            //按时间从老到新
            temp = temp.stream().sorted(Comparator.comparing(ItemDto::getDateListingTime))
                    .collect(Collectors.toList());
        } else {
            //按时间从新到老
            temp = temp.stream().sorted(Comparator.comparing(ItemDto::getDateListingTime).reversed())
                    .collect(Collectors.toList());
        }

        Comparator<ItemDto> comparator = comparatorMap.getOrDefault(sortType, new TodaySaleDescComparator());
        Set<ItemDto> list = Sets.newTreeSet(comparator);
        list.addAll(temp);
        return Lists.newArrayList(list);
    }

    /**
     * 根据sku获取缓存
     *
     * @param sku
     * @return
     */
    public ItemDto getSingleCache(Integer awsUserMarketId, String sku) {
        return cache.get(installCacheKey(awsUserMarketId, sku));
    }

    /**
     * 删除缓存
     *
     * @param sku
     */
    public void deleteCache(Integer awsUserMarketId, String sku) {
        cache.invalidate(installCacheKey(awsUserMarketId, sku));
    }

    /**
     * 根据sku获取缓存
     *
     * @param skus
     * @return
     */
    public List<ItemDto> getCache(Integer userMarketId, List<String> skus) {
        if(CollectionUtils.isEmpty(skus)){
            return Lists.newArrayList();
        }
        List<String> keys = skus.stream().map(sku -> installCacheKey(userMarketId, sku)).collect(Collectors.toList());
        Map<String, ItemDto> cacheMap = cache.getAll(keys);
        return Lists.newArrayList(cacheMap.values());
    }

    /**
     * 获取子体商品
     *
     * @param asin
     * @return
     */
    public List<ItemDto> getChildrenCache(Integer userMarketId, String asin) {
        return childrenCacheMap.get(installCacheKey(userMarketId, asin));
    }

    public void refreshCaches(Integer userMarketId, List<String> skus) {
        skus.forEach(sku -> cache.refresh(installCacheKey(userMarketId, sku)));
    }

    public void refreshCache(Integer userMarketId, String sku) {
        cache.refresh(installCacheKey(userMarketId, sku));
    }


    public ItemDto installItemDTO(Integer userMarketId, String sku, Date usDate) {
        ItemDto itemDTO = null;
        try {
            ItemDo itemDO = itemDao.getItemDOBySku(userMarketId, sku);
            if (itemDO == null) {
                return null;
            }
            itemDTO = itemService.buildItemDTO(itemDO, usDate);
        } catch (Exception e) {
            log.error("item缓存对象创建失败，sku:{} e:", sku, e);
        }
        return itemDTO;
    }

    //父类用asin做
    public ItemDto installRelationItemDTO(Integer usrMarketId, String asin, Date usDate) {
        ItemDo fatherItem = itemDao.getItemDOByAsin(asin, 1, usrMarketId);
        ItemDto relationItem = itemService.buildItemDTO(fatherItem, usDate);
        List<ItemDto> childItems = getChildrenItem(usrMarketId, asin);

        SaleInfoDto today = new SaleInfoDto();
        SaleInfoDto yesterday = new SaleInfoDto();
        SaleInfoDto duration30Day = new SaleInfoDto();
        SaleInfoDto duration3060Day = new SaleInfoDto();
        SaleInfoDto setLastYearDuration30Day = new SaleInfoDto();
        Integer fulfillableQuantity = 0;
        Integer localQuantity = 0;
        Integer inboundShippedQuantity = 0;
        for (ItemDto item : childItems) {
            addData(today, item.getToday());
            addData(yesterday, item.getYesterday());
            addData(duration30Day, item.getDuration30Day());
            addData(duration3060Day, item.getDuration3060Day());
            addData(setLastYearDuration30Day, item.getLastYearDuration30Day());

            fulfillableQuantity += item.getInventoryDTO().getFulfillableQuantity() == null ? 0 : item.getInventoryDTO().getFulfillableQuantity();
            localQuantity += item.getInventoryDTO().getLocalQuantity() == null ? 0 : item.getInventoryDTO().getLocalQuantity();
            inboundShippedQuantity += item.getInventoryDTO().getInboundShippedQuantity() == null ? 0 : item.getInventoryDTO().getInboundShippedQuantity();
        }
        relationItem.setToday(today);
        relationItem.setYesterday(yesterday);
        relationItem.setDuration30Day(duration30Day);
        relationItem.setDuration3060Day(duration3060Day);
        relationItem.setLastYearDuration30Day(setLastYearDuration30Day);
        relationItem.setChildrenNum(childItems.size());
        relationItem.setHaveChildren(!CollectionUtils.isEmpty(childItems));

        //库存信息合并
        InventoryDto inventoryDTO = new InventoryDto();
        inventoryDTO.setFulfillableQuantity(fulfillableQuantity);
        inventoryDTO.setLocalQuantity(localQuantity);
        inventoryDTO.setInboundShippedQuantity(inboundShippedQuantity);
        relationItem.setInventoryDTO(inventoryDTO);
        return relationItem;
    }

    //获取父类子体
    public List<ItemDto> getChildrenItem(Integer userMarketId, String asin) {
        List<FatherChildRelationDo> relations = fatherChildRelationDao.getAllRelation(userMarketId, asin);
        List<ItemDto> childItems = getCache(userMarketId, relations.stream().map(FatherChildRelationDo::getChildSku).collect(Collectors.toList()));
        childrenCacheMap.put(installCacheKey(userMarketId, asin), childItems);
        return childItems;
    }

    private void refreshRelationCache() {
        List<FatherChildRelationDo> relations = fatherChildRelationDao.getAllRelation(null, null);
        Map<String, List<FatherChildRelationDo>> relationMap = relations.stream().collect(Collectors.groupingBy(FatherChildRelationDo::getFatherAsin));

        Date usDate = TimeUtil.transformNowToUsDate();
        long startTime = System.currentTimeMillis();
        Map<String, ItemDto> tmpMap = Maps.newHashMap();
        relationMap.forEach((fatherAsin, relationDos) -> {
            Integer usrMarketId = relationDos.get(0).getUserMarketId();
            ItemDto itemDTO = null;
            try {
                itemDTO = installRelationItemDTO(usrMarketId, fatherAsin, usDate);
            } catch (Exception e) {
                log.error("{} 刷新父子关系缓存错误：", fatherAsin, e);
            }
            if (itemDTO != null) {
                tmpMap.put(installCacheKey(itemDTO.getUserMarketId(), fatherAsin), itemDTO);
            }
        });
        long endTime = System.currentTimeMillis();
        log.info("==================缓冲父子关系对象结构对：{} 耗时：{}", relationMap.size(), endTime - startTime);

        List<ItemDo> itemDOS = itemDao.getItemByParentType(2, new String[]{"user_market_id", "sku", "asin"});
        itemDOS.forEach(itemDO -> {
            ItemDto relationItem = getSingleCache(itemDO.getUserMarketId(), itemDO.getSku());
            if (relationItem != null) {
                tmpMap.put(installCacheKey(itemDO.getUserMarketId(), itemDO.getAsin()), relationItem);
            }
        });
        startTime = System.currentTimeMillis();
        log.info("==================创建虚拟父体对象：{} 耗时：{}", itemDOS.size(), startTime - endTime);

        //全局替换
        relationCacheMap.clear();
        relationCacheMap.putAll(tmpMap);
    }

    public void refreshRelationCache(Integer awsUserMarketId, String fatherAsin) {
        ItemDto itemDTO = null;
        try {
            itemDTO = installRelationItemDTO(awsUserMarketId, fatherAsin, TimeUtil.transformNowToUsDate());
        } catch (Exception e) {
            log.error("{} 刷新父子关系缓存错误：", fatherAsin, e);
        }
        if (itemDTO != null) {
            relationCacheMap.put(installCacheKey(itemDTO.getUserMarketId(), fatherAsin), itemDTO);
        }
    }

    private void addData(SaleInfoDto saleInfo, SaleInfoDto addSaleInfo) {
        Integer orderNum = saleInfo.getOrderNum() == null ? 0 : saleInfo.getOrderNum();
        Integer saleNum = saleInfo.getSaleNum() == null ? 0 : saleInfo.getSaleNum();
        Double saleVolume = saleInfo.getSaleVolume() == null ? 0.0 : saleInfo.getSaleVolume();
        Double taxFee = saleInfo.getSaleTax() == null ? 0.0 : saleInfo.getSaleTax();
        Double fbaFulfillmentFee = saleInfo.getFbaFulfillmentFee() == null ? 0.0 : saleInfo.getFbaFulfillmentFee();
        Double commission = saleInfo.getCommission() == null ? 0.0 : saleInfo.getCommission();

        saleInfo.setOrderNum(orderNum + addSaleInfo.getOrderNum());
        saleInfo.setSaleNum(saleNum + addSaleInfo.getSaleNum());
        saleInfo.setSaleVolume(RandomUtil.saveDefaultDecimal(saleVolume + addSaleInfo.getSaleVolume()));
        if (saleInfo.getSaleNum() == 0) {
            saleInfo.setUnitPrice(saleInfo.getSaleVolume());
        } else {
            saleInfo.setUnitPrice(saleInfo.getSaleVolume() / (double) saleInfo.getSaleNum());
        }

        saleInfo.setSaleTax(RandomUtil.saveDefaultDecimal(taxFee + addSaleInfo.getSaleTax()));
        saleInfo.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(fbaFulfillmentFee + addSaleInfo.getFbaFulfillmentFee()));
        saleInfo.setCommission(RandomUtil.saveDefaultDecimal(commission + addSaleInfo.getCommission()));

        //计算净收入
        double income = saleVolume - taxFee - fbaFulfillmentFee - commission;
        saleInfo.setIncome(RandomUtil.saveDefaultDecimal(income));
    }

    private String installCacheKey(Integer userMarketId, String sku) {
        return sku + "|" + userMarketId;
    }
}
