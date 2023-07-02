package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AmazonOrderDo;
import com.cn.hzm.core.repository.mapper.AmazonOrderMapper;
import com.cn.hzm.core.util.TimeUtil;
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
public class AmazonOrderDao {

    @Autowired
    private AmazonOrderMapper amazonOrderMapper;

    public List<AmazonOrderDo> getListByCondition(Integer awsUserMarketId, Map<String, String> condition, String[] fields) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.eq("user_market_id", awsUserMarketId);
        String orderStatus = condition.remove("order_status");
        if (!StringUtils.isEmpty(orderStatus)) {
            query.eq("order_status", orderStatus);
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
        return amazonOrderMapper.selectList(query);
    }

    public List<AmazonOrderDo> getOrdersByOrderStatus(Integer awsUserMarketId, String orderStatus) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.eq("user_market_id", awsUserMarketId);
        query.eq("order_status", orderStatus);
        query.orderByAsc("purchase_date");
        //query.last(String.format("limit %d,%d", offset, limit));
        return amazonOrderMapper.selectList(query);
    }

    public List<AmazonOrderDo> getOrdersByOrderStatusAndFinanceStatus(Integer awsUserMarketId, String orderStatus, Integer financeStatus, Integer limit) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.eq("user_market_id", awsUserMarketId);
        query.eq("order_status", orderStatus);
        query.eq("is_finance", financeStatus);
        query.orderByAsc("id");
        query.last(String.format("limit %d", limit));
        return amazonOrderMapper.selectList(query);
    }

    public List<AmazonOrderDo> getOrdersByAmazonIds(List<String> amazonOrderIds) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.in("amazon_order_id", amazonOrderIds);
        return amazonOrderMapper.selectList(query);
    }

    public List<AmazonOrderDo> getOrdersByPurchaseDate(Integer awsUserMarketId, Date startDate, Date endDate, String orderStatus, String[] fields) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.eq("user_market_id", awsUserMarketId);
        if (!StringUtils.isEmpty(orderStatus)) {
            query.eq("order_status", orderStatus);
        }
        query.between("purchase_date", startDate, endDate);
        query.select(fields);
        query.orderByAsc("purchase_date");
        return amazonOrderMapper.selectList(query);
    }

    public AmazonOrderDo getById(Integer id) {
        return amazonOrderMapper.selectById(id);
    }

    public AmazonOrderDo getOrderByAmazonId(Integer userMarketId, String amazonOrderId) {
        QueryWrapper<AmazonOrderDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("amazon_order_id", amazonOrderId);
        return amazonOrderMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param orderDO
     */
    public Boolean createOrder(AmazonOrderDo orderDO) {
        orderDO.setUtime(new Date());
        orderDO.setCtime(new Date());
        return amazonOrderMapper.insert(orderDO) != 0;
    }

    /**
     * 更新商品
     *
     * @param orderDO
     */
    public Boolean updateOrder(AmazonOrderDo orderDO) {
        orderDO.setUtime(new Date());
        return amazonOrderMapper.updateById(orderDO) != 0;
    }
}
