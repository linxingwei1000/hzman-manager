package com.cn.hzm.factory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.FactoryItemDO;
import com.cn.hzm.factory.dao.FactoryItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 3:45 下午
 */
@Component
public class FactoryItemService {

    @Autowired
    private FactoryItemMapper factoryItemMapper;

    /**
     * 创建商品工厂归属
     * @param factoryItemDO
     */
    public void createFactoryItem(FactoryItemDO factoryItemDO){
        factoryItemDO.setUtime(new Date());
        factoryItemDO.setCtime(new Date());
        factoryItemMapper.insert(factoryItemDO);
    }

    public void updateFactoryItem(FactoryItemDO factoryItemDO) {
        factoryItemDO.setUtime(new Date());
        factoryItemMapper.updateById(factoryItemDO);
    }

    public List<FactoryItemDO> getInfoBySku(String sku){
        QueryWrapper<FactoryItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        return factoryItemMapper.selectList(query);
    }

    public FactoryItemDO getInfoBySkuAndFactoryId(String sku, Integer factoryId){
        QueryWrapper<FactoryItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.eq("factory_id", factoryId);
        return factoryItemMapper.selectOne(query);
    }
}
