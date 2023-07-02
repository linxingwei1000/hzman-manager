package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AmazonOrderFinanceDo;
import com.cn.hzm.core.repository.mapper.AmazonOrderFinanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class AmazonOrderFinanceDao {

    @Autowired
    private AmazonOrderFinanceMapper amazonOrderFinanceMapper;

    public AmazonOrderFinanceDo getOrderFinanceByAmazonId(String amazonOrderId) {
        QueryWrapper<AmazonOrderFinanceDo> query = new QueryWrapper<>();
        query.eq("amazon_order_id", amazonOrderId);
        return amazonOrderFinanceMapper.selectOne(query);
    }

    public List<AmazonOrderFinanceDo> getOrderFinanceByBathAmazonId(List<String> amazonOrderIds) {
        QueryWrapper<AmazonOrderFinanceDo> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return amazonOrderFinanceMapper.selectList(query);
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
     * @param amazonOrderFinanceDO
     */
    public Boolean createOrderFinance(AmazonOrderFinanceDo amazonOrderFinanceDO) {
        amazonOrderFinanceDO.setUtime(new Date());
        amazonOrderFinanceDO.setCtime(new Date());
        return amazonOrderFinanceMapper.insert(amazonOrderFinanceDO) != 0;
    }

    /**
     * 更新订单财务
     *
     * @param amazonOrderFinanceDO
     */
    public Boolean updateOrderFinance(AmazonOrderFinanceDo amazonOrderFinanceDO) {
        amazonOrderFinanceDO.setUtime(new Date());
        return amazonOrderFinanceMapper.updateById(amazonOrderFinanceDO) != 0;
    }
}
