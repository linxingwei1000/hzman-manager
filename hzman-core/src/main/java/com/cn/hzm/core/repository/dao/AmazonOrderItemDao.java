package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AmazonOrderItemDo;
import com.cn.hzm.core.repository.mapper.AmazonOrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class AmazonOrderItemDao {

    @Autowired
    private AmazonOrderItemMapper amazonOrderItemMapper;

    public List<AmazonOrderItemDo> getOrderByAmazonId(String amazonOrderId) {
        QueryWrapper<AmazonOrderItemDo> query = new QueryWrapper<>();
        query.eq("amazon_order_id", amazonOrderId);
        return amazonOrderItemMapper.selectList(query);
    }

    public List<AmazonOrderItemDo> getOrderByBathAmazonId(List<String> amazonOrderIds) {
        QueryWrapper<AmazonOrderItemDo> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return amazonOrderItemMapper.selectList(query);
    }

    public AmazonOrderItemDo getOrderItemByOrderItemId(String orderItemId) {
        QueryWrapper<AmazonOrderItemDo> query = new QueryWrapper<>();
        query.eq("order_item_id", orderItemId);
        return amazonOrderItemMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param orderItemDO
     */
    public Boolean createOrderItem(AmazonOrderItemDo orderItemDO) {
        orderItemDO.setUtime(new Date());
        orderItemDO.setCtime(new Date());
        return amazonOrderItemMapper.insert(orderItemDO) != 0;
    }

    /**
     * 更新商品
     *
     * @param orderItemDO
     */
    public Boolean updateOrderItem(AmazonOrderItemDo orderItemDO) {
        orderItemDO.setUtime(new Date());
        return amazonOrderItemMapper.updateById(orderItemDO) != 0;
    }
}
