package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.dao.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public List<OrderDO> getListByCondition(Map<String, String> condition) {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        if (condition.size() != 0) {
        }

        String purchaseDate = condition.remove("purchaseDate");
        if (!StringUtils.isEmpty(purchaseDate)) {
            Date date = TimeUtil.getDateBySimple(purchaseDate);
            Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
            query.between("purchase_date", date, nextDate);
        }

        String purchaseDateBegin = condition.remove("purchaseDateBegin");
        if (!StringUtils.isEmpty(purchaseDateBegin)) {
            Date date = TimeUtil.getDateBySimple(purchaseDateBegin);
            query.ge("purchase_date", date);
        }

        String purchaseDateEnd = condition.remove("purchaseDateEnd");
        if (!StringUtils.isEmpty(purchaseDateEnd)) {
            Date date = TimeUtil.getDateBySimple(purchaseDateEnd);
            query.le("purchase_date", date);
        }

        String lastUpdateDate = condition.remove("lastUpdateDate");
        if (!StringUtils.isEmpty(lastUpdateDate)) {
            Date date = TimeUtil.getDateBySimple(lastUpdateDate);
            Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
            query.between("last_update_date", date, nextDate);
        }

        String lastUpdateDateBegin = condition.remove("lastUpdateDateBegin");
        if (!StringUtils.isEmpty(lastUpdateDateBegin)) {
            Date date = TimeUtil.getDateBySimple(lastUpdateDateBegin);
            query.ge("last_update_date", date);
        }

        String lastUpdateDateEnd = condition.remove("lastUpdateDateEnd");
        if (!StringUtils.isEmpty(lastUpdateDateEnd)) {
            Date date = TimeUtil.getDateBySimple(lastUpdateDateEnd);
            query.le("last_update_date", date);
        }

        String buyerName = condition.remove("buyerName");
        if (!StringUtils.isEmpty(buyerName)) {
            query.like("buyer_name", buyerName);
        }

        condition.remove("pageNum");
        condition.remove("pageSize");
        condition.forEach((k, v) -> {
            if (!StringUtils.isEmpty(v)) {
                query.eq(k, v);
            }
        });

        query.orderByAsc("ctime");
        return orderMapper.selectList(query);
    }

    public List<OrderDO> getOrdersByPurchaseDate(Date startDate, Date endDate){
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        query.between("purchase_date", startDate, endDate);
        query.orderByAsc("purchase_date");
        return orderMapper.selectList(query);
    }

    public OrderDO getById(Integer id) {
        return orderMapper.selectById(id);
    }

    public OrderDO getOrderByAmazonId(String amazonOrderId) {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        query.eq("amazon_order_id", amazonOrderId);
        return orderMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param orderDO
     */
    public Boolean createOrder(OrderDO orderDO) {
        orderDO.setUtime(new Date());
        orderDO.setCtime(new Date());
        return orderMapper.insert(orderDO) != 0;
    }

    /**
     * 更新商品
     *
     * @param orderDO
     */
    public Boolean updateOrder(OrderDO orderDO) {
        orderDO.setUtime(new Date());
        return orderMapper.updateById(orderDO) != 0;
    }
}
