package com.cn.hzm.core.misc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.*;
import com.cn.hzm.api.enums.FactoryOrderStatusEnum;
import com.cn.hzm.core.cache.ItemDetailCache;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.cache.comparator.SortHelper;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.manager.AwsUserManager;
import com.cn.hzm.core.manager.TaskManager;
import com.cn.hzm.core.processor.SmartReplenishmentProcessor;
import com.cn.hzm.core.repository.dao.*;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.spa.SpaManager;
import com.cn.hzm.core.spa.fbainventory.model.GetInventorySummariesResponse;
import com.cn.hzm.core.spa.item.model.*;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 5:55 下午
 */
@Slf4j
@Service
public class ItemService {

    @Autowired
    private AsinItemDao asinItemDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemInventoryDao inventoryDao;

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private AwsUserManager awsUserManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private ItemRemarkDao itemRemarkDao;

    @Autowired
    private FatherChildRelationDao fatherChildRelationDao;

    @Autowired
    private FactoryDao factoryDao;

    @Autowired
    private FactoryOrderItemDao factoryOrderItemDao;

    @Autowired
    private FactoryItemDao factoryItemDao;

    @Autowired
    private FactoryOrderDao factoryOrderDao;

    @Autowired
    private SaleInfoDao saleInfoDao;

    @Autowired
    private ItemCategoryDao itemCategoryDao;

    @Autowired
    private FbaInboundDao fbaInboundDao;

    @Autowired
    private FbaInboundItemDao fbaInboundItemDao;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private ItemDetailCache itemDetailCache;

    @Resource(name = "processItemInfoThreadExecutor")
    private ExecutorService executorService;

    @Autowired
    private SmartReplenishmentProcessor smartReplenishmentProcessor;

    private Map<String, Object> syncLock = Maps.newHashMap();

    public JSONObject processListItem(ItemConditionDto conditionDto) {
        List<ItemDto> itemRespList = itemDetailCache.getCacheBySort(conditionDto.getStatusType(),
                conditionDto.getFactoryId(), conditionDto.getKey(), conditionDto.getTitle(), conditionDto.getItemType(),
                conditionDto.getItemSortType(), conditionDto.getStartListingTime(), conditionDto.getEndListingTime(),
                conditionDto.getListingTimeSortType(), ThreadLocalCache.getUser().getUserMarketId());

        JSONObject respJo = new JSONObject();
        respJo.put("total", itemRespList.size());
        respJo.put("data", conditionDto.pageResult(itemRespList));
        return respJo;
    }

    public JSONObject processListItemV2(ItemConditionDto conditionDto) {
        String[] selectFields = new String[]{"id", "asin", "title"};
        List<AsinItemDo> asinItemDos;
        List<Integer> selectItemIds = Lists.newArrayList();
        switch (conditionDto.getStatusType()) {
            case 0:
            case 6:
                //全部商品
                asinItemDos = asinItemDao.selectAll(selectFields);
                break;
            case 1:
            case 2:
                //补货/订货商品
                Map<Integer, List<String>> userMarketIdSkus = smartReplenishmentProcessor.getSkusAll(conditionDto.getStatusType());
                Set<String> asins = Sets.newHashSet();
                userMarketIdSkus.forEach((userMarketId, skus) -> skus.forEach(sku -> {
                    ItemDo itemDo = itemDao.getItemDOBySku(userMarketId, sku);
                    if (itemDo != null) {
                        selectItemIds.add(itemDo.getId());
                        asins.add(itemDo.getAsin());
                    }
                }));
                asinItemDos = asins.size() == 0 ? Lists.newArrayList() : asinItemDao.getByAsins(asins, selectFields);
                break;
            case 3:
                //父体
                List<ItemDo> fatherItems = itemDao.getItemByParentType(1, new String[]{"user_market_id", "sku", "asin"});
                Set<String> fatherAsins = fatherItems.stream().map(ItemDo::getAsin).collect(Collectors.toSet());
                asinItemDos = fatherAsins.size() == 0 ? Lists.newArrayList() : asinItemDao.getByAsins(fatherAsins, selectFields);
                break;
            case 4:
                //备注商品
                List<ItemRemarkDo> remarkDos = itemRemarkDao.selectAll(new String[]{"item_id"});
                Set<Integer> itemIds = remarkDos.stream().map(ItemRemarkDo::getItemId).collect(Collectors.toSet());
                Set<String> remarkAsins = itemIds.stream()
                        .map(itemId -> {
                            selectItemIds.add(itemId);
                            ItemDo itemDo = itemDao.getById(itemId);
                            return itemDo == null ? null : itemDo.getAsin();
                        }).filter(asin -> !StringUtils.isEmpty(asin)).collect(Collectors.toSet());
                asinItemDos = remarkAsins.size() == 0 ? Lists.newArrayList() : asinItemDao.getByAsins(remarkAsins, selectFields);
                break;
            case 5:
                //未备注，找老板商量，直接去掉
//                temp = cache.asMap().values().stream().filter(itemDto -> itemDto.getUserMarketId().equals(userMarketId)).collect(Collectors.toList());
//                temp = temp.stream().filter(item -> CollectionUtils.isEmpty(item.getRemarkDtos())).collect(Collectors.toList());
//                break;
            case 7:
                List<ItemDo> itemDos = itemDao.getListByCondition(Maps.newHashMap(), new String[]{"id", "asin", "activity_info"});
                Set<String> transparencyPlanAsins = itemDos.stream()
                        .filter(itemDo -> itemDo.getActivityObj().getIsTransparencyPlan())
                        .map(item -> {
                            selectItemIds.add(item.getId());
                            return item.getAsin();
                        })
                        .collect(Collectors.toSet());
                asinItemDos = transparencyPlanAsins.size() == 0 ? Lists.newArrayList() : asinItemDao.getByAsins(transparencyPlanAsins, selectFields);
                break;
            default:
                asinItemDos = asinItemDao.selectAll(selectFields);
        }

        //asin过滤 or sku过滤
        if (!StringUtils.isEmpty(conditionDto.getKey())) {
            if (conditionDto.getStatusType().equals(6)) {
                asinItemDos = asinItemDos.stream()
                        .filter(item -> item.getAsin().contains(conditionDto.getKey()))
                        .collect(Collectors.toList());
            } else {
                List<ItemDo> skuItems = itemDao.fuzzyGetItemDOSBySku(conditionDto.getKey(), null);
                Set<String> selectAsins = Sets.newHashSet();
                skuItems.forEach(skuItem -> {
                    selectAsins.add(skuItem.getAsin());
                    selectItemIds.add(skuItem.getId());
                });
                asinItemDos = asinItemDos.stream()
                        .filter(asinItemDo -> selectAsins.contains(asinItemDo.getAsin()))
                        .collect(Collectors.toList());
            }

        }

        //title过滤
        if (!StringUtils.isEmpty(conditionDto.getTitle())) {
            String[] keys = conditionDto.getTitle().split(" ");
            asinItemDos = asinItemDos.stream().filter(item -> {
                for (String subKey : keys) {
                    if (!item.getTitle().toLowerCase().contains(subKey.toLowerCase())) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        //厂家过滤
        Integer factoryId = conditionDto.getFactoryId();
        if (factoryId != null && factoryId != 0) {
            List<FactoryItemDo> factoryItemDOS;
            if (factoryId == -1) {
                //拉取未绑定厂家商品
                factoryItemDOS = factoryItemDao.getAll();
            } else {
                //拉取指定厂家商品
                factoryItemDOS = factoryItemDao.getInfoByFactoryId(factoryId);
            }

            Set<String> selectAsins = Sets.newHashSet();
            factoryItemDOS.forEach(factoryItemDo -> {
                List<ItemDo> factoryItems = itemDao.getItemDOSBySku(factoryItemDo.getSku(), null);
                factoryItems.forEach(factoryItem -> {
                    selectAsins.add(factoryItem.getAsin());
                    selectItemIds.add(factoryItem.getId());
                });

            });
            asinItemDos = asinItemDos.stream()
                    .filter(asinItemDo -> (factoryId == -1) != selectAsins.contains(asinItemDo.getAsin()))
                    .collect(Collectors.toList());
        }

        //商品类型过滤
        if (!StringUtils.isEmpty(conditionDto.getItemType())) {
            asinItemDos = asinItemDos.stream()
                    .filter(item -> item.getItemType().equals(conditionDto.getItemType()))
                    .collect(Collectors.toList());
        }

        //上架时间过滤排序
        String startListingTime = conditionDto.getStartListingTime();
        if (!StringUtils.isEmpty(startListingTime)) {
            startListingTime += ":00";
        }
        String endListingTime = conditionDto.getEndListingTime();
        if (!StringUtils.isEmpty(endListingTime)) {
            endListingTime += ":00";
        }

        if (!StringUtils.isEmpty(startListingTime) || !StringUtils.isEmpty(endListingTime)) {
            List<ItemDo> listingTimeItems = itemDao.getItemDOSByListingTime(startListingTime, endListingTime, new String[]{"id", "asin", "title"});
            if (!CollectionUtils.isEmpty(listingTimeItems)) {
                Integer listingDateSortType = conditionDto.getListingTimeSortType();
                if (listingDateSortType == null || listingDateSortType == 0) {
                    //按时间从老到新
                    listingTimeItems = listingTimeItems.stream().sorted(Comparator.comparing(ItemDo::getListingTime))
                            .collect(Collectors.toList());
                } else {
                    //按时间从新到老
                    listingTimeItems = listingTimeItems.stream().sorted(Comparator.comparing(ItemDo::getListingTime).reversed())
                            .collect(Collectors.toList());
                }
                //上架时间过滤出的asin
                List<String> listingTimeAsins = listingTimeItems.stream().map(ItemDo::getAsin).collect(Collectors.toList());
                Map<String, AsinItemDo> map = asinItemDos.stream().collect(Collectors.toMap(AsinItemDo::getAsin, a -> a));
                asinItemDos = listingTimeAsins.stream()
                        .map(listingTimeAsin -> map.getOrDefault(listingTimeAsin, null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        }


        List<SaleInfoDo> saleInfoDos;
        Date usDate = TimeUtil.transformNowToUsDate();
        if (conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_TODAY_DESC)) {
            String strDate = TimeUtil.getSimpleFormat(usDate);
            saleInfoDos = saleInfoDao.getSaleInfoByDate(strDate);
        } else if (conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_YESTERDAY_DESC)) {
            usDate = TimeUtil.dateFixByDay(usDate, -1, 0, 0);
            String strDate = TimeUtil.getSimpleFormat(usDate);
            saleInfoDos = saleInfoDao.getSaleInfoByDate(strDate);
        } else if (conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_30_DAY_DESC)) {
            Date beginDate = TimeUtil.dateFixByDay(usDate, -30, 0, 0);
            String strEndDate = TimeUtil.getSimpleFormat(usDate);
            String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
            saleInfoDos = saleInfoDao.getSaleInfoByDurationDate(null, null, strBeginDate, strEndDate);
        } else {
            saleInfoDos = Lists.newArrayList();
        }


        Map<String, List<SaleInfoDo>> asinSaleInfoMap = Maps.newHashMap();
        saleInfoDos.forEach(saleInfoDo -> {
            List<ItemDo> itemDos = itemDao.getItemDOSBySku(saleInfoDo.getSku(), saleInfoDo.getUserMarketId());
            itemDos.forEach(itemDo -> {
                List<SaleInfoDo> tmpSaleInfos = asinSaleInfoMap.getOrDefault(itemDo.getAsin(), Lists.newArrayList());
                tmpSaleInfos.add(saleInfoDo);
                asinSaleInfoMap.put(itemDo.getAsin(), tmpSaleInfos);
            });
        });

        List<AsinItemDto> asinItemDtos = Lists.newArrayList();
        asinItemDos.forEach(asinItemDo -> {
            AsinItemDto asinItemDto = new AsinItemDto();
            asinItemDto.setId(asinItemDo.getId());
            asinItemDto.setAsin(asinItemDo.getAsin());

            List<SaleInfoDo> tmpSaleInfos = asinSaleInfoMap.get(asinItemDo.getAsin());

            int totalOrderNum = 0;
            int totalSaleNum = 0;
            double totalSaleVolume = 0.0;
            double totalTaxFee = 0.0;
            double totalFbaFulfillmentFee = 0.0;
            double totalCommission = 0.0;
            if (!CollectionUtils.isEmpty(tmpSaleInfos)) {
                for (SaleInfoDo saleInfo : tmpSaleInfos) {
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
            }

            double unitPrice = totalSaleVolume;
            if (totalSaleNum != 0) {
                unitPrice = totalSaleVolume / totalSaleNum;
            }

            SaleInfoDto saleInfoDTO = new SaleInfoDto();
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
            asinItemDto.setSortSaleInfo(saleInfoDTO);
            asinItemDtos.add(asinItemDto);
        });

        Set<AsinItemDto> sortSet = Sets.newTreeSet((o1, o2) -> {
            SaleInfoDto s1 = o1.getSortSaleInfo();
            SaleInfoDto s2 = o2.getSortSaleInfo();
            if (conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_TODAY_DESC)
                    || conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_YESTERDAY_DESC)
                    || conditionDto.getItemSortType().equals(ContextConst.ITEM_SORT_30_DAY_DESC)) {
                return SortHelper.compareEach(s2.getSaleNum(), s1.getSaleNum(), o2.getAsin(), o1.getAsin());
            }

            //暂时这么写
            return SortHelper.compareEach(s1.getSaleNum(), s2.getSaleNum(), o2.getAsin(), o1.getAsin());
        });
        sortSet.addAll(asinItemDtos);
        List<AsinItemDto> result = conditionDto.pageResult(Lists.newArrayList(sortSet));

        List<AsinItemDo> selectAsinItems = CollectionUtils.isEmpty(result) ? Lists.newArrayList() : asinItemDao.selectByIds(
                result.stream().map(AsinItemDto::getId).collect(Collectors.toList()));
        Map<Integer, AsinItemDo> selectMap = selectAsinItems.stream().collect(Collectors.toMap(AsinItemDo::getId, a -> a));

        //多线程组装返回结果
        List<Callable<Boolean>> dealCallables = Lists.newArrayList();
        int threadNum = result.size() / 5 + (result.size() % 5 != 0 ? 1 : 0);
        for (int i = 0; i < threadNum; i++) {
            int finalI = i;
            dealCallables.add(() -> {
                int begin = finalI * 5;
                int end = Math.min((finalI + 1) * 5, result.size());
                result.subList(begin, end).forEach(
                        asinItemDto -> {
                            AsinItemDo asinItemDo = selectMap.get(asinItemDto.getId());
                            asinItemDto.setIcon(asinItemDo.getIcon());
                            asinItemDto.setTitle(asinItemDo.getTitle());
                            asinItemDto.setLocalQuantity(asinItemDo.getLocalQuantity());

                            Date curDate = TimeUtil.transformNowToUsDate();
                            asinItemDto.setDimension(JSONObject.parseObject(asinItemDo.getPackageDimension(), PackageDimensionDto.class));
                            List<ItemDo> itemDos = itemDao.getItemDoByAsin(asinItemDto.getAsin());

                            //本地库存修改展示修改，有美国展示美国，没有美国展示第一个商品sku
                            String showSku = itemDos.get(0).getSku();
                            for(ItemDo itemDo : itemDos){
                                if(itemDo.getUserMarketId().equals(1)){
                                    showSku = itemDo.getSku();
                                    break;
                                }
                            }
                            asinItemDto.setShowSku(showSku);

                            asinItemDto.setSiteItems(itemDos.stream()
                                    .filter(itemDo -> CollectionUtils.isEmpty(selectItemIds) || selectItemIds.contains(itemDo.getId()))
                                    .map(itemDo -> buildItemDTO(itemDo, curDate))
                                    .collect(Collectors.toList()));

                            //构造子体数据
                            if (conditionDto.getStatusType().equals(3)) {
                                asinItemDto.getSiteItems().forEach(fatherItem -> {
                                    Integer itemNum = itemDetailCache.getChildrenItemNum(fatherItem.getUserMarketId(), fatherItem.getAsin());
                                    fatherItem.setChildrenNum(itemNum);
                                    fatherItem.setHaveChildren(itemNum != 0);
                                });
                            }

                            List<SaleInfoDto> calculateSaleInfos = asinItemDto.
                                    getSiteItems().stream().map(ItemDto::getToday).collect(Collectors.toList());
                            asinItemDto.setToday(statSaleInfo(calculateSaleInfos));
                            calculateSaleInfos = asinItemDto.
                                    getSiteItems().stream().map(ItemDto::getYesterday).collect(Collectors.toList());
                            asinItemDto.setYesterday(statSaleInfo(calculateSaleInfos));
                            calculateSaleInfos = asinItemDto.
                                    getSiteItems().stream().map(ItemDto::getDuration30Day).collect(Collectors.toList());
                            asinItemDto.setDuration30Day(statSaleInfo(calculateSaleInfos));
                            calculateSaleInfos = asinItemDto.
                                    getSiteItems().stream().map(ItemDto::getDuration3060Day).collect(Collectors.toList());
                            asinItemDto.setDuration3060Day(statSaleInfo(calculateSaleInfos));
                            calculateSaleInfos = asinItemDto.
                                    getSiteItems().stream().map(ItemDto::getLastYearDuration30Day).collect(Collectors.toList());
                            asinItemDto.setLastYearDuration30Day(statSaleInfo(calculateSaleInfos));
                        });
                return true;
            });
        }

        try {
            List<Future<Boolean>> returnFutures = executorService.invokeAll(dealCallables);
            for (Future<Boolean> booleanFuture : returnFutures) {
                boolean resultBool = booleanFuture.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject respJo = new JSONObject();
        respJo.put("total", sortSet.size());
        respJo.put("data", result);
        return respJo;

    }

    public Boolean setAsinInventoryLocal(Integer asinItemId, Integer curLocalNum){
        AsinItemDo asinItemDo = asinItemDao.selectById(asinItemId);
        asinItemDo.setLocalQuantity(curLocalNum);
        asinItemDao.updateItem(asinItemDo);
        return true;
    }

    private SaleInfoDto statSaleInfo(List<SaleInfoDto> calculateSaleInfos) {
        int totalOrderNum = 0;
        int totalSaleNum = 0;
        double totalSaleVolume = 0.0;
        double totalTaxFee = 0.0;
        double totalFbaFulfillmentFee = 0.0;
        double totalCommission = 0.0;
        if (!CollectionUtils.isEmpty(calculateSaleInfos)) {
            for (SaleInfoDto saleInfoDto : calculateSaleInfos) {
                totalOrderNum += saleInfoDto.getOrderNum();
                totalSaleNum += saleInfoDto.getSaleNum();
                totalSaleVolume += saleInfoDto.getSaleVolume();
                totalTaxFee += saleInfoDto.getSaleTax();
                totalFbaFulfillmentFee += saleInfoDto.getFbaFulfillmentFee();
                totalCommission += saleInfoDto.getCommission();
            }
        }

        double unitPrice = totalSaleVolume;
        if (totalSaleNum != 0) {
            unitPrice = totalSaleVolume / totalSaleNum;
        }

        SaleInfoDto saleInfoDTO = new SaleInfoDto();
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
        return saleInfoDTO;
    }

    private List<AsinItemDto> mockAsinList() {
        List<AsinItemDto> asinItemDtos = Lists.newArrayList();

        String iconUrl = "https://sellercentral.amazon.com/abis/listing/edit/offer?marketplaceID=ATVPDKIKX0DER&ref=xx_myiedit_cont_myifba&sku=S7312B-Round-T&asin=B09CD6726J&productType=NECKLACE#offer";

        List<String> asins = Lists.newArrayList(
                "B01I9XYKOG", "B0746BY7M2", "B07PFWC3FK", "B07C3M2JBB", "B07ZNL5MTR",
                "B07B1V2R2W", "B074DDZ472", "B078ZS6NMJ", "B079P4NKTC", "B07VHDJNPX");
        for (int i = 0; i < 10; i++) {
            AsinItemDto asinItemDto = new AsinItemDto();
            asinItemDto.setId(i);
            asinItemDto.setAsin(asins.get(i));
            asinItemDto.setIcon(iconUrl);
            asinItemDto.setTitle(String.format("mockTitle-%d", i));

            asinItemDto.setToday(mockSaleInfo(i));
            asinItemDto.setYesterday(mockSaleInfo(i));
            asinItemDto.setDuration30Day(mockSaleInfo(i));
            asinItemDto.setDuration3060Day(mockSaleInfo(i));
            asinItemDto.setLastYearDuration30Day(mockSaleInfo(i));

            asinItemDto.setLocalQuantity(i);

            PackageDimensionDto dto = new PackageDimensionDto();
            dto.setHeight("2.7");
            dto.setLength("2.8");
            dto.setWeight("2.9");
            dto.setWidth("3.0");
            asinItemDto.setDimension(dto);

            List<ItemDo> itemDos = itemDao.getItemDoByAsin(asinItemDto.getAsin());

            Date usDate = TimeUtil.transformNowToUsDate();
            asinItemDto.setSiteItems(itemDos.stream()
                    .map(itemDo -> {
                        ItemDto itemDto = buildItemDTO(itemDo, usDate);
                        Integer userMarketId = itemDto.getUserMarketId();
                        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(userMarketId);
                        String siteName = awsUserManager.getManager(awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId()).getSiteName();
                        itemDto.setSiteName(siteName);
                        return itemDto;
                    }).collect(Collectors.toList()));
            asinItemDtos.add(asinItemDto);
        }
        return asinItemDtos;
    }

    private SaleInfoDto mockSaleInfo(int i) {
        SaleInfoDto saleInfoDTO = new SaleInfoDto();
        saleInfoDTO.setUserMarketId(0);
        saleInfoDTO.setOrderNum(i);
        saleInfoDTO.setSaleNum(i);
        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(0.0D + i));
        saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(0.0D + i));
        saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(0.0D + i));
        saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(0.0D + i));
        saleInfoDTO.setUnitPrice(RandomUtil.saveDefaultDecimal(0.0D + i));
        return saleInfoDTO;
    }

    public List<ItemDto> getChildrenItem(Integer userMarketId, String asin) {
        return itemDetailCache.getChildrenCache(userMarketId, asin);
    }

    public List<String> getItemType() {
        List<ItemDo> itemDOS = itemDao.getItemType();
        return itemDOS.stream().map(ItemDo::getItemType)
                .filter(itemType -> !StringUtils.isEmpty(itemType)).collect(Collectors.toList());
    }

    //添加备注
    public Integer addRemark(AddItemRemarkDto remarkDto) {
        ItemDo itemDO = itemDao.getById(remarkDto.getItemId());
        if (itemDO == null) {
            throw new RuntimeException("商品不存在");
        }

        ItemRemarkDo itemRemarkDo = new ItemRemarkDo();
        itemRemarkDo.setItemId(remarkDto.getItemId());
        itemRemarkDo.setRemark(remarkDto.getRemark());
        itemRemarkDao.insert(itemRemarkDo);

        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        return itemRemarkDo.getId();
    }

    public boolean modRemark(AddItemRemarkDto remarkDto) {
        ItemDo itemDO = itemDao.getById(remarkDto.getItemId());
        if (itemDO == null) {
            throw new RuntimeException("商品不存在");
        }

        ItemRemarkDo itemRemarkDo = itemRemarkDao.selectById(remarkDto.getId());
        itemRemarkDo.setRemark(remarkDto.getRemark());
        itemRemarkDao.update(itemRemarkDo);

        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        return true;
    }

    public boolean delRemark(Integer id) {
        ItemRemarkDo itemRemarkDo = itemRemarkDao.selectById(id);
        if (itemRemarkDo == null) {
            return true;
        }

        ItemDo itemDO = itemDao.getById(itemRemarkDo.getItemId());
        if (itemDO == null) {
            return true;
        }

        itemRemarkDao.delete(id);
        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        return true;
    }

    public boolean modTransparencyPlan(ModItemTransparencyPlayDto dto) {
        //操作商品透明计划标
        dto.getItemIds().forEach(itemId -> {
            ItemDo itemDO = itemDao.getById(itemId);
            ItemDo.ItemActivityObj itemActivityObj = itemDO.getActivityObj();
            itemActivityObj.setIsTransparencyPlan(dto.getOperate().equals(1));
            itemDO.setActivityInfo(JSONObject.toJSONString(itemActivityObj));
            itemDao.updateItem(itemDO);
            itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        });
        return true;
    }

    //批量商品添加处理
    public void excelProcessSync(AddItemDeallDto dto, Integer awsUserId, String marketId) throws Exception {
        //先爬取商品相关信息
        processSync(dto.getSku(), awsUserId, marketId);

        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(awsUserId, marketId);
        //修改成本
        ItemDo itemDO = itemDao.getSingleItemDOByAsin(dto.getAsin(), dto.getSku(), awsUserMarketDo.getId());
        itemDO.setItemCost(dto.getCost());
        itemDao.updateItem(itemDO);

        //添加备注
        ItemRemarkDo itemRemarkDo = new ItemRemarkDo();
        itemRemarkDo.setItemId(itemDO.getId());
        itemRemarkDo.setRemark(dto.getRemark());
        itemRemarkDao.insert(itemRemarkDo);

        //关联厂家
        factoryService.factoryClaimItem(dto.getFactoryId(), dto.getSku(), "批量添加关联厂家");
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSync(String sourceSku, Integer awsUserId, String marketId) throws Exception {
        SpaManager spaManager = awsUserManager.getManager(awsUserId, marketId);

        List<String> skus;
        if (sourceSku.contains(";")) {
            skus = Lists.newArrayList(sourceSku.split(";"));
        } else {
            skus = Lists.newArrayList(sourceSku);
        }

        //asin取aws数据：商品信息
        for (String sku : skus) {
            Item item = spaManager.getItemBySku(sku);
            if (item == null) {
                log.info("商品【{}】aws请求为空，等待下次刷新", sku);
                return;
                //throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, "返回结果为空");
            }

            AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(awsUserId, marketId);
            ItemDo itemDo = ConvertUtil.convertToItemDo(new ItemDo(), item, sku, awsUserMarketDo);
            com.cn.hzm.core.spa.listings.model.Item listItem = spaManager.getListingsItem(sku);
            ConvertUtil.addListingTime(itemDo, listItem);
            //获取商品单价
            if (itemDo.getIsParent() != 1) {
                Double itemPrice = ConvertUtil.getItemPrice(spaManager.getPriceBySku(sku));
                itemDo.setItemPrice(itemPrice);
            } else {
                itemDo.setItemPrice(0.0);
            }

            ItemDo old = itemDao.getSingleItemDOByAsin(itemDo.getAsin(), itemDo.getSku(), awsUserMarketDo.getId());
            if (old != null) {
                itemDo.setId(old.getId());
                itemDao.updateItem(itemDo);
            } else {
                itemDao.createItem(itemDo);

                //创建asinItem
                if (asinItemDao.getByAsin(itemDo.getAsin()) == null) {
                    AsinItemDo asinItemDo = ConvertUtil.convertToItemDo(itemDo);
                    asinItemDao.createItem(asinItemDo);
                }
            }

            //刷新排名信息
            processSaleRankInfo(itemDo);

            //保存父子sku信息
            processRelationShip(itemDo, awsUserMarketDo, spaManager);

            //子类sku刷新库存信息
            if (itemDo.getIsParent() != 1) {
                dealSkuInventory(sku, awsUserId, marketId, "refresh", 0);
            }
        }
    }

    /**
     * @param asin
     */
    public void deleteItem(String asin, String sku) {
        //删除商品
        ItemDo old = itemDao.getSingleItemDOByAsin(asin, sku, ThreadLocalCache.getUser().getUserMarketId());
        if (old != null) {
            itemDao.deleteItem(old.getId());
        }

        //删除库存
        ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySkuAndAsin(sku, asin, ThreadLocalCache.getUser().getUserMarketId());
        if (inventoryDO != null) {
            inventoryDao.deleteInventory(inventoryDO.getId());
        }

        String fatherAsin = null;
        if (old != null) {
            if (old.getIsParent() == 1) {
                //删除父关系
                fatherChildRelationDao.deleteRelation(ThreadLocalCache.getUser().getUserMarketId(), old.getAsin(), old.getSku(), null, null);
                fatherAsin = old.getAsin();
            } else {
                //删除子关系
                List<FatherChildRelationDo> relationDos = fatherChildRelationDao.getAllRelationByChild(ThreadLocalCache.getUser().getUserMarketId(),
                        old.getSku(), old.getAsin());
                if (!CollectionUtils.isEmpty(relationDos)) {
                    fatherAsin = relationDos.get(0).getFatherAsin();
                }
                fatherChildRelationDao.deleteRelation(ThreadLocalCache.getUser().getUserMarketId(), null, null, old.getAsin(), old.getSku());
            }
        }

        //删除缓存
        itemDetailCache.deleteCache(ThreadLocalCache.getUser().getUserMarketId(), sku);

        //删除父子关系缓存
        if (!StringUtils.isEmpty(fatherAsin)) {
            itemDetailCache.refreshRelationCache(ThreadLocalCache.getUser().getUserMarketId(), fatherAsin, old.getIsParent() == 1);
        }
    }

    /**
     * @param sku
     */
    public void deleteItem(String sku) {
        //删除商品
        List<ItemDo> olds = itemDao.getItemDOSBySku(sku, ThreadLocalCache.getUser().getUserMarketId());
        if (!CollectionUtils.isEmpty(olds)) {
            olds.forEach(itemDO -> {
                itemDao.deleteItem(itemDO.getId());

                //删除库存
                ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySkuAndAsin(sku, itemDO.getAsin(), ThreadLocalCache.getUser().getUserMarketId());
                if (inventoryDO != null) {
                    inventoryDao.deleteInventory(inventoryDO.getId());
                }

                //删除缓存
                itemDetailCache.deleteCache(ThreadLocalCache.getUser().getUserMarketId(), sku);
            });
        }
    }


    public List<SimpleItemDto> fuzzyQuery(Integer searchType, String value) {
        String searchKey = "sku";
        switch (searchType) {
            case 1:
                searchKey = "sku";
                break;
            case 2:
                searchKey = "title";
                break;
            default:
        }

        List<ItemDo> list = itemDao.fuzzyQuery(searchKey, value, ThreadLocalCache.getUser().getUserMarketId());
        return list.stream().map(item -> JSONObject.parseObject(JSONObject.toJSONString(item), SimpleItemDto.class))
                .collect(Collectors.toList());
    }

    public boolean modLocalNum(String sku, Integer curLocalNum, Integer awsUserId, String marketId) {
        dealSkuInventory(sku, awsUserId, marketId, "set", curLocalNum);
        return true;
    }

    public ItemInventoryDo showAwsInventory(String sku, Integer awsUserId, String marketId) {
        SpaManager spaManager = awsUserManager.getManager(awsUserId, marketId);
        GetInventorySummariesResponse response = spaManager.getInventoryInfoBySku(sku);
        if (response == null) {
            throw new RuntimeException(String.format("商品【%s】库存aws请求为空，等待下次刷新", sku));
        }

        if (CollectionUtils.isEmpty(response.getPayload().getInventorySummaries())) {
            throw new RuntimeException(String.format("商品【%s】库存aws请求为空，等待下次刷新", sku));
        }

        //存在就更新
        ItemInventoryDo inventory = new ItemInventoryDo();
        ConvertUtil.convertToInventoryDO(response, inventory, spaManager.getAwsUserMarketId());
        return inventory;
    }

    public boolean modSkuCost(String asin, String sku, Double cost, Integer userMarketId) {
        ItemDo itemDO = itemDao.getSingleItemDOByAsin(asin, sku, userMarketId);
        itemDO.setItemCost(cost);
        itemDao.updateItem(itemDO);
        itemDetailCache.refreshCache(userMarketId, itemDO.getSku());
        return true;
    }

    public ItemDto buildItemDTO(ItemDo itemDO, Date usDate) {
        ItemDto itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDto.class);
        itemDTO.setCost(itemDO.getItemCost());

        //设置过滤时间
        if (!StringUtils.isEmpty(itemDTO.getListingTime())) {
            try {
                itemDTO.setDateListingTime(TimeUtil.transformMilliSecondUTCToDate(itemDTO.getListingTime()));
            } catch (ParseException ignored) {
            }
        } else {
            itemDTO.setDateListingTime(new Date());
            itemDTO.setListingTime(TimeUtil.dateToUTC(itemDTO.getDateListingTime()));
        }

        //设置尺寸
        JSONObject packageJo = JSONObject.parseObject(itemDO.getPackageDimension());
        if (packageJo != null && packageJo.containsKey("package")) {
            JSONObject targetJo = packageJo.getJSONObject("package");
            PackageDimensionDto dto = new PackageDimensionDto();
            if (targetJo.containsKey("height")) {
                dto.setHeight(targetJo.getJSONObject("height").getString("value"));
            }
            if (targetJo.containsKey("length")) {
                dto.setLength(targetJo.getJSONObject("length").getString("value"));
            }
            if (targetJo.containsKey("weight")) {
                dto.setWeight(targetJo.getJSONObject("weight").getString("value"));
            }
            if (targetJo.containsKey("width")) {
                dto.setWidth(targetJo.getJSONObject("width").getString("value"));
            }
            itemDTO.setDimension(dto);
        } else {
            itemDTO.setDimension(JSONObject.parseObject(itemDO.getPackageDimension(), PackageDimensionDto.class));
        }

        //设置类目排名&最小排名
        List<ItemCategoryDo> itemCategoryDOs = itemCategoryDao.getItemCategoryByItemId(itemDO.getId());
        if (!CollectionUtils.isEmpty(itemCategoryDOs)) {
            itemDTO.setCategoryRankDTOS(itemCategoryDOs.stream().map(itemCategoryDo -> JSONObject.parseObject(JSONObject.toJSONString(itemCategoryDo), CategoryRankDto.class)).collect(Collectors.toList()));
            itemDTO.setMaxRank(itemCategoryDOs.stream().map(ItemCategoryDo::getCategoryRank).min(Comparator.comparing(Integer::intValue)).get());
        } else {
            itemDTO.setMaxRank(-1);
        }

        ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySku(itemDO.getSku(), itemDO.getUserMarketId());
        InventoryDto inventoryDTO = JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDto.class);
        if (inventoryDTO == null) {
            inventoryDTO = new InventoryDto();
        }

        //组装预览数据
        if (inventoryDO != null && !StringUtils.isEmpty(inventoryDO.getReservedQuantity())) {
            InventoryReservedDetailDto reservedDetailDto = JSONObject.parseObject(inventoryDO.getReservedQuantity(), InventoryReservedDetailDto.class);
            inventoryDTO.setReservedDetailDto(reservedDetailDto);
            int totalReservedQuantity = 0;
            if (reservedDetailDto != null) {
                totalReservedQuantity = reservedDetailDto.getTotalReservedQuantity();
            }
            totalReservedQuantity = totalReservedQuantity + (inventoryDTO.getInboundWorkingQuantity() == null ? 0 : inventoryDTO.getInboundWorkingQuantity());
            inventoryDTO.setTotalReservedQuantity(totalReservedQuantity);
        }

        //不可售组装
        if (inventoryDO != null && !StringUtils.isEmpty(inventoryDO.getUnfulfillableQuantity())) {
            InventoryUnfulfillableDetailDto unfulfillableDetailDto = JSONObject.parseObject(inventoryDO.getUnfulfillableQuantity(), InventoryUnfulfillableDetailDto.class);
            inventoryDTO.setUnfulfillableDetailDto(unfulfillableDetailDto);
            inventoryDTO.setTotalUnfulfillableQuantity(unfulfillableDetailDto != null ? unfulfillableDetailDto.getTotalUnfulfillableQuantity() : 0);
        }


        List<FactoryOrderItemDo> factoryOrderItemDos = factoryOrderItemDao.getOrderBySku(itemDO.getSku());
        Map<Integer, FactoryOrderDo> map = Maps.newHashMap();
        List<FactoryQuantityDto> factoryQuantityDTOS = factoryOrderItemDos.stream().filter(orderItem -> {
            FactoryOrderDo order = factoryOrderDao.getOrderById(orderItem.getFactoryOrderId());
            map.put(order.getId(), order);
            return FactoryOrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode().equals(order.getOrderStatus())
                    || FactoryOrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode().equals(order.getOrderStatus());
        }).map(order -> {
            FactoryQuantityDto dto = new FactoryQuantityDto();
            dto.setNum(order.getOrderNum());
            dto.setDeliveryDate(map.get(order.getFactoryOrderId()).getDeliveryDate());
            return dto;
        }).collect(Collectors.toList());
        inventoryDTO.setFactoryQuantityInfos(factoryQuantityDTOS);

        inventoryDTO.setFactoryQuantity(Math.toIntExact(factoryQuantityDTOS.stream().map(FactoryQuantityDto::getNum).count()));
        inventoryDTO.setLocalTotalQuantity(inventoryDTO.getFactoryQuantity() + (inventoryDTO.getLocalQuantity() == null ? 0 : inventoryDTO.getLocalQuantity()));
        inventoryDTO.setTotalQuantity(inventoryDTO.getLocalTotalQuantity() + (inventoryDTO.getAmazonQuantity() == null ? 0 : inventoryDTO.getAmazonQuantity()));
        itemDTO.setInventoryDTO(inventoryDTO);

        //销量
        itemDTO.setToday(getSaleInfoByDate(usDate, itemDTO.getSku(), itemDTO.getUserMarketId()));
        itemDTO.setYesterday(getSaleInfoByDate(TimeUtil.dateFixByDay(usDate, -1, 0, 0), itemDTO.getSku(), itemDTO.getUserMarketId()));
        itemDTO.setDuration30Day(getSaleInfoByDurationDate(usDate, itemDTO.getSku(), itemDTO.getUserMarketId()));
        itemDTO.setDuration3060Day(getSaleInfoByDurationDate(TimeUtil.dateFixByDay(usDate, -30, 0, 0), itemDTO.getSku(), itemDTO.getUserMarketId()));
        itemDTO.setLastYearDuration30Day(getSaleInfoByDurationDate(TimeUtil.dateFixByYear(usDate, -1), itemDTO.getSku(), itemDTO.getUserMarketId()));

        //商品工厂归宿信息
        List<FactoryItemDo> factoryItemDOS = factoryItemDao.getInfoBySku(itemDTO.getSku());
        itemDTO.setFactoryItemDTOS(factoryItemDOS.stream().map(factoryItemDO -> {
            FactoryDo factoryDO = factoryDao.getByFid(factoryItemDO.getFactoryId());
            FactoryItemDto factoryItemDTO = new FactoryItemDto();
            factoryItemDTO.setId(factoryItemDO.getId());
            factoryItemDTO.setFactoryId(factoryDO.getId());
            factoryItemDTO.setFactoryName(factoryDO.getFactoryName());
            factoryItemDTO.setSku(factoryItemDO.getSku());
            factoryItemDTO.setFactoryPrice(factoryItemDO.getFactoryPrice());
            factoryItemDTO.setDesc(factoryItemDO.getItemDesc());
            return factoryItemDTO;
        }).collect(Collectors.toList()));

        //智能补货标
        SmartReplenishmentDto smart = smartReplenishmentProcessor.getSmartReplenishment(itemDO.getUserMarketId(), itemDO.getSku());
        itemDTO.setReplenishmentCode(smart == null ? 0 : smart.getReplenishmentCode());
        itemDTO.setReplenishmentNum(smart == null ? 0 : smart.getNeedNum().intValue());

        //添加备注列表
        List<ItemRemarkDo> remarkDos = itemRemarkDao.selectByItemId(itemDO.getId());
        itemDTO.setRemarkDtos(remarkDos.stream().map(remarkDo -> {
            ItemRemarkDto itemRemarkDto = new ItemRemarkDto();
            itemRemarkDto.setId(remarkDo.getId());
            itemRemarkDto.setItemId(remarkDo.getItemId());
            itemRemarkDto.setRemark(remarkDo.getRemark());
            itemRemarkDto.setCtime(TimeUtil.getSimpleFormat(remarkDo.getCtime()));
            itemRemarkDto.setUtime(TimeUtil.getSimpleFormat(remarkDo.getUtime()));
            return itemRemarkDto;
        }).collect(Collectors.toList()));

        AwsUserMarketDo userMarketDo = awsUserMarketDao.getById(itemDO.getUserMarketId());
        AwsMarket awsMarket = AwsMarket.getByMarketId(userMarketDo.getMarketId());
        //拼装站点名
        String siteName = awsUserManager.getManager(userMarketDo.getAwsUserId(), userMarketDo.getMarketId()).getSiteName();
        itemDTO.setSiteName(siteName);

        //拼装跳转链接
        Map<String, String> paramMap = Maps.newHashMap();
        paramMap.put("${ares}", awsMarket.getAres());
        paramMap.put("${asin}", itemDO.getAsin());
        paramMap.put("${sku}", itemDO.getSku());
        paramMap.put("${marketplaceId}", awsMarket.getId());
        paramMap.put("${productType}", itemDO.getItemType());

        String itemUrl = ContextConst.ITEM_URL;
        itemDTO.setItemUrl(replaceUrl(itemUrl, paramMap));

        String backgroundSkuUrl = ContextConst.BACKGROUND_SKU_URL;
        itemDTO.setBackgroundSkuUrl(replaceUrl(backgroundSkuUrl, paramMap));

        String backgroundFnskuUrl = awsMarket.getCountryCode().equals("UK") ? ContextConst.BACKGROUND_FNSKU_UK_URL : ContextConst.BACKGROUND_FNSKU_URL;
        itemDTO.setBackgroundFnskuUrl(replaceUrl(backgroundFnskuUrl, paramMap));

        //活动类型打标
        itemDTO.setIsTransparencyPlan(itemDO.getActivityObj().getIsTransparencyPlan());
        return itemDTO;
    }

    private String replaceUrl(String url, Map<String, String> paramMap) {
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (url.contains(entry.getKey())) {
                url = url.replace(entry.getKey(), entry.getValue());
            }
        }
        return url;
    }

    private SaleInfoDto getSaleInfoByDate(Date date, String sku, Integer userMarketId) {
        String statDate = TimeUtil.getSimpleFormat(date);
        SaleInfoDo saleInfoDO = saleInfoDao.getSaleInfoDOByDate(statDate, userMarketId, sku);

        SaleInfoDto saleInfoDTO = new SaleInfoDto();
        if (saleInfoDO == null) {
            saleInfoDTO.setSaleNum(0);
            saleInfoDTO.setOrderNum(0);
            saleInfoDTO.setSaleVolume(0.0);
            saleInfoDTO.setUnitPrice(0.0);
            saleInfoDTO.setSaleTax(0.0);
            saleInfoDTO.setFbaFulfillmentFee(0.0);
            saleInfoDTO.setCommission(0.0);
        } else {
            saleInfoDTO.setSaleNum(saleInfoDO.getSaleNum());
            saleInfoDTO.setOrderNum(saleInfoDO.getOrderNum());
            saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(saleInfoDO.getSaleVolume()));
            saleInfoDTO.setUnitPrice(RandomUtil.saveDefaultDecimal(saleInfoDO.getUnitPrice()));
            saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(saleInfoDO.getSaleTax()));
            saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(saleInfoDO.getFbaFulfillmentFee()));
            saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(saleInfoDO.getCommission()));
        }

        //计算净收入
        double income = saleInfoDTO.getSaleVolume() - saleInfoDTO.getSaleTax() - saleInfoDTO.getFbaFulfillmentFee() - saleInfoDTO.getCommission();
        saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
        return saleInfoDTO;
    }

    private SaleInfoDto getSaleInfoByDurationDate(Date date, String sku, Integer userMarketId) {
        Date beginDate = TimeUtil.dateFixByDay(date, -30, 0, 0);
        String strEndDate = TimeUtil.getSimpleFormat(date);
        String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
        List<SaleInfoDo> compareList = saleInfoDao.getSaleInfoByDurationDate(sku, userMarketId, strBeginDate, strEndDate);

        int saleNum = 0;
        int orderNum = 0;
        double saleVolume = 0.0;
        double taxFee = 0.0;
        double fbaFulfillmentFee = 0.0;
        double commission = 0.0;
        if (!CollectionUtils.isEmpty(compareList)) {
            for (SaleInfoDo saleInfo : compareList) {
                saleNum += saleInfo.getSaleNum();
                orderNum += saleInfo.getOrderNum();
                saleVolume += saleInfo.getSaleVolume();
                taxFee += saleInfo.getSaleTax();
                fbaFulfillmentFee += saleInfo.getFbaFulfillmentFee();
                commission += saleInfo.getCommission();
            }
        }
        SaleInfoDto saleInfoDTO = new SaleInfoDto();
        saleInfoDTO.setSaleNum(saleNum);
        saleInfoDTO.setOrderNum(orderNum);
        saleInfoDTO.setSaleVolume(RandomUtil.saveDefaultDecimal(saleVolume));
        if (saleNum == 0) {
            saleInfoDTO.setUnitPrice(0.0);
        } else {
            saleInfoDTO.setUnitPrice(saleVolume / (double) saleNum);
        }

        saleInfoDTO.setSaleTax(RandomUtil.saveDefaultDecimal(taxFee));
        saleInfoDTO.setFbaFulfillmentFee(RandomUtil.saveDefaultDecimal(fbaFulfillmentFee));
        saleInfoDTO.setCommission(RandomUtil.saveDefaultDecimal(commission));
        //计算净收入
        double income = saleVolume - taxFee - fbaFulfillmentFee - commission;
        saleInfoDTO.setIncome(RandomUtil.saveDefaultDecimal(income));
        return saleInfoDTO;
    }


    /**
     * 仓储统一操作，防止多线程操作引起数据错误
     *
     * @param sku
     * @param operateType
     * @param dealNum
     */
    public void dealSkuInventory(String sku, Integer awsUserId, String marketId, String operateType, Integer dealNum) {
        if (!syncLock.containsKey(sku)) {
            syncLock.put(sku, new Object());
        }

        //对sku进行同步操作
        synchronized (syncLock.get(sku)) {
            AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(awsUserId, marketId);
            ItemInventoryDo inventory = inventoryDao.getInventoryBySku(sku, awsUserMarketDo.getId());

            List<ItemDo> itemDos = itemDao.getItemDOSBySku(sku, awsUserMarketDo.getId());
            switch (operateType) {
                case "set":
                    if (inventory != null) {
                        inventory.setLocalQuantity(dealNum);
                        inventory.calculateTotalQuantity();
                        inventoryDao.updateInventory(inventory);

                        itemDos.forEach(itemDo -> {
                            AsinItemDo asinItemDo = asinItemDao.getByAsin(itemDo.getAsin());
                            if (asinItemDo != null) {
                                asinItemDo.setLocalQuantity(dealNum);
                                asinItemDao.updateItem(asinItemDo);
                            }
                        });
                    }
                    break;
                case "mod":
                    if (inventory != null) {
                        Integer localNum = inventory.getLocalQuantity() == null ? 0 : inventory.getLocalQuantity();
                        int nowLocal = localNum + dealNum;
                        inventory.setLocalQuantity(Math.max(nowLocal, 0));
                        inventory.calculateTotalQuantity();
                        inventoryDao.updateInventory(inventory);

                        itemDos.forEach(itemDo -> {
                            AsinItemDo asinItemDo = asinItemDao.getByAsin(itemDo.getAsin());
                            if (asinItemDo != null) {
                                asinItemDo.setLocalQuantity(Math.max(nowLocal, 0));
                                asinItemDao.updateItem(asinItemDo);
                            }
                        });
                    }
                    break;
                case "refresh":
                    SpaManager spaManager = awsUserManager.getManager(awsUserId, marketId);
                    GetInventorySummariesResponse response = spaManager.getInventoryInfoBySku(sku);
                    if (response == null) {
                        log.info("商品【{}】库存aws请求为空，等待下次刷新", sku);
                        return;
                    }

                    if (CollectionUtils.isEmpty(response.getPayload().getInventorySummaries())) {
                        log.info("商品【{}】库存aws请求为空，等待下次刷新", sku);
                        return;
                    }

                    //存在就更新
                    if (inventory == null) {
                        inventory = new ItemInventoryDo();
                        ConvertUtil.convertToInventoryDO(response, inventory, awsUserMarketDo.getId());
                        inventoryDao.createInventory(inventory);
                    } else {
                        ConvertUtil.convertToInventoryDO(response, inventory, awsUserMarketDo.getId());
                        inventoryDao.updateInventory(inventory);
                    }
                    break;
                default:
            }
            itemDetailCache.refreshCache(awsUserMarketDo.getId(), sku);

            //刷新asin 商品 本地库存
            itemDos.forEach(itemDo -> {
                AsinItemDo asinItemDo = asinItemDao.getByAsin(itemDo.getAsin());
                if (asinItemDo != null) {
                    List<ItemDo> tmpItemDos = itemDao.getItemDoByAsin(itemDo.getAsin());

                    int localInventory = 0;
                    for(ItemDo tmp: tmpItemDos){
                        ItemInventoryDo tmpInventory = inventoryDao.getInventoryBySku(tmp.getSku(), awsUserMarketDo.getId());
                        if(tmpInventory != null){
                            localInventory += tmpInventory.getLocalQuantity();
                        }
                    }
                    asinItemDo.setLocalQuantity(localInventory);
                    asinItemDao.updateItem(asinItemDo);
                }
            });
        }
    }

    public List<SmartReplenishmentDto> querySmartList() {
        return smartReplenishmentProcessor.getSmartReplenishmentDTOList(ThreadLocalCache.getUser().getUserMarketId(), null);
    }

    public String spiderShipment(Integer userMarketId, String shipmentId) {
        return taskManager.execTaskByRelationIds(userMarketId, SpiderType.SHIPMENT_INFO.getCode(), Lists.newArrayList(shipmentId));
    }

    public FbaInboundDto showShipmentItem(Integer userMarketId, String shipmentId, String sku) {
        FbaInboundDo fbaInboundDo = fbaInboundDao.getByShipmentId(userMarketId, shipmentId);
        List<FbaInboundItemDo> fbaInboundItemDos = fbaInboundItemDao.getAllRecordBySku(shipmentId, sku);

        FbaInboundDto fbaInboundDto = new FbaInboundDto();
        fbaInboundDto.setShipmentId(shipmentId);
        fbaInboundDto.setShipmentName(fbaInboundDo.getShipmentName());
        fbaInboundDto.setShipmentStatus(fbaInboundDo.getShipmentStatus());
        fbaInboundDto.setFbaInboundItemDtos(fbaInboundItemDos.stream().map(fbaInboundItemDo -> {
            FbaInboundItemDto fbaInboundItemDto = new FbaInboundItemDto();
            fbaInboundItemDto.setSellerSKU(fbaInboundItemDo.getSellerSKU());
            fbaInboundItemDto.setFulfillmentNetworkSKU(fbaInboundItemDo.getFulfillmentNetworkSKU());
            fbaInboundItemDto.setQuantityReceived(fbaInboundItemDo.getQuantityReceived());
            fbaInboundItemDto.setQuantityShipped(fbaInboundItemDo.getQuantityShipped());
            fbaInboundItemDto.setQuantityInCase(fbaInboundItemDo.getQuantityInCase());
            return fbaInboundItemDto;
        }).collect(Collectors.toList()));

        return fbaInboundDto;
    }

    public String fnskuQuery(String fnsku) {
        ItemInventoryDo inventoryDO = inventoryDao.getInventoryByFnsku(fnsku, ThreadLocalCache.getUser().getUserMarketId());
        if (inventoryDO != null) {
            return inventoryDO.getSku();
        }
        return null;
    }

    //处理类目排名
    public void processSaleRankInfo(ItemDo itemDO) {
        if (StringUtils.isEmpty(itemDO.getSaleRank().trim())) {
            return;
        }

        ItemSalesRanksByMarketplace salesRankings = JSONArray.parseObject(itemDO.getSaleRank(), ItemSalesRanksByMarketplace.class);
        if (CollectionUtils.isEmpty(salesRankings.getClassificationRanks())) {
            return;
        }

        List<ItemCategoryDo> categoryDos = Lists.newArrayList();
        for (ItemClassificationSalesRank classificationSalesRank : salesRankings.getClassificationRanks()) {
            ItemCategoryDo categoryDo = new ItemCategoryDo();
            categoryDo.setItemId(itemDO.getId());
            categoryDo.setRelationInfo(classificationSalesRank.getClassificationId());
            categoryDo.setCategoryTitle(classificationSalesRank.getTitle());
            categoryDo.setCategoryLink(classificationSalesRank.getLink());
            categoryDo.setCategoryRank(classificationSalesRank.getRank());
            categoryDos.add(categoryDo);
        }

        //删除老排序关系
        itemCategoryDao.deleteItemCategoryByItemId(itemDO.getId());

        //添加新关系
        if (!CollectionUtils.isEmpty(categoryDos)) {
            categoryDos.forEach(categoryDo -> itemCategoryDao.createItemCategory(categoryDo));
        }
    }

    public void processRelationShip(ItemDo itemDo, AwsUserMarketDo awsUserMarketDo, SpaManager spaManager) {
        ItemRelationshipsByMarketplace relationships = JSONObject.parseObject(itemDo.getRelationship(), ItemRelationshipsByMarketplace.class);
        if (itemDo.getIsParent() == 0) {
            String fatherAsin = relationships.getRelationships().get(0).getParentAsins().get(0);
            ItemDo fatherItem = itemDao.getItemDOByAsin(fatherAsin, 1, awsUserMarketDo.getId());
            if (fatherItem == null) {
                Item item = spaManager.getItemByAsin(fatherAsin);
                if (item == null) {
                    return;
                }
                fatherItem = ConvertUtil.convertToItemDo(new ItemDo(), item, null, awsUserMarketDo);
                fatherItem.setItemPrice(0.0);
                fatherItem.setSku(StringUtils.isEmpty(fatherItem.getSku()) ? "" : fatherItem.getSku());
                itemDao.createItem(fatherItem);

                if (asinItemDao.getByAsin(fatherItem.getAsin()) == null) {
                    AsinItemDo asinItemDo = ConvertUtil.convertToItemDo(fatherItem);
                    asinItemDao.createItem(asinItemDo);
                }

                log.info("创建父sku商品：{}", fatherItem.getAsin());
            }

            //创建对应关系
            FatherChildRelationDo relationDO = fatherChildRelationDao.getRelationByFatherAndChildAsin(awsUserMarketDo.getId(), fatherItem.getAsin(), itemDo.getAsin());
            if (relationDO == null) {
                relationDO = new FatherChildRelationDo();
                relationDO.setFatherSku(fatherItem.getSku());
                relationDO.setFatherAsin(fatherItem.getAsin());
                relationDO.setChildSku(itemDo.getSku());
                relationDO.setChildAsin(itemDo.getAsin());
                relationDO.setUserMarketId(awsUserMarketDo.getId());
                fatherChildRelationDao.createRelation(relationDO);
            }
        } else if (itemDo.getIsParent() == 1) {
            List<String> childAsins = relationships.getRelationships().get(0).getChildAsins();
            childAsins.forEach(childAsin -> {
                ItemDo childItem = itemDao.getItemDOByAsin(childAsin, 0, awsUserMarketDo.getId());
                if (childItem == null) {
                    Item fatherResp = spaManager.getItemByAsin(childAsin);
                    if (fatherResp == null) {
                        return;
                    }
                    childItem = ConvertUtil.convertToItemDo(new ItemDo(), fatherResp, null, awsUserMarketDo);

                    //获取商品价格
                    Double itemPrice = ConvertUtil.getItemPrice(spaManager.getPriceBySku(childItem.getSku()));
                    childItem.setItemPrice(itemPrice);

                    childItem.setActive(1);
                    itemDao.createItem(childItem);

                    if (asinItemDao.getByAsin(childItem.getAsin()) == null) {
                        AsinItemDo asinItemDo = ConvertUtil.convertToItemDo(childItem);
                        asinItemDao.createItem(asinItemDo);
                    }

                    dealSkuInventory(childItem.getSku(), awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId(), "refresh", 0);
                }

                //创建对应关系
                FatherChildRelationDo relationDO = fatherChildRelationDao.getRelationByFatherAndChildAsin(awsUserMarketDo.getId(), itemDo.getAsin(), childItem.getAsin());
                if (relationDO == null) {
                    relationDO = new FatherChildRelationDo();
                    relationDO.setFatherSku(itemDo.getSku());
                    relationDO.setFatherAsin(itemDo.getAsin());
                    relationDO.setChildSku(childItem.getSku());
                    relationDO.setChildAsin(childItem.getAsin());
                    relationDO.setUserMarketId(awsUserMarketDo.getId());
                    fatherChildRelationDao.createRelation(relationDO);
                }
            });
        } else {
            log.info("本sku：{} 即没有子体也没有父体", itemDo.getSku());
        }
    }

    /**
     * 本地库存商品文档下载
     *
     * @param response
     */
    public void stockItemDownload(HttpServletResponse response) {
        List<String> rowNameList = Lists.newArrayList("sku", "库存数量");
        List<String> rowFiledList = Lists.newArrayList("sku", "localNum");
        String sheetName = "本地库存";

        List<ItemInventoryDo> inventoryDos = inventoryDao.getInventoryWhenStockNotNull();
        List<List<String>> values = inventoryDos.stream()
                .map(itemInventoryDo -> Lists.newArrayList(itemInventoryDo.getSku(), String.valueOf(itemInventoryDo.getLocalQuantity())))
                .collect(Collectors.toList());
        commonDeal(response, sheetName, rowNameList, rowFiledList, values);
    }

    /**
     * 未添加成本商品文档下载
     *
     * @param response
     */
    public void costItemDownload(HttpServletResponse response) {
        List<String> rowNameList = Lists.newArrayList("asin", "sku", "成本");
        List<String> rowFiledList = Lists.newArrayList("asin", "sku", "cost");
        String sheetName = "成本";

        List<ItemDo> itemDos = itemDao.getUnCostItemDOS(ThreadLocalCache.getUser().getUserMarketId());
        List<List<String>> values = itemDos.stream().map(itemDo -> Lists.newArrayList(itemDo.getAsin(), itemDo.getSku(), "0")).collect(Collectors.toList());
        commonDeal(response, sheetName, rowNameList, rowFiledList, values);
    }

    private void commonDeal(HttpServletResponse response, String sheetName,
                            List<String> rowNameList, List<String> rowFiledList, List<List<String>> values) {
        //创建一个工作蒲
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet1");
        sheet.setDefaultColumnWidth(19);

        //全局样式
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);//居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//上下居中

        //标题
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < rowNameList.size(); i++) {
            row.setHeight((short) 450);
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(rowNameList.get(i));
            cell.setCellStyle(cellStyle);
        }

        XSSFRow fieldRow = sheet.createRow(1);
        for (int i = 0; i < rowFiledList.size(); i++) {
            fieldRow.setHeight((short) 450);
            XSSFCell cell = fieldRow.createCell(i);
            cell.setCellValue(rowFiledList.get(i));
            cell.setCellStyle(cellStyle);
        }


        int valueRow = 2;
        for (List<String> defaultValue : values) {
            XSSFRow defaultValueRow = sheet.createRow(valueRow);
            for (int i = 0; i < defaultValue.size(); i++) {
                defaultValueRow.setHeight((short) 450);
                XSSFCell cell = defaultValueRow.createCell(i);
                cell.setCellValue(defaultValue.get(i));
                cell.setCellStyle(cellStyle);
            }
            valueRow++;
        }

        //数据输出流
        try {
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-Disposition",
                    "attachment;filename=" +
                            new String((sheetName + ".xlsx").getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("下载模版文件失败：", e);
            throw new HzmException(ExceptionCode.TEMPLATE_EXCEL_DOWNLOAD_ERROR);
        }
    }
}
