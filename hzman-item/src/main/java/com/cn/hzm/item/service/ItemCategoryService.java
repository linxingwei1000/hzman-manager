package com.cn.hzm.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.ItemCategoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.dao.ItemCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class ItemCategoryService {

    @Autowired
    private ItemCategoryMapper itemCategoryMapper;

    public ItemCategoryDO getItemCategoryByItemId(Integer ItemId) {
        QueryWrapper<ItemCategoryDO> query = new QueryWrapper<>();
        query.eq("item_id", ItemId);
        return itemCategoryMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param itemCategoryDO
     */
    public void createItemCategory(ItemCategoryDO itemCategoryDO) {
        itemCategoryDO.setUtime(new Date());
        itemCategoryDO.setCtime(new Date());
        itemCategoryMapper.insert(itemCategoryDO);
    }

    /**
     * 更新商品
     *
     * @param itemCategoryDO
     */
    public void updateItemCategory(ItemCategoryDO itemCategoryDO) {
        itemCategoryDO.setUtime(new Date());
        itemCategoryMapper.updateById(itemCategoryDO);
    }

    public Integer deleteItemCategory(Integer id){
        return itemCategoryMapper.deleteById(id);
    }
}
