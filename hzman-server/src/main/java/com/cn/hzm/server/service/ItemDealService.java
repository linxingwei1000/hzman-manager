package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.inventory.Member;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.dto.InventoryDTO;
import com.cn.hzm.server.dto.ItemConditionDTO;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.InventoryService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    public List<ItemDTO> processListItem(ItemConditionDTO conditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(conditionDTO);

        Integer offset = conditionDTO.getPageSize() * conditionDTO.getPageNum();
        Integer limit = conditionDTO.getPageSize();
        List<ItemDO> list = itemService.getListByCondition(condition, offset, limit);

        List<ItemDTO> itemDTOS = Lists.newArrayList();
        list.forEach(itemDO -> {
            ItemDTO itemDTO = JSONObject.parseObject(JSONObject.toJSONString(itemDO), ItemDTO.class);

            InventoryDO inventoryDO = inventoryService.getInventoryByItemId(itemDO.getId());
            itemDTO.setInventoryDTO(JSONObject.parseObject(JSONObject.toJSONString(inventoryDO), InventoryDTO.class));
            itemDTOS.add(itemDTO);
        });
        return itemDTOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processItemCreate(ItemDTO item) {

        ItemDO itemDO = new ItemDO();
        InventoryDO inventoryDO = new InventoryDO();

        //asin取aws数据：商品库存，销量
        //todo sku 空判断
        itemDO.setSku(item.getSku());
        Member member = ConvertUtil.convertToInventoryDO(awsClient.getInventoryInfoBySku(item.getSku()), inventoryDO);

        //设置属性
        itemDO.setFnsku(member.getFnsku());

        //设置asin
        itemDO.setAsin(member.getAsin());

        //asin取aws数据：商品信息
        if(!StringUtils.isEmpty(itemDO.getAsin())){
            GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin(itemDO.getAsin());
            itemDO.setAttrs(JSONObject.toJSONString(resp));
        }

        ItemDO old = itemService.getItemDOBySku(item.getSku());
        if(old!=null){
            itemDO.setId(old.getId());
            itemService.updateItem(itemDO);
        }else{
            itemService.createItem(itemDO);
        }

        if (itemDO.getId() == null) {
            throw new RuntimeException("创建商品失败");
        }

        inventoryDO.setItemId(itemDO.getId());
        inventoryService.createInventory(inventoryDO);
    }
}
