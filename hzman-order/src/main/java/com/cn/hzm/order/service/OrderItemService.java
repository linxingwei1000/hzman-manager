package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.core.util.SqlCommonUtil;
import com.cn.hzm.order.dao.OrderItemMapper;
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
public class OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    public List<OrderItemDO> getOrderByAmazonId(String amazonOrderId) {
        QueryWrapper<OrderItemDO> query = new QueryWrapper<>();
        query.eq("amazon_order_id", amazonOrderId);
        return orderItemMapper.selectList(query);
    }

    public List<OrderItemDO> getOrderByBathAmazonId(List<String> amazonOrderIds) {
        QueryWrapper<OrderItemDO> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return orderItemMapper.selectList(query);
    }

    public OrderItemDO getOrderItemByOrderItemId(String orderItemId) {
        QueryWrapper<OrderItemDO> query = new QueryWrapper<>();
        query.eq("order_item_id", orderItemId);
        return orderItemMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param orderItemDO
     */
    public Boolean createOrderItem(OrderItemDO orderItemDO) {
        orderItemDO.setUtime(new Date());
        orderItemDO.setCtime(new Date());
        return orderItemMapper.insert(orderItemDO) != 0;
    }

    /**
     * 更新商品
     *
     * @param orderItemDO
     */
    public Boolean updateOrderItem(OrderItemDO orderItemDO) {
        orderItemDO.setUtime(new Date());
        return orderItemMapper.updateById(orderItemDO) != 0;
    }
}
