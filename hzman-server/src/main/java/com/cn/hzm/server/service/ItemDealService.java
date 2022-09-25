package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.product.*;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.entity.*;
import com.cn.hzm.core.enums.AmazonShipmentStatusEnum;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.factory.enums.OrderStatusEnum;
import com.cn.hzm.factory.service.FactoryItemService;
import com.cn.hzm.factory.service.FactoryOrderItemService;
import com.cn.hzm.factory.service.FactoryOrderService;
import com.cn.hzm.factory.service.FactoryService;
import com.cn.hzm.item.service.FatherChildRelationService;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.task.ShipmentSpiderTask;
import com.cn.hzm.server.task.SmartReplenishmentTask;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.cn.hzm.stock.service.ShipmentInfoRecordService;
import com.cn.hzm.stock.service.ShipmentItemRecordService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 5:55 下午
 */
@Slf4j
@Service
public class ItemDealService {

    @Autowired
    private AwsClient awsClient;

    @Autowired
    private ItemService itemService;

    @Autowired
    private FatherChildRelationService fatherChildRelationService;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private FactoryOrderService factoryOrderService;

    @Autowired
    private FactoryOrderItemService factoryOrderItemService;

    @Autowired
    private FactoryItemService factoryItemService;

    @Autowired
    private SaleInfoService saleInfoService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShipmentInfoRecordService shipmentInfoRecordService;

    @Autowired
    private ShipmentItemRecordService shipmentItemRecordService;

    @Autowired
    private ItemDetailCache itemDetailCache;

    @Autowired
    private ShipmentSpiderTask shipmentSpiderTask;

    @Autowired
    private SmartReplenishmentTask smartReplenishmentTask;

    private Map<String, Object> syncLock = Maps.newHashMap();

    public JSONObject processListItem(ItemConditionDTO conditionDTO) {
        List<ItemDTO> itemRespList = itemDetailCache.getCacheBySort(conditionDTO.getSearchType(), conditionDTO.getKey(),
                conditionDTO.getItemSortType(), conditionDTO.getShowType());

        JSONObject respJo = new JSONObject();
        respJo.put("total", itemRespList.size());
        respJo.put("data", conditionDTO.pageResult(itemRespList));
        return respJo;
    }

    public List<ItemDTO> getChildrenItem(String asin) {
        return itemDetailCache.getChildrenCache(asin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSync(String sku) {

        //asin取aws数据：商品信息
        GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin("SellerSKU", sku);
        if (resp == null) {
            log.info("商品【{}】aws请求为空，等待下次刷新", sku);
            return;
            //throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, "返回结果为空");
        }

        //判断错误
        if (resp.getGetMatchingProductForIdResult().getError() != null) {
            ProductError error = resp.getGetMatchingProductForIdResult().getError();
            throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, error.getMessage());
        }

        ItemDO itemDO = ConvertUtil.convertToItemDO(new ItemDO(), resp, sku);

        //获取商品单价
        if (itemDO.getIsParent() != 1) {
            Double itemPrice = ConvertUtil.getItemPrice(awsClient.getMyPriceForSku(sku));
            itemDO.setItemPrice(itemPrice);
        } else {
            itemDO.setItemPrice(0.0);
        }
        itemDO.setActive(1);

        ItemDO old = itemService.getSingleItemDOByAsin(itemDO.getAsin(), itemDO.getSku());
        if (old != null) {
            itemDO.setId(old.getId());
            itemService.updateItem(itemDO);
        } else {
            itemService.createItem(itemDO);
        }

        //保存父子sku信息
        processRelationShip(itemDO);

        //子类sku刷新库存信息
        if (itemDO.getIsParent() != 1) {
            dealSkuInventory(sku, "refresh", 0);
        }

    }

    /**
     * @param asin
     */
    public void deleteItem(String asin, String sku) {
        //删除商品
        ItemDO old = itemService.getSingleItemDOByAsin(asin, sku);
        if (old != null) {
            itemService.deleteItem(old.getId());
        }

        //删除库存
        InventoryDO inventoryDO = inventoryService.getInventoryBySkuAndAsin(sku, asin);
        if (inventoryDO != null) {
            inventoryService.deleteInventory(inventoryDO.getId());
        }

        //删除缓存
        itemDetailCache.deleteCache(sku);
    }

    /**
     * @param sku
     */
    public void deleteItem(String sku) {
        //删除商品
        List<ItemDO> olds = itemService.getItemDOSBySku(sku);
        if (!CollectionUtils.isEmpty(olds)) {
            olds.forEach(itemDO -> {
                itemService.deleteItem(itemDO.getId());

                //删除库存
                InventoryDO inventoryDO = inventoryService.getInventoryBySkuAndAsin(sku, itemDO.getAsin());
                if (inventoryDO != null) {
                    inventoryService.deleteInventory(inventoryDO.getId());
                }

                //删除缓存
                itemDetailCache.deleteCache(sku);
            });
        }
    }


    public List<SimpleItemDTO> fuzzyQuery(Integer searchType, String value) {
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

        List<ItemDO> list = itemService.fuzzyQuery(searchKey, value);
        return list.stream().map(item -> JSONObject.parseObject(JSONObject.toJSONString(item), SimpleItemDTO.class))
                .collect(Collectors.toList());
    }

    public boolean modLocalNum(String sku, Integer curLocalNum) {
        dealSkuInventory(sku, "set", curLocalNum);
        return true;
    }

    public ItemDTO buildItemDTO(ItemDO itemDO, Date usDate) {
        ItemDTO itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDTO.class);
        InventoryDO inventoryDO = inventoryService.getInventoryBySku(itemDO.getSku());
        InventoryDTO inventoryDTO = JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDTO.class);
        if (inventoryDTO == null) {
            inventoryDTO = new InventoryDTO();
        }

        //计算入库中的数量
        inventoryDTO.setAmazonInboundQuantity(getInBoundNum(itemDO.getSku()));

        List<FactoryOrderItemDO> factoryOrderItemDOS = factoryOrderItemService.getOrderBySku(itemDO.getSku());
        Map<Integer, FactoryOrderDO> map = Maps.newHashMap();
        List<FactoryQuantityDTO> factoryQuantityDTOS = factoryOrderItemDOS.stream().filter(orderItem -> {
            FactoryOrderDO order = factoryOrderService.getOrderById(orderItem.getFactoryOrderId());
            map.put(order.getId(), order);
            return OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode().equals(order.getOrderStatus())
                    || OrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode().equals(order.getOrderStatus());
        }).map(order -> {
            FactoryQuantityDTO dto = new FactoryQuantityDTO();
            dto.setNum(order.getOrderNum());
            dto.setDeliveryDate(map.get(order.getFactoryOrderId()).getDeliveryDate());
            return dto;
        }).collect(Collectors.toList());
        inventoryDTO.setFactoryQuantityInfos(factoryQuantityDTOS);

        inventoryDTO.setFactoryQuantity(Math.toIntExact(factoryQuantityDTOS.stream().map(FactoryQuantityDTO::getNum).count()));
        inventoryDTO.setLocalTotalQuantity(inventoryDTO.getFactoryQuantity() + (inventoryDTO.getLocalQuantity() == null ? 0 : inventoryDTO.getLocalQuantity()));
        inventoryDTO.setTotalQuantity(inventoryDTO.getLocalTotalQuantity() + (inventoryDTO.getAmazonQuantity() == null ? 0 : inventoryDTO.getAmazonQuantity()));
        itemDTO.setInventoryDTO(inventoryDTO);

        //销量
        itemDTO.setToday(getSaleInfoByDate(usDate, itemDTO.getSku()));
        itemDTO.setYesterday(getSaleInfoByDate(TimeUtil.dateFixByDay(usDate, -1, 0, 0), itemDTO.getSku()));
        itemDTO.setDuration30Day(getSaleInfoByDurationDate(usDate, itemDTO.getSku()));
        itemDTO.setDuration3060Day(getSaleInfoByDurationDate(TimeUtil.dateFixByDay(usDate, -30, 0, 0), itemDTO.getSku()));
        itemDTO.setLastYearDuration30Day(getSaleInfoByDurationDate(TimeUtil.dateFixByYear(usDate, -1), itemDTO.getSku()));

        //商品工厂归宿信息
        List<FactoryItemDO> factoryItemDOS = factoryItemService.getInfoBySku(itemDTO.getSku());
        itemDTO.setFactoryItemDTOS(factoryItemDOS.stream().map(factoryItemDO -> {
            FactoryDO factoryDO = factoryService.getByFid(factoryItemDO.getFactoryId());
            FactoryItemDTO factoryItemDTO = new FactoryItemDTO();
            factoryItemDTO.setId(factoryItemDO.getId());
            factoryItemDTO.setFactoryId(factoryDO.getId());
            factoryItemDTO.setFactoryName(factoryDO.getFactoryName());
            factoryItemDTO.setSku(factoryItemDO.getSku());
            factoryItemDTO.setFactoryPrice(factoryItemDO.getFactoryPrice());
            factoryItemDTO.setDesc(factoryItemDO.getItemDesc());
            return factoryItemDTO;
        }).collect(Collectors.toList()));

        //智能补货标
        SmartReplenishmentDTO smart = smartReplenishmentTask.getSmartReplenishment(itemDO.getSku());
        itemDTO.setReplenishmentCode(smart == null ? 0 : smart.getReplenishmentCode());
        itemDTO.setReplenishmentNum(smart == null ? 0 : smart.getNeedNum().intValue());

        return itemDTO;
    }

    private Integer getInBoundNum(String sku) {
        List<ShipmentItemRecordDO> records = shipmentItemRecordService.getAllRecordBySku(sku);
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }

        List<String> shipmentIds = records.stream().map(ShipmentItemRecordDO::getShipmentId).collect(Collectors.toList());
        List<ShipmentInfoRecordDO> infoRecords = shipmentInfoRecordService.getAllRecordByShipmentIds(shipmentIds);
        Map<String, String> useMap = infoRecords.stream().filter(infoRecord -> !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CLOSED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CANCELLED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_DELETED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_ERROR.getCode()))
                .collect(Collectors.toMap(ShipmentInfoRecordDO::getShipmentId, ShipmentInfoRecordDO::getShipmentStatus));


        if (CollectionUtils.isEmpty(useMap)) {
            return 0;
        }

        int num = 0;
        for (ShipmentItemRecordDO record : records) {
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

    private SaleInfoDTO getSaleInfoByDate(Date date, String sku) {
        String statDate = TimeUtil.getSimpleFormat(date);
        SaleInfoDO saleInfoDO = saleInfoService.getSaleInfoDOByDate(statDate, sku);

        SaleInfoDTO saleInfoDTO = new SaleInfoDTO();
        if (saleInfoDO == null) {
            saleInfoDTO.setSaleNum(0);
            saleInfoDTO.setOrderNum(0);
            saleInfoDTO.setSaleVolume(0.0);
            saleInfoDTO.setUnitPrice(0.0);
        } else {
            saleInfoDTO.setSaleNum(saleInfoDO.getSaleNum());
            saleInfoDTO.setOrderNum(saleInfoDO.getOrderNum());
            saleInfoDTO.setSaleVolume(saleInfoDO.getSaleVolume());
            saleInfoDTO.setUnitPrice(saleInfoDO.getUnitPrice());
        }
        return saleInfoDTO;
    }

    private SaleInfoDTO getSaleInfoByDurationDate(Date date, String sku) {
        Date beginDate = TimeUtil.dateFixByDay(date, -30, 0, 0);
        String strEndDate = TimeUtil.getSimpleFormat(date);
        String strBeginDate = TimeUtil.getSimpleFormat(beginDate);
        List<SaleInfoDO> compareList = saleInfoService.getSaleInfoByDurationDate(sku, strBeginDate, strEndDate);

        int saleNum = 0;
        int orderNum = 0;
        double saleVolume = 0.0;
        if (!CollectionUtils.isEmpty(compareList)) {
            for (SaleInfoDO saleInfo : compareList) {
                saleNum += saleInfo.getSaleNum();
                orderNum += saleInfo.getOrderNum();
                saleVolume += saleInfo.getSaleVolume();
            }
        }
        SaleInfoDTO saleInfoDTO = new SaleInfoDTO();
        saleInfoDTO.setSaleNum(saleNum);
        saleInfoDTO.setOrderNum(orderNum);
        saleInfoDTO.setSaleVolume(saleVolume);
        if (saleNum == 0) {
            saleInfoDTO.setUnitPrice(0.0);
        } else {
            saleInfoDTO.setUnitPrice(saleVolume / (double) saleNum);
        }
        return saleInfoDTO;
    }


    /**
     * 仓储统一操作，防止多线程操作引起数据错误
     *
     * @param sku
     * @param operateType
     * @param dealNum
     */
    public void dealSkuInventory(String sku, String operateType, Integer dealNum) {

        if (!syncLock.containsKey(sku)) {
            syncLock.put(sku, new Object());
        }

        //对sku进行同步操作
        synchronized (syncLock.get(sku)) {
            InventoryDO inventory = inventoryService.getInventoryBySku(sku);
            switch (operateType) {
                case "set":
                    inventory.setLocalQuantity(dealNum);
                    inventory.calculateTotalQuantity();
                    inventoryService.updateInventory(inventory);
                    break;
                case "mod":
                    Integer localNum = inventory.getLocalQuantity() == null ? 0 : inventory.getLocalQuantity();
                    Integer nowLocal = localNum + dealNum;
                    inventory.setLocalQuantity(nowLocal < 0 ? 0 : nowLocal);
                    inventory.calculateTotalQuantity();
                    inventoryService.updateInventory(inventory);
                    break;
                case "refresh":
                    ListInventorySupplyResponse inventorySupplyResponse = awsClient.getInventoryInfoBySku(sku);
                    if (inventorySupplyResponse == null) {
                        log.info("商品【{}】库存aws请求为空，等待下次刷新", sku);
                        return;
                        //throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, "返回结果为空");
                    }

                    //存在就更新
                    if (inventory == null) {
                        inventory = new InventoryDO();
                        ConvertUtil.convertToInventoryDO(inventorySupplyResponse, inventory);
                        inventoryService.createInventory(inventory);
                    } else {
                        ConvertUtil.convertToInventoryDO(inventorySupplyResponse, inventory);
                        inventoryService.updateInventory(inventory);
                    }
                    break;
                default:
            }
            itemDetailCache.refreshCache(sku);
        }
    }

    public List<SmartReplenishmentDTO> querySmartList() {
        return smartReplenishmentTask.getSmartReplenishmentDTOList(null);
    }

    public String spiderShipment(String shipmentId) {
        return shipmentSpiderTask.shipmentSpiderTask(shipmentId);
    }

    public String fnskuQuery(String fnsku) {
        InventoryDO inventoryDO = inventoryService.getInventoryByFnsku(fnsku);
        if (inventoryDO != null) {
            return inventoryDO.getSku();
        }
        return null;
    }

    public void processRelationShip(ItemDO itemDO) {
        Relationships relationships = JSONObject.parseObject(itemDO.getRelationship(), Relationships.class);
        if (itemDO.getIsParent() == 0) {
            String fatherAsin = relationships.getVariationParent().getIdentifiers().getMarketplaceASIN().getAsin();
            ItemDO fatherItem = itemService.getItemDOByAsin(fatherAsin, 1);
            if (fatherItem == null) {
                GetMatchingProductForIdResponse fatherResp = awsClient.getProductInfoByAsin("ASIN", fatherAsin);
                fatherItem = ConvertUtil.convertToItemDO(new ItemDO(), fatherResp, null);
                fatherItem.setItemPrice(0.0);
                fatherItem.setSku(StringUtils.isEmpty(fatherItem.getSku()) ? "" : fatherItem.getSku());
                itemService.createItem(fatherItem);
                log.info("创建父sku商品：{}", fatherItem.getAsin());
            }

            //创建对应关系
            FatherChildRelationDO relationDO = fatherChildRelationService.getRelationByFatherAndChildAsin(fatherItem.getAsin(), itemDO.getAsin());
            if (relationDO == null) {
                relationDO = new FatherChildRelationDO();
                relationDO.setFatherSku(fatherItem.getSku());
                relationDO.setFatherAsin(fatherItem.getAsin());
                relationDO.setChildSku(itemDO.getSku());
                relationDO.setChildAsin(itemDO.getAsin());
                fatherChildRelationService.createRelation(relationDO);
            }
        } else if (itemDO.getIsParent() == 1) {
            List<String> childAsins = relationships.getVariationChildrens()
                    .stream().map(child -> child.getIdentifiers().getMarketplaceASIN().getAsin())
                    .collect(Collectors.toList());
            childAsins.forEach(childAsin -> {
                ItemDO childItem = itemService.getItemDOByAsin(childAsin, 0);
                if (childItem == null) {
                    GetMatchingProductForIdResponse fatherResp = awsClient.getProductInfoByAsin("ASIN", childAsin);
                    childItem = ConvertUtil.convertToItemDO(new ItemDO(), fatherResp, null);

                    //获取商品价格
                    Double itemPrice = ConvertUtil.getItemPrice(awsClient.getMyPriceForSku(childItem.getSku()));
                    childItem.setItemPrice(itemPrice);

                    childItem.setActive(1);
                    itemService.createItem(childItem);

                    dealSkuInventory(childItem.getSku(), "refresh", 0);
                }

                //创建对应关系
                FatherChildRelationDO relationDO = fatherChildRelationService.getRelationByFatherAndChildAsin(itemDO.getAsin(), childItem.getAsin());
                if (relationDO == null) {
                    relationDO = new FatherChildRelationDO();
                    relationDO.setFatherSku(itemDO.getSku());
                    relationDO.setFatherAsin(itemDO.getAsin());
                    relationDO.setChildSku(childItem.getSku());
                    relationDO.setChildAsin(childItem.getAsin());
                    fatherChildRelationService.createRelation(relationDO);
                }
            });
        } else {
            log.info("本sku：{} 即没有子体也没有父体", itemDO.getSku());
        }
    }

    public static void main(String[] args) {
        AwsClient cliet = new AwsClient();
        GetMatchingProductForIdResponse response = cliet.getProductInfoByAsin("SellerSKU", "P20-410-14A");
        ItemDO itemDO = ConvertUtil.convertToItemDO(new ItemDO(), response, null);
        System.out.println(response);
    }
}
