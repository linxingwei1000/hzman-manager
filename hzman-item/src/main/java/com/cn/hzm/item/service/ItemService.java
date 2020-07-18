package com.cn.hzm.item.service;

import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.dao.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class ItemService {

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 创建商品
     *
     * @param itemDO
     */
    public void createItem(ItemDO itemDO){
        itemDO.setUtime(new Date());
        itemDO.setCtime(new Date());
        itemMapper.insert(itemDO);
    }
}
