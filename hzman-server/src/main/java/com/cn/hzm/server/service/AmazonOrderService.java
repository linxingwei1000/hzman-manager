package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.dto.OrderConditionDTO;
import com.cn.hzm.server.dto.OrderDTO;
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
    private OrderService orderService;

    public JSONObject processListOrder(OrderConditionDTO conditionDTO) throws ParseException {
        Map<String, String> conditionMap = Maps.newHashMap();
        conditionMap.put("order_status", ContextConst.AMAZON_STATUS_PENDING);
        List<OrderDO> list = orderService.getListByCondition(conditionMap,
               new String[]{"id", "amazon_order_id", "purchase_date", "order_status"});

        List<OrderDTO> orderDTOS = conditionDTO.pageResult(list).stream().map(
                orderDO -> JSONObject.parseObject(JSONObject.toJSONString(orderDO), OrderDTO.class))
                .collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data",JSONObject.toJSON(orderDTOS));
        return respJo;
    }

    public Boolean localDeleteAmazonOrder(String amazonOrderDbId){
        OrderDO old = orderService.getOrderByAmazonId(amazonOrderDbId);

        old.setOrderStatus(ContextConst.AMAZON_STATUS_DELETE);
        return orderService.updateOrder(old);
    }
}
