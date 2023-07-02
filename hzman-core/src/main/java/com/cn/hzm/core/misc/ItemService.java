package com.cn.hzm.core.misc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.*;
import com.cn.hzm.api.enums.FactoryOrderStatusEnum;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.product.*;
import com.cn.hzm.core.aws.resp.product.GetProductCategoriesForSKUResponse;
import com.cn.hzm.core.cache.ItemDetailCache;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.enums.AmazonShipmentStatusEnum;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.core.manager.AwsUserManager;
import com.cn.hzm.core.manager.TaskManager;
import com.cn.hzm.core.processor.SmartReplenishmentProcessor;
import com.cn.hzm.core.repository.dao.*;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.spa.SpaManager;
import com.cn.hzm.core.spa.fbainventory.model.GetInventorySummariesResponse;
import com.cn.hzm.core.spa.item.model.Item;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 5:55 下午
 */
@Slf4j
@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemInventoryDao inventoryDao;

    @Autowired
    private AwsSpiderTaskDao awsSpiderTaskDao;

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

    @Autowired
    private SmartReplenishmentProcessor smartReplenishmentProcessor;

    @Autowired
    private AwsClient awsClient;

    private Map<String, Object> syncLock = Maps.newHashMap();

    public JSONObject processListItem(ItemConditionDto conditionDto) {
        List<ItemDto> itemRespList = itemDetailCache.getCacheBySort(conditionDto.getShowType(), conditionDto.getItemStatusType(),
                conditionDto.getFactoryId(), conditionDto.getKey(), conditionDto.getTitle(), conditionDto.getItemType(),
                conditionDto.getItemSortType(), ThreadLocalCache.getUser().getUserMarketId());

        //todo 上架时间

        JSONObject respJo = new JSONObject();
        respJo.put("total", itemRespList.size());
        respJo.put("data", conditionDto.pageResult(itemRespList));
        return respJo;
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
    public boolean addRemark(AddItemRemarkDto remarkDto) {
        ItemDo itemDO = itemDao.getById(remarkDto.getItemId());
        if (itemDO == null) {
            throw new RuntimeException("商品不存在");
        }

        ItemRemarkDo itemRemarkDo = new ItemRemarkDo();
        itemRemarkDo.setItemId(remarkDto.getItemId());
        itemRemarkDo.setRemark(remarkDto.getRemark());
        itemRemarkDao.insert(itemRemarkDo);

        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        return true;
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

    //批量商品添加处理
    public void excelProcessSync(AddItemDeallDto dto) throws Exception {
        //先爬取商品相关信息
        processSync(dto.getSku(), ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId());

        //修改成本
        ItemDo itemDO = itemDao.getSingleItemDOByAsin(dto.getAsin(), dto.getSku(), ThreadLocalCache.getUser().getUserMarketId());
        itemDO.setItemCost(dto.getCost());
        itemDO.setItemRemark(dto.getRemark());
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
    public void processSync(String sku, Integer awsUserId, String marketId) throws Exception {
        SpaManager spaManager = awsUserManager.getManager(awsUserId, marketId);
        //asin取aws数据：商品信息
        Item item = spaManager.getItemBySku(sku);
        if (item == null) {
            log.info("商品【{}】aws请求为空，等待下次刷新", sku);
            return;
            //throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, "返回结果为空");
        }

        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(awsUserId, marketId);
        ItemDo itemDo = ConvertUtil.convertToItemDo(new ItemDo(), item, sku, awsUserMarketDo);

        //获取商品单价
        if (itemDo.getIsParent() != 1) {
            Double itemPrice = ConvertUtil.getItemPrice(spaManager.getPriceBySku(sku));
            itemDo.setItemPrice(itemPrice);
        } else {
            itemDo.setItemPrice(0.0);
        }
        itemDo.setActive(1);
        itemDo.setItemCost(0.0);

        ItemDo old = itemDao.getSingleItemDOByAsin(itemDo.getAsin(), itemDo.getSku(), awsUserMarketDo.getId());
        if (old != null) {
            itemDo.setId(old.getId());
            itemDao.updateItem(itemDo);
        } else {
            itemDao.createItem(itemDo);
        }

        //刷新排名信息
        //processSaleRankInfo(itemDO);

        //保存父子sku信息
        processRelationShip(itemDo, awsUserMarketDo, spaManager);

        //子类sku刷新库存信息
        if (itemDo.getIsParent() != 1) {
            dealSkuInventory(sku, awsUserId, marketId, "refresh", 0);
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
        ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySkuAndAsin(sku, asin);
        if (inventoryDO != null) {
            inventoryDao.deleteInventory(inventoryDO.getId());
        }

        //删除缓存
        itemDetailCache.deleteCache(ThreadLocalCache.getUser().getUserMarketId(), sku);
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
                ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySkuAndAsin(sku, itemDO.getAsin());
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

    public boolean modLocalNum(String sku, Integer curLocalNum) {
        dealSkuInventory(sku, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId(), "set", curLocalNum);
        return true;
    }

    public boolean modSkuCost(String asin, String sku, Double cost) {
        ItemDo itemDO = itemDao.getSingleItemDOByAsin(asin, sku, ThreadLocalCache.getUser().getUserMarketId());
        itemDO.setItemCost(cost);
        itemDao.updateItem(itemDO);
        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), itemDO.getSku());
        return true;
    }

    public ItemDto buildItemDTO(ItemDo itemDO, Date usDate) {
        ItemDto itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDto.class);
        itemDTO.setCost(itemDO.getItemCost());

        //设置尺寸
        itemDTO.setDimension(JSONObject.parseObject(itemDO.getPackageDimension(), PackageDimensionDto.class));

        //设置类目排名
        ItemCategoryDo itemCategoryDO = itemCategoryDao.getItemCategoryByItemId(itemDO.getId());
        if (itemCategoryDO != null) {
            itemDTO.setCategoryRankDTOS(JSONArray.parseArray(itemCategoryDO.getCategoryTreeInfo(), CategoryRankDto.class));
        }

        //设置最小排名
        if (!StringUtils.isEmpty(itemDO.getSaleRank().trim())) {
            SalesRankings salesRankings = JSONArray.parseObject(itemDO.getSaleRank(), SalesRankings.class);
            if (salesRankings.getSalesRanks() != null) {
                Optional<Integer> maxOptional = salesRankings.getSalesRanks().stream().map(SalesRank::getRank).min(Comparator.comparing(Integer::intValue));
                itemDTO.setMaxRank(maxOptional.get());
            }
        }

        ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySku(itemDO.getSku(), itemDO.getUserMarketId());
        InventoryDto inventoryDTO = JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDto.class);
        if (inventoryDTO == null) {
            inventoryDTO = new InventoryDto();
        }

        //计算入库中的数量
        inventoryDTO.setAmazonInboundQuantity(getInBoundNum(itemDO.getSku()));

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
            BeanUtils.copyProperties(remarkDo, itemRemarkDto);
            return itemRemarkDto;
        }).collect(Collectors.toList()));

        return itemDTO;
    }

    private Integer getInBoundNum(String sku) {
        List<FbaInboundItemDo> records = fbaInboundItemDao.getAllRecordBySku(sku);
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }

        List<String> shipmentIds = records.stream().map(FbaInboundItemDo::getShipmentId).collect(Collectors.toList());
        List<FbaInboundDo> infoRecords = fbaInboundDao.getAllRecordByShipmentIds(shipmentIds);
        Map<String, String> useMap = infoRecords.stream().filter(infoRecord -> !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CLOSED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CANCELLED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_DELETED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_ERROR.getCode()))
                .collect(Collectors.toMap(FbaInboundDo::getShipmentId, FbaInboundDo::getShipmentStatus));


        if (CollectionUtils.isEmpty(useMap)) {
            return 0;
        }

        int num = 0;
        for (FbaInboundItemDo record : records) {
            if (useMap.containsKey(record.getShipmentId())) {
                String shipmentStatus = useMap.get(record.getShipmentId());
                if (shipmentStatus.equals(AmazonShipmentStatusEnum.STATUS_RECEIVING.getCode())) {
                    int quantityShipped = record.getQuantityShipped() != null ? record.getQuantityShipped() : 0;
                    int quantityReceived = record.getQuantityReceived() != null ? record.getQuantityReceived() : 0;
                    int remain = quantityShipped - quantityReceived;
                    if (remain < 0) {
                        remain = 0;
                    }
                    num += remain;
                } else {
                    if (record.getQuantityShipped() != null) {
                        num += record.getQuantityShipped();
                    }
                }
            }
        }
        return num;
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
            switch (operateType) {
                case "set":
                    inventory.setLocalQuantity(dealNum);
                    inventory.calculateTotalQuantity();
                    inventoryDao.updateInventory(inventory);
                    break;
                case "mod":
                    Integer localNum = inventory.getLocalQuantity() == null ? 0 : inventory.getLocalQuantity();
                    int nowLocal = localNum + dealNum;
                    inventory.setLocalQuantity(Math.max(nowLocal, 0));
                    inventory.calculateTotalQuantity();
                    inventoryDao.updateInventory(inventory);
                    break;
                case "refresh":
                    SpaManager spaManager = awsUserManager.getManager(awsUserId, marketId);
                    GetInventorySummariesResponse response = spaManager.getInventoryInfoBySku(sku);
                    if (response == null) {
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
        }
    }

    public List<SmartReplenishmentDto> querySmartList() {
        return smartReplenishmentProcessor.getSmartReplenishmentDTOList(ThreadLocalCache.getUser().getUserMarketId(), null);
    }

    public String spiderShipment(Integer userMarketId, String shipmentId) {
        return taskManager.execTaskByRelationIds(userMarketId, SpiderType.SHIPMENT_INFO.getCode(), Lists.newArrayList(shipmentId));
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

        SalesRankings salesRankings = JSONArray.parseObject(itemDO.getSaleRank(), SalesRankings.class);
        if (salesRankings.getSalesRanks() == null) {
            return;
        }

        GetProductCategoriesForSKUResponse response = awsClient.getProductCategoriesForSku(itemDO.getSku());
        if (response != null) {

            Map<String, Integer> rankMap = Maps.newHashMap();
            salesRankings.getSalesRanks().forEach(salesRank -> rankMap.put(salesRank.getProductCategoryId(), salesRank.getRank()));

            List<CategoryParent> categoryParents = response.getGetProductCategoriesForSKUResult().getCategoryParents();

            //未获取类目信息，停止刷新类目信息
            if (categoryParents == null) {
                return;
            }

            Map<String, CategoryRankDto> categoryMap = Maps.newHashMap();
            for (CategoryParent categoryParent : categoryParents) {
                dealParentCategory(categoryParent, rankMap, categoryMap);
            }
            String categoryTreeInfo = JSONObject.toJSONString(categoryMap.values());

            ItemCategoryDo old = itemCategoryDao.getItemCategoryByItemId(itemDO.getId());
            if (old == null) {
                old = new ItemCategoryDo();
                old.setItemId(itemDO.getId());
                old.setCategoryTreeInfo(categoryTreeInfo);
                itemCategoryDao.createItemCategory(old);
            } else {
                old.setCategoryTreeInfo(categoryTreeInfo);
                itemCategoryDao.updateItemCategory(old);
            }
        }
    }

    private CategoryRankDto dealParentCategory(CategoryParent categoryParent, Map<String, Integer> rankMap, Map<String, CategoryRankDto> categoryMap) {
        if (categoryParent.getCategoryParent() != null) {
            CategoryRankDto categoryRankDTO = dealParentCategory(categoryParent.getCategoryParent(), rankMap, categoryMap);

            Map<String, CategoryRankDto> children = categoryRankDTO.getChildCategoryMap();
            if (children == null) {
                children = Maps.newHashMap();
                categoryRankDTO.setChildCategory(Lists.newArrayList());
            }

            CategoryRankDto childCategory = children.get(categoryParent.getProductCategoryId());
            if (childCategory == null) {
                //组装子类目
                childCategory = new CategoryRankDto();
                childCategory.setProductCategoryId(categoryParent.getProductCategoryId());
                childCategory.setProductCategoryName(categoryParent.getProductCategoryName());
                if (rankMap.containsKey(categoryParent.getProductCategoryId())) {
                    childCategory.setRank(rankMap.get(categoryParent.getProductCategoryId()));
                }

                children.put(categoryParent.getProductCategoryId(), childCategory);
                categoryRankDTO.getChildCategory().add(childCategory);
                categoryRankDTO.setChildCategoryMap(children);
            }
            return childCategory;
        }

        //保留父类类目
        CategoryRankDto categoryRankDTO = categoryMap.get(categoryParent.getProductCategoryId());
        if (categoryRankDTO == null) {
            categoryRankDTO = new CategoryRankDto();
            categoryMap.put(categoryParent.getProductCategoryId(), categoryRankDTO);
        }
        categoryRankDTO.setProductCategoryId(categoryParent.getProductCategoryId());
        categoryRankDTO.setProductCategoryName(categoryParent.getProductCategoryName());
        if (rankMap.containsKey(categoryParent.getProductCategoryId())) {
            categoryRankDTO.setRank(rankMap.get(categoryParent.getProductCategoryId()));
        }
        return categoryRankDTO;
    }

    public void processRelationShip(ItemDo itemDo, AwsUserMarketDo awsUserMarketDo, SpaManager spaManager) {
        Relationships relationships = JSONObject.parseObject(itemDo.getRelationship(), Relationships.class);
        if (itemDo.getIsParent() == 0) {
            String fatherAsin = relationships.getVariationParent().getIdentifiers().getMarketplaceASIN().getAsin();
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
                fatherChildRelationDao.createRelation(relationDO);
            }
        } else if (itemDo.getIsParent() == 1) {
            List<String> childAsins = relationships.getVariationChildrens()
                    .stream().map(child -> child.getIdentifiers().getMarketplaceASIN().getAsin())
                    .collect(Collectors.toList());
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
                    fatherChildRelationDao.createRelation(relationDO);
                }
            });
        } else {
            log.info("本sku：{} 即没有子体也没有父体", itemDo.getSku());
        }
    }
}
