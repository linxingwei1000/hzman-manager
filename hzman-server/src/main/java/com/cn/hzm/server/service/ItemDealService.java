package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.dto.InventoryDTO;
import com.cn.hzm.server.dto.ItemConditionDTO;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.dto.SimpleItemDTO;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Resource
    private ItemService itemService;

    @Resource
    private InventoryService inventoryService;

    public JSONObject processListItem(ItemConditionDTO conditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(conditionDTO);
        List<ItemDO> list = itemService.getListByCondition(condition);

        List<ItemDTO> itemDTOS = Lists.newArrayList();
        conditionDTO.pageResult(list).forEach(itemDO -> {
            ItemDTO itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDTO.class);
            InventoryDO inventoryDO = inventoryService.getInventoryByAsin(itemDO.getAsin());
            itemDTO.setInventoryDTO(JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDTO.class));
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
            tmpInventory.setLocalQuantity(0);
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
}
