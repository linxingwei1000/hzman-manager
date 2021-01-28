package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.entity.*;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.factory.enums.OrderStatusEnum;
import com.cn.hzm.factory.service.FactoryItemService;
import com.cn.hzm.factory.service.FactoryOrderItemService;
import com.cn.hzm.factory.service.FactoryOrderService;
import com.cn.hzm.factory.service.FactoryService;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.cache.ItemDetailCache;
import com.cn.hzm.server.domain.HzmUserRole;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.meta.HzmRoleType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private FactoryOrderService factoryOrderService;

    @Autowired
    private FactoryOrderItemService factoryOrderItemService;

    @Autowired
    private FactoryItemService factoryItemService;

    @Autowired
    private ItemDetailCache itemDetailCache;

    @Autowired
    private ItemDealService itemDealService;

    @Resource(name = "backTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    private static final String HIDE_INFO = "*******";

    public JSONObject processFactoryList(FactoryConditionDTO factoryConditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(factoryConditionDTO);
        List<FactoryDO> list = factoryService.getListByCondition(condition);

        List<FactoryDTO> factoryDTOS = factoryConditionDTO.pageResult(list).stream().map(factoryDO -> {
            FactoryDTO factoryDTO = roleControlFactoryInfo(factoryDO);
            List<FactoryOrderDO> orderDOS = factoryOrderService.getOrderByFactoryId(factoryDO.getId());
            factoryDTO.setOrderList(orderDOS.stream().map(orderDO -> {
                FactoryOrderDTO orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDTO.class);
                orderDTO.setStatus(orderDO.getOrderStatus());
                orderDTO.setStatusDesc(OrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());

                List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(orderDO.getId());
                orderDTO.setOrderItems(orderItemDOS.stream().map(orderItemDO -> {
                    FactoryOrderItemDTO orderItemDTO = JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), FactoryOrderItemDTO.class);
                    ItemDO itemDO = itemService.getItemDOBySku(orderItemDO.getSku());
                    orderItemDTO.setTitle(itemDO.getTitle());
                    orderItemDTO.setIcon(itemDO.getIcon());
                    return orderItemDTO;
                }).collect(Collectors.toList()));
                return orderDTO;
            }).collect(Collectors.toList()));
            return factoryDTO;
        }).collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data", JSONObject.toJSON(factoryDTOS));
        return respJo;
    }

    private FactoryDTO roleControlFactoryInfo(FactoryDO factoryDO) {
        FactoryDTO factoryDTO;
        Set<String> roleSet = HzmContext.get().getRoles();
        if (!roleSet.contains(HzmRoleType.ROLE_ADMIN.getRoleId())) {
            factoryDTO = new FactoryDTO();
            factoryDTO.setFactoryName(factoryDO.getFactoryName());
            factoryDTO.setAddress(HIDE_INFO);
            factoryDTO.setCollectMethod(HIDE_INFO);
            factoryDTO.setContactPerson(HIDE_INFO);
            factoryDTO.setContactInfo(HIDE_INFO);
            factoryDTO.setWx(HIDE_INFO);
        } else {
            factoryDTO = JSONObject.parseObject(JSONObject.toJSONString(factoryDO), FactoryDTO.class);
        }
        return factoryDTO;
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
     * 厂家认领商品
     *
     * @param factoryId
     * @param sku
     */
    public void factoryClaimItem(Integer factoryId, String sku, String desc) {

        FactoryItemDO factoryItemDO = new FactoryItemDO();
        factoryItemDO.setFactoryId(factoryId);
        factoryItemDO.setSku(sku);
        factoryItemDO.setItemDesc(desc);

        List<FactoryOrderDO> orders = factoryOrderService.getOrderByFactoryId(factoryId);
        if (!CollectionUtils.isEmpty(orders)) {
            //从厂家确认订单开始
            Set<Integer> orderIds = orders.stream()
                    .filter(order -> order.getOrderStatus() >= OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode())
                    .map(FactoryOrderDO::getId)
                    .collect(Collectors.toSet());

            List<FactoryOrderItemDO> orderItems = factoryOrderItemService.getOrderBySku(sku);
            for (FactoryOrderItemDO orderItemDO : orderItems) {
                if (orderIds.contains(orderItemDO.getFactoryOrderId())) {
                    factoryItemDO.setFactoryPrice(orderItemDO.getItemPrice());
                    break;
                }
            }
        }

        FactoryItemDO old = factoryItemService.getInfoBySkuAndFactoryId(sku, factoryId);
        if (old != null) {
            factoryItemDO.setId(old.getId());
            factoryItemService.updateFactoryItem(factoryItemDO);
        } else {
            factoryItemService.createFactoryItem(factoryItemDO);
        }
        //刷新缓存
        itemDetailCache.refreshCache(sku);
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
     * 获取厂家外链订单
     *
     * @param factoryOrderConditionDTO
     * @return
     */
    public JSONObject outOrderList(FactoryOrderConditionDTO factoryOrderConditionDTO) {
        List<FactoryOrderDO> orderDOS = factoryOrderService.getOrderByFactoryId(factoryOrderConditionDTO.getFactoryId());
        List<FactoryOrderDO> needDealOrders = orderDOS.stream().filter(order -> OrderStatusEnum.ORDER_CONFIRM_PLACE.getCode().equals(order.getOrderStatus())
                || OrderStatusEnum.ORDER_CONFIRM.getCode().equals(order.getOrderStatus())
                || OrderStatusEnum.ORDER_PAY.getCode().equals(order.getOrderStatus())).collect(Collectors.toList());
        return processOrderList(needDealOrders, factoryOrderConditionDTO);
    }


    private JSONObject processOrderList(List<FactoryOrderDO> needDealOrders, FactoryOrderConditionDTO condition) {
        //角色过滤厂家订单
        Set<String> roleSet = HzmContext.get().getRoles();
        if (roleSet.size() == 1 && roleSet.contains(HzmRoleType.ROLE_FACTORY.getRoleId())) {
            needDealOrders = needDealOrders.stream().filter(order -> OrderStatusEnum.ORDER_CONFIRM_PLACE.getCode().equals(order.getOrderStatus())
                    || OrderStatusEnum.ORDER_CONFIRM.getCode().equals(order.getOrderStatus())
                    || OrderStatusEnum.ORDER_PAY.getCode().equals(order.getOrderStatus())).collect(Collectors.toList());
        }

        List<FactoryOrderDTO> dtos = condition.pageResult(needDealOrders).stream().map(orderDO -> {
            FactoryOrderDTO orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDTO.class);
            orderDTO.setStatus(orderDO.getOrderStatus());
            orderDTO.setStatusDesc(OrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());
            orderDTO.setOrderDesc(orderDO.getOrderDesc());
            orderDTO.setOrderCreateTime(TimeUtil.getDateFormat(orderDO.getCtime()));

            List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(orderDO.getId());
            orderDTO.setOrderItems(orderItemDOS.stream().map(orderItemDO -> {
                FactoryOrderItemDTO orderItemDTO = JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), FactoryOrderItemDTO.class);
                ItemDO itemDO = itemService.getItemDOBySku(orderItemDO.getSku());
                orderItemDTO.setTitle(itemDO.getTitle());
                orderItemDTO.setIcon(itemDO.getIcon());
                return orderItemDTO;
            }).collect(Collectors.toList()));
            return orderDTO;
        }).collect(Collectors.toList());

        JSONObject respJo = new JSONObject();
        respJo.put("total", needDealOrders.size());
        respJo.put("data", JSONObject.toJSON(dtos));
        return respJo;
    }


    /**
     * 获取所有订单
     *
     * @param factoryOrderConditionDTO
     * @return
     */
    public JSONObject orderList(FactoryOrderConditionDTO factoryOrderConditionDTO) {
        List<FactoryOrderDO> orderDOS = factoryOrderService.getOrderByFactoryId(factoryOrderConditionDTO.getFactoryId());
        return processOrderList(orderDOS, factoryOrderConditionDTO);
    }

    public JSONArray getOrderByStatus(Integer orderStatus) {
        List<FactoryOrderDO> orderDOS = factoryOrderService.getOrderByStatus(orderStatus);
        JSONArray ja = new JSONArray();
        if (!CollectionUtils.isEmpty(orderDOS)) {
            orderDOS.forEach(order -> {
                JSONObject jo = new JSONObject();
                jo.put("orderId", order.getId());
                jo.put("orderDesc", order.getOrderDesc());
                ja.add(jo);
            });
        }
        return ja;
    }

    /**
     * 创建厂家订单
     *
     * @return
     */
    @Transactional
    public Integer createOrder(CreateFactoryOrderDTO createFactoryOrderDTO) {
        if (createFactoryOrderDTO.getFactoryId() == null || factoryService.getByFid(createFactoryOrderDTO.getFactoryId()) == null) {
            throw new RuntimeException("厂家参数非法");
        }

        //创建工厂订单
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setFactoryId(createFactoryOrderDTO.getFactoryId());
        factoryOrderDO.setOrderDesc(createFactoryOrderDTO.getDesc());
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_START.getCode());
        factoryOrderService.createFactoryOrder(factoryOrderDO);

        List<CreateFactoryOrderItemDTO> orderItems = createFactoryOrderDTO.getOrderItems();
        for (CreateFactoryOrderItemDTO orderItemDTO : orderItems) {
            FactoryOrderItemDO orderItemDO = new FactoryOrderItemDO();
            orderItemDO.setFactoryOrderId(factoryOrderDO.getId());
            orderItemDO.setSku(orderItemDTO.getSku());
            orderItemDO.setOrderNum(orderItemDTO.getOrderNum());
            orderItemDO.setRemark(orderItemDTO.getRemark());
            factoryOrderItemService.createFactoryOrderItem(orderItemDO);
        }
        return 1;
    }


    /**
     * 删除订单
     *
     * @param orderId
     * @return
     */
    public Integer deleteOrder(Integer orderId) {
        FactoryOrderDO factoryOrderDO = factoryOrderService.getOrderById(orderId);
        if (factoryOrderDO.getOrderStatus() > OrderStatusEnum.ORDER_CONFIRM_PLACE.getCode()) {
            throw new HzmException(ExceptionCode.ORDER_DELETE_ILLEGAL);
        }

        //删除订单
        factoryOrderService.deleteFactoryOrder(orderId);

        //删除订单商品
        factoryOrderItemService.deleteByOrderId(orderId);

        return 1;
    }

    /**
     * 修改厂家订单
     *
     * @return
     */
    @Transactional
    public Integer modOrderItem(List<FactoryOrderItemDTO> orderItems) {
        for (FactoryOrderItemDTO orderItemDTO : orderItems) {
            FactoryOrderItemDO orderItemDO = new FactoryOrderItemDO();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setSku(orderItemDTO.getSku());
            orderItemDO.setOrderNum(orderItemDTO.getOrderNum());
            orderItemDO.setRemark(orderItemDTO.getRemark());
            factoryOrderItemService.updateFactoryOrder(orderItemDO);
        }
        return 1;
    }

    /**
     * 添加厂家订单
     */
    @Transactional
    public Integer addOrderItem(AddFactoryOrderDTO addFactoryOrderDTO) {
        addFactoryOrderDTO.getOrderItems().forEach(orderItem -> {
            FactoryOrderItemDO old = factoryOrderItemService.getItemByOrderIdAndSku(addFactoryOrderDTO.getFactoryOrderId(), orderItem.getSku());
            if (old != null) {
                old.setOrderNum(orderItem.getOrderNum() + old.getOrderNum());
                old.setRemark(orderItem.getRemark());
                factoryOrderItemService.updateFactoryOrder(old);
            } else {
                FactoryOrderItemDO orderItemDO = new FactoryOrderItemDO();
                orderItemDO.setFactoryOrderId(addFactoryOrderDTO.getFactoryOrderId());
                orderItemDO.setSku(orderItem.getSku());
                orderItemDO.setOrderNum(orderItem.getOrderNum());
                orderItemDO.setRemark(orderItem.getRemark());
                factoryOrderItemService.createFactoryOrderItem(orderItemDO);
            }
        });

        return 1;
    }

    public void hzmConfirmPlace(Integer oId) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_CONFIRM_PLACE.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    @Transactional
    public void factoryConfirmOrder(List<FactoryOrderItemDTO> orderItems, Integer oId, String deliveryDate) {
        FactoryOrderDO old = factoryOrderService.getOrderById(oId);
        if (old.getOrderStatus() > OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode()) {
            throw new RuntimeException("当前订单状态不能修改");
        }

        if (StringUtils.isEmpty(deliveryDate)) {
            throw new RuntimeException("厂家交货日期必填");
        }

        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setDeliveryDate(deliveryDate);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);

        for (FactoryOrderItemDTO orderItemDTO : orderItems) {
            FactoryOrderItemDO orderItemDO = new FactoryOrderItemDO();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setItemPrice(orderItemDTO.getItemPrice());
            factoryOrderItemService.updateFactoryOrder(orderItemDO);

            //存在认领记录，更新最新订价
            FactoryItemDO factoryItemDO = factoryItemService.getInfoBySkuAndFactoryId(orderItemDO.getSku(), old.getFactoryId());
            if (factoryItemDO != null) {
                factoryItemDO.setFactoryPrice(orderItemDTO.getItemPrice());
                factoryItemService.updateFactoryItem(factoryItemDO);
            }
        }

        //刷新缓存
        executor.execute(() -> itemDetailCache.refreshCaches(orderItems.stream().map(FactoryOrderItemDTO::getSku).collect(Collectors.toList())));
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

    @Transactional
    public void complete(Integer oId, List<CreateFactoryOrderItemDTO> orderItems) {

        Map<String, Integer> skuMap = orderItems.stream().collect(Collectors.toMap(CreateFactoryOrderItemDTO::getSku, CreateFactoryOrderItemDTO::getOrderNum));
        List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(oId);
        orderItemDOS.forEach(orderItem -> {
            orderItem.setReceiveNum(skuMap.getOrDefault(orderItem.getSku(), 0));
            factoryOrderItemService.updateFactoryOrder(orderItem);
        });

        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_DELIVERY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);

        //同步库存信息
        executor.execute(() -> orderItemDOS.forEach(orderItem ->
                itemDealService.dealSkuInventory(orderItem.getSku(), "mod", orderItem.getReceiveNum())));
    }

    public void pay(Integer oId, String paymentVoucher) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setPaymentVoucher(paymentVoucher);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_PAY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

}
