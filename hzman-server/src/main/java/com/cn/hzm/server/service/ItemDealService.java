package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.entity.*;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.factory.enums.OrderStatusEnum;
import com.cn.hzm.factory.service.FactoryOrderItemService;
import com.cn.hzm.factory.service.FactoryOrderService;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.order.service.SaleInfoService;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 5:55 下午
 */
@Service
public class ItemDealService {

    @Autowired
    private AwsClient awsClient;

    @Autowired
    private ItemService itemService;

    @Autowired
    private FactoryOrderService factoryOrderService;

    @Autowired
    private FactoryOrderItemService factoryOrderItemService;

    @Autowired
    private SaleInfoService saleInfoService;

    @Autowired
    private InventoryService inventoryService;

    public JSONObject processListItem(ItemConditionDTO conditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(conditionDTO);
        condition.remove("pageNum");
        condition.remove("pageSize");
        List<ItemDO> list = itemService.getListByCondition(condition);

        List<ItemDTO> itemDTOS = Lists.newArrayList();
        conditionDTO.pageResult(list).forEach(itemDO -> {
            ItemDTO itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDTO.class);
            InventoryDO inventoryDO = inventoryService.getInventoryByAsin(itemDO.getAsin());
            InventoryDTO inventoryDTO = JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDTO.class);

            List<FactoryOrderItemDO> factoryOrderItemDOS = factoryOrderItemService.getOrderBySku(itemDO.getSku());

            Map<Integer, FactoryOrderDO> map = Maps.newHashMap();
            List<FactoryQuantityDTO> factoryQuantityDTOS = factoryOrderItemDOS.stream().filter(orderItem ->{
                FactoryOrderDO order = factoryOrderService.getOrderById(orderItem.getFactoryOrderId());
                map.put(order.getId(), order);
                return OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode().equals(order.getOrderStatus())
                        || OrderStatusEnum.ORDER_CONFIRM.getCode().equals(order.getOrderStatus())
                        || OrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode().equals(order.getOrderStatus());
            }).map(order -> {
                FactoryQuantityDTO dto = new FactoryQuantityDTO();
                dto.setNum(order.getOrderNum());
                dto.setDeliveryDate(map.get(order.getFactoryOrderId()).getDeliveryDate());
                return dto;
            }).collect(Collectors.toList());
            inventoryDTO.setFactoryQuantityInfos(factoryQuantityDTOS);

            inventoryDTO.setFactoryQuantity(Math.toIntExact(factoryQuantityDTOS.stream().map(FactoryQuantityDTO::getNum).count()));
            inventoryDTO.setLocalTotalQuantity(inventoryDTO.getFactoryQuantity() + inventoryDTO.getLocalQuantity());
            inventoryDTO.setTotalQuantity(inventoryDTO.getLocalTotalQuantity() + inventoryDTO.getAmazonQuantity());
            itemDTO.setInventoryDTO(inventoryDTO);

            //销量
            Date date = new Date();
            itemDTO.setToday(getSaleInfoByDate(TimeUtil.dateFixByDay(date, 0, -8, 0), itemDTO.getSku()));
            itemDTO.setYesterday(getSaleInfoByDate(TimeUtil.dateFixByDay(date, -1, -8, 0), itemDTO.getSku()));
            itemDTO.setLastWeekToday(getSaleInfoByDate(TimeUtil.dateFixByDay(date, -7, -8, 0), itemDTO.getSku()));
            itemDTOS.add(itemDTO);
        });

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data", JSONObject.toJSON(itemDTOS));
        return respJo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSync(String sku) {

        //asin取aws数据：商品信息
        GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin("SellerSKU", sku);
        ItemDO itemDO = ConvertUtil.convertToItemDO(new ItemDO(), resp, sku);

        ItemDO old = itemService.getItemDOBySku(sku);
        if (old != null) {
            itemDO.setId(old.getId());
            itemService.updateItem(itemDO);
        } else {
            itemService.createItem(itemDO);
        }

        //存在就更新
        InventoryDO tmpInventory = inventoryService.getInventoryBySku(sku);
        if (tmpInventory == null) {
            tmpInventory = new InventoryDO();
            ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(sku), tmpInventory);
            inventoryService.createInventory(tmpInventory);
        } else {
            ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(sku), tmpInventory);
            inventoryService.updateInventory(tmpInventory);
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

    public boolean modLocalNum(Integer iId, Integer curLocalNum){
        InventoryDO inventory = new InventoryDO();
        inventory.setId(iId);
        inventory.setLocalQuantity(curLocalNum);
        inventoryService.updateInventory(inventory);

        return true;
    }

    private SaleInfoDTO getSaleInfoByDate(Date date, String sku){
        String statDate = TimeUtil.getSimpleFormat(date);
        SaleInfoDO saleInfoDO = saleInfoService.getSaleInfoDOByDate(statDate, sku);

        SaleInfoDTO saleInfoDTO = new SaleInfoDTO();
        if(saleInfoDO == null){
            saleInfoDTO.setSaleNum(0);
            saleInfoDTO.setSaleVolume(0.0);
            saleInfoDTO.setUnitPrice(0.0);
        }else{
            saleInfoDTO.setSaleNum(saleInfoDO.getSaleNum());
            saleInfoDTO.setSaleVolume(saleInfoDO.getSaleVolume());
            saleInfoDTO.setUnitPrice(saleInfoDO.getUnitPrice());
        }
        return saleInfoDTO;
    }
}
