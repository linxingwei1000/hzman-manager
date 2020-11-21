package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.entity.FactoryDO;
import com.cn.hzm.core.entity.FactoryOrderDO;
import com.cn.hzm.core.entity.InventoryDO;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.factory.enums.OrderStatusEnum;
import com.cn.hzm.factory.service.FactoryOrderService;
import com.cn.hzm.factory.service.FactoryService;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.stock.service.InventoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:50 下午
 */
@Service
public class FactoryDealService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FactoryOrderService factoryOrderService;

    @Resource(name = "backTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    public JSONObject processFactoryList(FactoryConditionDTO factoryConditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(factoryConditionDTO);
        List<FactoryDO> list = factoryService.getListByCondition(condition);

        List<FactoryDTO> factoryDTOS = factoryConditionDTO.pageResult(list).stream().map(factoryDO -> {
            FactoryDTO factoryDTO = JSONObject.parseObject(JSONObject.toJSONString(factoryDO), FactoryDTO.class);
            List<FactoryOrderDO> orderDOS = factoryOrderService.getOrderByFactoryId(factoryDO.getId());
            factoryDTO.setOrderList(orderDOS.stream().map(orderDO -> {
                FactoryOrderDTO orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDTO.class);
                orderDTO.setStatus(OrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());

                ItemDO itemDO = itemService.getItemDOBySku(orderDO.getSku());
                orderDTO.setTitle(itemDO.getTitle());
                orderDTO.setIcon(itemDO.getIcon());
                return orderDTO;
            }).collect(Collectors.toList()));
            return factoryDTO;
        }).collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data", JSONObject.toJSON(factoryDTOS));
        return respJo;
    }

    public void dealFactory(FactoryDTO factoryDTO, Boolean isCreateMode) throws Exception {
        if (StringUtils.isEmpty(factoryDTO.getFactoryName())) {
            throw new RuntimeException("厂家名为空");
        }

        FactoryDO old = factoryService.getByName(factoryDTO.getFactoryName());
        if (old != null) {
            if (isCreateMode) {
                throw new Exception("厂家已存在");
            } else {
                if (!old.getId().equals(factoryDTO.getId())) {
                    throw new Exception("厂家已存在");
                }
            }
        }

        if (StringUtils.isEmpty(factoryDTO.getContactPerson())) {
            throw new RuntimeException("联系人为空");
        }
        FactoryDO factoryDO = JSONObject.parseObject(JSONObject.toJSONString(factoryDTO), FactoryDO.class);

        if (isCreateMode) {
            factoryService.createFactory(factoryDO);
        } else {
            factoryService.updateFactory(factoryDO);
        }
    }

    /**
     * todo 待定删除功能
     *
     * @param fId
     */
    public void deleteFactory(Integer fId) {
        factoryService.deleteFactory(fId);
    }

    /**
     * 创建厂家订单
     */
    public Integer createOrder(Integer factoryId, String sku, Integer orderNum, String remark) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();

        if (factoryId == null || factoryService.getByFid(factoryId) == null) {
            throw new RuntimeException("厂家参数非法");
        }
        factoryOrderDO.setFactoryId(factoryId);


        if (sku == null || itemService.getItemDOBySku(sku) == null) {
            throw new RuntimeException("商品参数非法");
        }
        factoryOrderDO.setSku(sku);

        if (orderNum == null || orderNum == 0) {
            throw new RuntimeException("商品数量参数非法");
        }
        factoryOrderDO.setOrderNum(orderNum);
        factoryOrderDO.setRemark(remark);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_START.getCode());
        factoryOrderService.createFactoryOrder(factoryOrderDO);
        return factoryOrderDO.getId();
    }

    public void factoryConfirmOrder(Integer oId, Double itemPrice, String deliveryDate) {
        FactoryOrderDO old = factoryOrderService.getOrderById(oId);
        if (old.getOrderStatus() > OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode()) {
            throw new RuntimeException("当前订单状态不能修改");
        }

        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setItemPrice(itemPrice);
        factoryOrderDO.setDeliveryDate(deliveryDate);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void hzmConfirm(Integer oId) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_CONFIRM.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void delivery(Integer oId, String waybillNum) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setWaybillNum(waybillNum);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void complete(Integer oId, Integer receiveNum) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setReceiveNum(receiveNum);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_DELIVERY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);

        //同步库存信息
        executor.execute(() -> {
            FactoryOrderDO temp = factoryOrderService.getOrderById(oId);
            InventoryDO inventoryDO = inventoryService.getInventoryBySku(temp.getSku());

            Integer localNum = inventoryDO.getLocalQuantity() == null ? 0 : inventoryDO.getLocalQuantity();
            inventoryDO.setLocalQuantity(localNum + receiveNum);
            inventoryService.updateInventory(inventoryDO);
        });

    }

    public void pay(Integer oId, String paymentVoucher) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setPaymentVoucher(paymentVoucher);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_PAY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

}
