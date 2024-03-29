package com.cn.hzm.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.dao.ItemMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class ItemService {

    @Autowired
    private ItemMapper itemMapper;

    public List<ItemDO> getListByCondition(Map<String, String> condition, String[] fields) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        if (condition.size() != 0) {
            if (condition.containsKey("sku")) {
                query.like("sku", condition.get("sku"));
            }
        }

        query.eq("active", 1);
        query.select(fields);
        return itemMapper.selectList(query);
    }

    public List<ItemDO> getItemByParentType(Integer isParent, String[] fields) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("is_parent", isParent);
        query.eq("active", 1);
        query.select(fields);
        return itemMapper.selectList(query);
    }

    public ItemDO getById(Integer id) {
        return itemMapper.selectById(id);
    }

    /**
     * sku只处理子体
     *
     * @param sku
     * @return
     */
    public ItemDO getItemDOBySku(String sku) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.in("is_parent", Lists.newArrayList(0, 2));
        return itemMapper.selectOne(query);
    }

    /**
     * sku 数据库判断
     *
     * @param asin
     * @return
     */
    public ItemDO getSingleItemDOByAsin(String asin, String sku) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("asin", asin);
        query.eq("sku", sku);
        return itemMapper.selectOne(query);
    }

    /**
     * sku 数据库判断
     *
     * @param sku
     * @return
     */
    public List<ItemDO> getItemDOSBySku(String sku) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        return itemMapper.selectList(query);
    }

    public ItemDO getItemDOByAsin(String asin, Integer isParent) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("asin", asin);
        query.eq("is_parent", isParent);
        return itemMapper.selectOne(query);
    }

    public List<ItemDO> fuzzyQuery(String field, String value) {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.like(field, value);
        return itemMapper.selectList(query);
    }

    /**
     * 获取商品类型
     */
    public List<ItemDO> getItemType() {
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.select(" DISTINCT item_type ").lambda();
        return itemMapper.selectList(query);
    }

    /**
     * 创建商品
     *
     * @param itemDO
     */
    public void createItem(ItemDO itemDO) {
        itemDO.setUtime(new Date());
        itemDO.setCtime(new Date());
        itemMapper.insert(itemDO);
    }

    /**
     * 更新商品
     *
     * @param itemDO
     */
    public void updateItem(ItemDO itemDO) {
        itemDO.setUtime(new Date());
        itemMapper.updateById(itemDO);
    }

    public Integer deleteItem(Integer id){
        return itemMapper.deleteById(id);
    }
}
