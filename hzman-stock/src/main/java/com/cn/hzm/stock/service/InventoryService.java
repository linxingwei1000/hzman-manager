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
     * @param asin
     */
    public InventoryDO getInventoryById(String asin){
        QueryWrapper<InventoryDO> query = new QueryWrapper<>();
        query.eq("asin", asin);
        return inventoryMapper.selectOne(query);
    }

    /**
     * 获取库存
     * @param sku
     */
    public InventoryDO getInventoryBySku(String sku){
        QueryWrapper<InventoryDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        return inventoryMapper.selectOne(query);
    }

    /**
     * 获取库存
     * @param asin
     */
    public InventoryDO getInventoryByAsin(String asin){
        QueryWrapper<InventoryDO> query = new QueryWrapper<>();
        query.eq("asin", asin);
        return inventoryMapper.selectOne(query);
    }


    /**
     * 创建库存
     * @param inventoryDO
     */
    public void createInventory(InventoryDO inventoryDO){
        inventoryDO.setUtime(new Date());
        inventoryDO.setCtime(new Date());
        inventoryMapper.insert(inventoryDO);
    }

    /**
     * 更新库存
     * @param inventoryDO
     */
    public void updateInventory(InventoryDO inventoryDO){
        inventoryDO.setUtime(new Date());
        inventoryMapper.updateById(inventoryDO);
    }
}