package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.product.BuyingPrice;
import com.cn.hzm.core.aws.domain.product.Offer;
import com.cn.hzm.core.aws.domain.product.Product;
import com.cn.hzm.core.aws.domain.product.ProductError;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.aws.resp.product.GetMyPriceForSkuResponse;
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
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.cn.hzm.server.cache.comparator.SortHelper;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.task.ShipmentSpiderTask;
import com.cn.hzm.server.task.SmartReplenishmentTask;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.cn.hzm.stock.service.ShipmentInfoRecordService;
import com.cn.hzm.stock.service.ShipmentItemRecordService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
        List<ItemDTO> itemRespList = itemDetailCache.getCacheBySort(conditionDTO.getSearchType(), conditionDTO.getKey(), conditionDTO.getItemSortType());

        JSONObject respJo = new JSONObject();
        respJo.put("total", itemRespList.size());
        respJo.put("data", conditionDTO.pageResult(itemRespList));
        return respJo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSync(String sku) {

        //asin取aws数据：商品信息
        GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin("SellerSKU", sku);

        //判断错误
        if (resp.getGetMatchingProductForIdResult().getError() != null) {
            ProductError error = resp.getGetMatchingProductForIdResult().getError();
            throw new HzmException(ExceptionCode.REQUEST_SKU_REQUEST_ERROR, error.getMessage());
        }

        ItemDO itemDO = ConvertUtil.convertToItemDO(new ItemDO(), resp, sku);

        //获取商品单价
        Double itemPrice = ConvertUtil.getItemPrice(awsClient.getMyPriceForSku(sku));
        itemDO.setItemPrice(itemPrice);
        itemDO.setActive(1);

        ItemDO old = itemService.getItemDOBySku(sku);
        if (old != null) {
            itemDO.setId(old.getId());
            itemService.updateItem(itemDO);
        } else {
            itemService.createItem(itemDO);
        }

        dealSkuInventory(sku, "refresh", 0);
    }

    public void deleteItem(String sku) {
        //逻辑删除
        ItemDO old = itemService.getItemDOBySku(sku);
        old.setActive(0);
        itemService.updateItem(old);

        itemDetailCache.deleteCache(sku);
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

    public ItemDTO buildItemDTO(ItemDO itemDO) {
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
        Date usDate = TimeUtil.transformNowToUsDate();
        itemDTO.setToday(getSaleInfoByDate(usDate, itemDTO.getSku()));
        itemDTO.setYesterday(getSaleInfoByDate(TimeUtil.dateFixByDay(usDate, -1, 0, 0), itemDTO.getSku()));
        itemDTO.setDuration30Day(getSaleInfoByDurationDate(usDate, itemDTO.getSku()));
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
        List<String> shipmentIds = records.stream().map(ShipmentItemRecordDO::getShipmentId).collect(Collectors.toList());
        List<ShipmentInfoRecordDO> infoRecords = shipmentInfoRecordService.getAllRecordByShipmentIds(shipmentIds);
        List<String> useShipmentIds = infoRecords.stream().filter(infoRecord -> !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CLOSED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_CANCELLED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_DELETED.getCode())
                && !infoRecord.getShipmentStatus().equals(AmazonShipmentStatusEnum.STATUS_ERROR.getCode()))
                .map(ShipmentInfoRecordDO::getShipmentId).collect(Collectors.toList());

        int num = 0;
        for(ShipmentItemRecordDO record: records){
            if(useShipmentIds.contains(record.getShipmentId())){
                if(record.getQuantityReceived()!=null){
                    num += record.getQuantityReceived();
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
                    //存在就更新
                    if (inventory == null) {
                        inventory = new InventoryDO();
                        ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(sku), inventory);
                        inventoryService.createInventory(inventory);
                    } else {
                        ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(sku), inventory);
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
}
