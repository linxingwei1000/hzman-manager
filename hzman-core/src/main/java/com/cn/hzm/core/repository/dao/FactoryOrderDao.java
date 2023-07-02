package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FactoryOrderDo;
import com.cn.hzm.core.repository.mapper.FactoryOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:43 下午
 */
@Service
public class FactoryOrderDao {

    @Autowired
    private FactoryOrderMapper factoryOrderMapper;

    public FactoryOrderDo getOrderById(Integer id) {
        return factoryOrderMapper.selectById(id);
    }

    public List<FactoryOrderDo> getOrderByFactoryId(Integer fId) {
        QueryWrapper<FactoryOrderDo> query = new QueryWrapper<>();
        query.eq("factory_id", fId);
        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    public List<FactoryOrderDo> getOrderByStatus(Integer orderStatus) {
        QueryWrapper<FactoryOrderDo> query = new QueryWrapper<>();
        query.eq("order_status", orderStatus);
        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    public List<FactoryOrderDo> getOrderByStatusAndFactory(Integer orderStatus, Integer fId) {
        QueryWrapper<FactoryOrderDo> query = new QueryWrapper<>();
        if (orderStatus != null) {
            query.eq("order_status", orderStatus);
        }

        if (fId != null) {
            query.eq("factory_id", fId);
        }

        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    public List<FactoryOrderDo> getOrder() {
        QueryWrapper<FactoryOrderDo> query = new QueryWrapper<>();
        query.orderByDesc("ctime");
        return factoryOrderMapper.selectList(query);
    }

    /**
     * 创建商品
     *
     * @param factoryOrderDO
     */
    public void createFactoryOrder(FactoryOrderDo factoryOrderDO) {
        factoryOrderDO.setUtime(new Date());
        factoryOrderDO.setCtime(new Date());
        factoryOrderMapper.insert(factoryOrderDO);
    }

    /**
     * 更新商品
     *
     * @param factoryOrderDO
     */
    public void updateFactoryOrder(FactoryOrderDo factoryOrderDO) {
        factoryOrderDO.setUtime(new Date());
        factoryOrderMapper.updateById(factoryOrderDO);
    }

    public Integer deleteFactoryOrder(Integer oId) {
        return factoryOrderMapper.deleteById(oId);
    }
}
