package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.repository.dao.AmazonOrderDao;
import com.cn.hzm.core.repository.entity.AmazonOrderDo;
import com.cn.hzm.api.dto.OrderConditionDto;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/4 11:26 上午
 */
@Service
public class AmazonOrderService {

    @Autowired
    private AmazonOrderDao amazonOrderDao;

    public JSONObject processListOrder(OrderConditionDto conditionDTO) throws ParseException {
        Map<String, String> conditionMap = Maps.newHashMap();
        conditionMap.put("order_status", ContextConst.AMAZON_STATUS_PENDING);
        List<AmazonOrderDo> list = amazonOrderDao.getListByCondition(ThreadLocalCache.getUser().getUserMarketId(), conditionMap,
               new String[]{"id", "amazon_order_id", "purchase_date", "order_status"});

        List<AmazonOrderDo> orderDTOS = conditionDTO.pageResult(list).stream().map(
                orderDO -> JSONObject.parseObject(JSONObject.toJSONString(orderDO), AmazonOrderDo.class))
                .collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data",JSONObject.toJSON(orderDTOS));
        return respJo;
    }

    public Boolean localDeleteAmazonOrder(String amazonOrderDbId){
        AmazonOrderDo old = amazonOrderDao.getOrderByAmazonId(ThreadLocalCache.getUser().getUserMarketId(), amazonOrderDbId);

        old.setOrderStatus(ContextConst.AMAZON_STATUS_DELETE);
        return amazonOrderDao.updateOrder(old);
    }
}
