package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FactoryItemDo;
import com.cn.hzm.core.repository.mapper.FactoryItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 3:45 下午
 */
@Component
public class FactoryItemDao {

    @Autowired
    private FactoryItemMapper factoryItemMapper;

    /**
     * 创建商品工厂归属
     * @param factoryItemDO
     */
    public void createFactoryItem(FactoryItemDo factoryItemDO){
        factoryItemDO.setUtime(new Date());
        factoryItemDO.setCtime(new Date());
        factoryItemMapper.insert(factoryItemDO);
    }

    public void updateFactoryItem(FactoryItemDo factoryItemDO) {
        factoryItemDO.setUtime(new Date());
        factoryItemMapper.updateById(factoryItemDO);
    }

    public FactoryItemDo getInfoById(Integer id) {
        return factoryItemMapper.selectById(id);
    }

    public List<FactoryItemDo> getAll(){
        QueryWrapper<FactoryItemDo> query = new QueryWrapper<>();
        return factoryItemMapper.selectList(query);
    }

    public List<FactoryItemDo> getInfoBySku(String sku){
        QueryWrapper<FactoryItemDo> query = new QueryWrapper<>();
        query.eq("sku", sku);
        return factoryItemMapper.selectList(query);
    }

    public List<FactoryItemDo> getInfoByFactoryId(Integer factoryId){
        QueryWrapper<FactoryItemDo> query = new QueryWrapper<>();
        query.eq("factory_id", factoryId);
        return factoryItemMapper.selectList(query);
    }

    public FactoryItemDo getInfoBySkuAndFactoryId(String sku, Integer factoryId){
        QueryWrapper<FactoryItemDo> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.eq("factory_id", factoryId);
        return factoryItemMapper.selectOne(query);
    }

    public Integer deleteFactoryItem(Integer id) {
        return factoryItemMapper.deleteById(id);
    }
}
