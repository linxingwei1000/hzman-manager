package com.cn.hzm.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.core.util.SqlCommonUtil;
import com.cn.hzm.item.dao.ItemMapper;
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

    public List<ItemDO> getListByCondition(Map<String, String> condition, Integer offset, Integer limit){
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        if(condition.size()!=0){
        }

        query.orderByAsc("ctime");
        query.last(SqlCommonUtil.limitOffsetSql(offset, limit));
        return itemMapper.selectList(query);
    }

    public ItemDO getItemDOBySku(String sku){
        QueryWrapper<ItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        return itemMapper.selectOne(query);
    }

    /**
     * 创建商品
     * @param itemDO
     */
    public void createItem(ItemDO itemDO){
        itemDO.setUtime(new Date());
        itemDO.setCtime(new Date());
        itemMapper.insert(itemDO);
    }

    /**
     * 更新商品
     * @param itemDO
     */
    public void updateItem(ItemDO itemDO){
        itemDO.setUtime(new Date());
        itemMapper.updateById(itemDO);
    }
}