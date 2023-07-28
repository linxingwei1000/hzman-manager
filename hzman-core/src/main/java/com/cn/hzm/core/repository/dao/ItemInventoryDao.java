package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.ItemInventoryDo;
import com.cn.hzm.core.repository.mapper.ItemInventoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:22 下午
 */
@Service
public class ItemInventoryDao {

    @Autowired
    private ItemInventoryMapper itemInventoryMapper;

    /**
     * 获取库存
     * @param sku
     */
    public ItemInventoryDo getInventoryBySku(String sku, Integer userMarketId){
        QueryWrapper<ItemInventoryDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("sku", sku);
        return itemInventoryMapper.selectOne(query);
    }

    /**
     * 获取库存
     * @param fnsku
     */
    public ItemInventoryDo getInventoryByFnsku(String fnsku, Integer userMarketId){
        QueryWrapper<ItemInventoryDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("fnsku", fnsku);
        return itemInventoryMapper.selectOne(query);
    }

    /**
     * 获取库存
     * @param asin
     */
    public ItemInventoryDo getInventoryByAsin(String asin){
        QueryWrapper<ItemInventoryDo> query = new QueryWrapper<>();
        query.eq("asin", asin);
        return itemInventoryMapper.selectOne(query);
    }

    /**
     * 获取库存
     */
    public List<ItemInventoryDo> getInventoryWhenStockNotNull(){
        QueryWrapper<ItemInventoryDo> query = new QueryWrapper<>();
        query.last(" where local_quantity != 0 and local_quantity is not null ");
        query.select("asin", "sku", "user_market_id", "local_quantity");
        return itemInventoryMapper.selectList(query);
    }

    /**
     * 获取库存
     * @param sku
     */
    public ItemInventoryDo getInventoryBySkuAndAsin(String sku, String asin){
        QueryWrapper<ItemInventoryDo> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.eq("asin", asin);
        return itemInventoryMapper.selectOne(query);
    }

    /**
     * 创建库存
     * @param itemInventoryDO
     */
    public void createInventory(ItemInventoryDo itemInventoryDO){
        itemInventoryDO.setUtime(new Date());
        itemInventoryDO.setCtime(new Date());
        itemInventoryMapper.insert(itemInventoryDO);
    }

    /**
     * 更新库存
     * @param itemInventoryDO
     */
    public void updateInventory(ItemInventoryDo itemInventoryDO){
        itemInventoryDO.setUtime(new Date());
        itemInventoryMapper.updateById(itemInventoryDO);
    }

    public Integer deleteInventory(Integer id){
        return itemInventoryMapper.deleteById(id);
    }
}
