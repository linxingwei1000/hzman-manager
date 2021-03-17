package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.order.dao.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:55 下午
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public List<OrderDO> getListByCondition(Map<String, String> condition, String[] fields) throws ParseException {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();

        String purchaseDate = condition.remove("purchaseDate");
        if (!StringUtils.isEmpty(purchaseDate)) {
            Date date = TimeUtil.getDateBySimple(purchaseDate);
            Date nextDate = TimeUtil.dateFixByDay(date, 1, 0, 0);
            query.between("purchase_date", date, nextDate);
        }

        String purchaseDateBegin = condition.remove("purchaseDateBegin");
        if (!StringUtils.isEmpty(purchaseDateBegin)) {
            Date date = TimeUtil.transform(purchaseDateBegin);
            log.info("purchaseDateBegin:{}", date);
            query.ge("purchase_date", date);
        }

        String purchaseDateEnd = condition.remove("purchaseDateEnd");
        if (!StringUtils.isEmpty(purchaseDateEnd)) {
            Date date = TimeUtil.transform(purchaseDateEnd);
            log.info("purchaseDateEnd:{}", date);
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
        query.select(fields);
        query.orderByAsc("purchase_date");
        return orderMapper.selectList(query);
    }

    public List<OrderDO> getOrdersByOrderStatus(String orderStatus) {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        query.eq("order_status", orderStatus);
        query.orderByAsc("purchase_date");
        //query.last(String.format("limit %d,%d", offset, limit));
        return orderMapper.selectList(query);
    }

    public List<OrderDO> getOrdersByAmazonIds(List<String> amazonOrderIds) {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return orderMapper.selectList(query);
    }

    public List<OrderDO> getOrdersByPurchaseDate(Date startDate, Date endDate, String orderStatus, String[] fields) {
        QueryWrapper<OrderDO> query = new QueryWrapper<>();
        if (!StringUtils.isEmpty(orderStatus)) {
            query.eq("order_status", orderStatus);
        }
        query.between("purchase_date", startDate, endDate);
        query.select(fields);
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
