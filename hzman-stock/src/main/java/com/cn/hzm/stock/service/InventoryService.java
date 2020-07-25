package com.cn.hzm.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.stock.dao.InventoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:22 下午
 */
@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;


    /**
     * 获取库存
     * @param itemId
     */
    public InventoryDO getInventoryByItemId(Integer itemId){
        QueryWrapper<InventoryDO> query = new QueryWrapper<>();
        query.eq("item_id", itemId);
        return inventoryMapper.selectOne(query);
    }
    /**
     * 创建库存
     * @param inventoryDO
     */
    public void createInventory(InventoryDO inventoryDO){
        inventoryDO.setLocalQuantity(0);
        inventoryDO.calculateTotalQuantity();
        inventoryDO.setUtime(new Date());
        inventoryDO.setCtime(new Date());
        inventoryMapper.insert(inventoryDO);
    }
}
