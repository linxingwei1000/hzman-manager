package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.ItemCategoryDo;
import com.cn.hzm.core.repository.mapper.ItemCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class ItemCategoryDao {

    @Autowired
    private ItemCategoryMapper itemCategoryMapper;

    public List<ItemCategoryDo> getItemCategoryByItemId(Integer itemId) {
        QueryWrapper<ItemCategoryDo> query = new QueryWrapper<>();
        query.eq("item_id", itemId);
        return itemCategoryMapper.selectList(query);
    }

    /**
     * 创建商品
     *
     * @param itemCategoryDO
     */
    public void createItemCategory(ItemCategoryDo itemCategoryDO) {
        itemCategoryDO.setUtime(new Date());
        itemCategoryDO.setCtime(new Date());
        itemCategoryMapper.insert(itemCategoryDO);
    }

    /**
     * 更新商品
     *
     * @param itemCategoryDO
     */
    public void updateItemCategory(ItemCategoryDo itemCategoryDO) {
        itemCategoryDO.setUtime(new Date());
        itemCategoryMapper.updateById(itemCategoryDO);
    }

    public Integer deleteItemCategoryByItemId(Integer itemId){
        QueryWrapper<ItemCategoryDo> query = new QueryWrapper<>();
        query.eq("item_id", itemId);
        return itemCategoryMapper.delete(query);
    }

    public Integer deleteItemCategory(Integer id){
        return itemCategoryMapper.deleteById(id);
    }
}
