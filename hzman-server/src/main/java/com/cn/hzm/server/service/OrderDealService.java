package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.entity.OrderDO;
import com.cn.hzm.core.entity.OrderItemDO;
import com.cn.hzm.order.service.OrderItemService;
import com.cn.hzm.order.service.OrderService;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.task.OrderSpiderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 1:57 下午
 */
@Component
public class OrderDealService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderSpiderTask orderSpiderTask;

    public JSONObject processListOrder(OrderConditionDTO conditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(conditionDTO);
        List<OrderDO> list = orderService.getListByCondition(condition);

        List<OrderDTO> orderDTOS = conditionDTO.pageResult(list).stream().map(
                orderDO -> installRespOrderDTO(orderDO, orderItemService.getOrderByAmazonId(orderDO.getAmazonOrderId())))
                .collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data",JSONObject.toJSON(orderDTOS));
        return respJo;
    }

    /**
     * 根据amazonId获取订单，若本地数据库不存在，则从amazon服务端爬取
     *
     * @param amazonId
     */
    public OrderDTO searchByAmazonId(String amazonId) {
        OrderDO orderDO = orderService.getOrderByAmazonId(amazonId);
        if (orderDO == null) {
            try {
                orderSpiderTask.amazonIdSpiderTask(amazonId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            orderDO = orderService.getOrderByAmazonId(amazonId);
        }
        return installRespOrderDTO(orderDO, orderItemService.getOrderByAmazonId(amazonId));
    }

    private OrderDTO installRespOrderDTO(OrderDO orderDO, List<OrderItemDO> orderItemDOS) {
        OrderDTO orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), OrderDTO.class);
        orderDTO.setShippingAddressJson(JSONObject.parseObject(orderDO.getShippingAddress()));

        if (!CollectionUtils.isEmpty(orderItemDOS)) {
            orderDTO.setOrderItemDTOS(orderItemDOS.stream()
                    .map(orderItemDO -> JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), OrderItemDTO.class))
                    .collect(Collectors.toList()));
        }
        return orderDTO;
    }
}
