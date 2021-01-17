package com.cn.hzm.factory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.FactoryOrderDO;
import com.cn.hzm.factory.dao.FactoryOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:43 下午
 */
@Service
public class FactoryOrderService {

    @Autowired
    private FactoryOrderMapper factoryOrderMapper;

    public FactoryOrderDO getOrderById(Integer id){
        return factoryOrderMapper.selectById(id);
    }

    public List<FactoryOrderDO> getOrderByFactoryId(Integer fId){
        QueryWrapper<FactoryOrderDO> query = new QueryWrapper<>();
        query.eq("factory_id", fId);
        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    public List<FactoryOrderDO> getOrderByStatus(Integer orderStatus){
        QueryWrapper<FactoryOrderDO> query = new QueryWrapper<>();
        query.eq("order_status", orderStatus);
        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    /**
     * 创建商品
     * @param factoryOrderDO
     */
    public void createFactoryOrder(FactoryOrderDO factoryOrderDO){
        factoryOrderDO.setUtime(new Date());
        factoryOrderDO.setCtime(new Date());
        factoryOrderMapper.insert(factoryOrderDO);
    }

    /**
     * 更新商品
     * @param factoryOrderDO
     */
    public void updateFactoryOrder(FactoryOrderDO factoryOrderDO){
        factoryOrderDO.setUtime(new Date());
        factoryOrderMapper.updateById(factoryOrderDO);
    }
}
