package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.OrderFinanceDO;
import com.cn.hzm.order.dao.OrderFinanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class OrderFinanceService {

    @Autowired
    private OrderFinanceMapper orderFinanceMapper;

    public OrderFinanceDO getOrderFinanceByAmazonId(String amazonOrderId) {
        QueryWrapper<OrderFinanceDO> query = new QueryWrapper<>();
        query.eq("amazon_order_id", amazonOrderId);
        return orderFinanceMapper.selectOne(query);
    }

    public List<OrderFinanceDO> getOrderFinanceByBathAmazonId(List<String> amazonOrderIds) {
        QueryWrapper<OrderFinanceDO> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return orderFinanceMapper.selectList(query);
    }
//
//    public OrderItemDO getOrderItemByOrderItemId(String orderItemId) {
//        QueryWrapper<OrderItemDO> query = new QueryWrapper<>();
//        query.eq("order_item_id", orderItemId);
//        return orderItemMapper.selectOne(query);
//    }

    /**
     * 创建订单财务
     *
     * @param orderFinanceDO
     */
    public Boolean createOrderFinance(OrderFinanceDO orderFinanceDO) {
        orderFinanceDO.setUtime(new Date());
        orderFinanceDO.setCtime(new Date());
        return orderFinanceMapper.insert(orderFinanceDO) != 0;
    }

    /**
     * 更新订单财务
     *
     * @param orderFinanceDO
     */
    public Boolean updateOrderFinance(OrderFinanceDO orderFinanceDO) {
        orderFinanceDO.setUtime(new Date());
        return orderFinanceMapper.updateById(orderFinanceDO) != 0;
    }
}
