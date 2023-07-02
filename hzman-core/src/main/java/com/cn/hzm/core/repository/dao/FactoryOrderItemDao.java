package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FactoryOrderItemDo;
import com.cn.hzm.core.repository.mapper.FactoryOrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:43 下午
 */
@Service
public class FactoryOrderItemDao {

    @Autowired
    private FactoryOrderItemMapper factoryOrderItemMapper;

    /**
     * 创建订单商品
     * @param factoryOrderItemDO
     */
    public void createFactoryOrderItem(FactoryOrderItemDo factoryOrderItemDO){
        factoryOrderItemDO.setUtime(new Date());
        factoryOrderItemDO.setCtime(new Date());
        factoryOrderItemMapper.insert(factoryOrderItemDO);
    }

    public List<FactoryOrderItemDo> getItemsByOrderId(Integer oId){
        QueryWrapper<FactoryOrderItemDo> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        return factoryOrderItemMapper.selectList(query);
    }

    public List<FactoryOrderItemDo> getOrderBySku(String sku){
        QueryWrapper<FactoryOrderItemDo> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.orderByDesc("ctime");
        return factoryOrderItemMapper.selectList(query);
    }

    public FactoryOrderItemDo getItemByOrderIdAndSku(Integer oId, String sku){
        QueryWrapper<FactoryOrderItemDo> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        query.eq("sku", sku);
        query.orderByDesc("ctime");
        return factoryOrderItemMapper.selectOne(query);
    }

    /**
     * 更新订单商品
     * @param factoryOrderItemDO
     */
    public void updateFactoryOrder(FactoryOrderItemDo factoryOrderItemDO){
        factoryOrderItemDO.setUtime(new Date());
        factoryOrderItemMapper.updateById(factoryOrderItemDO);
    }

    public Integer deleteByOrderId(Integer oId){
        QueryWrapper<FactoryOrderItemDo> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        return factoryOrderItemMapper.delete(query);
    }
}
