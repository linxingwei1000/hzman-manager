package com.cn.hzm.factory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.FactoryOrderItemDO;
import com.cn.hzm.factory.dao.FactoryOrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:43 下午
 */
@Service
public class FactoryOrderItemService {

    @Autowired
    private FactoryOrderItemMapper factoryOrderItemMapper;

    /**
     * 创建订单商品
     * @param factoryOrderItemDO
     */
    public void createFactoryOrderItem(FactoryOrderItemDO factoryOrderItemDO){
        factoryOrderItemDO.setUtime(new Date());
        factoryOrderItemDO.setCtime(new Date());
        factoryOrderItemMapper.insert(factoryOrderItemDO);
    }

    public List<FactoryOrderItemDO> getItemsByOrderId(Integer oId){
        QueryWrapper<FactoryOrderItemDO> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        return factoryOrderItemMapper.selectList(query);
    }

    public List<FactoryOrderItemDO> getOrderBySku(String sku){
        QueryWrapper<FactoryOrderItemDO> query = new QueryWrapper<>();
        query.eq("sku", sku);
        query.orderByDesc("ctime");
        return factoryOrderItemMapper.selectList(query);
    }

    public FactoryOrderItemDO getItemByOrderIdAndSku(Integer oId, String sku){
        QueryWrapper<FactoryOrderItemDO> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        query.eq("sku", sku);
        query.orderByDesc("ctime");
        return factoryOrderItemMapper.selectOne(query);
    }

    /**
     * 更新订单商品
     * @param factoryOrderItemDO
     */
    public void updateFactoryOrder(FactoryOrderItemDO factoryOrderItemDO){
        factoryOrderItemDO.setUtime(new Date());
        factoryOrderItemMapper.updateById(factoryOrderItemDO);
    }

    public Integer deleteByOrderId(Integer oId){
        QueryWrapper<FactoryOrderItemDO> query = new QueryWrapper<>();
        query.eq("factory_order_id", oId);
        return factoryOrderItemMapper.delete(query);
    }
}
