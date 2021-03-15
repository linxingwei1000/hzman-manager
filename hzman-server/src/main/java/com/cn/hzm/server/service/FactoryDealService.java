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
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.meta.HzmRoleType;
import com.google.common.collect.Lists;
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

    private static final Double HIDE_PRICE_INFO = 999999.99;

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
     * 批量添加厂家商品信息
     *
     * @param factoryClaimDTOS
     */
    @Transactional
    public void factoryBatchClaimItem(List<FactoryClaimDTO> factoryClaimDTOS) {
        factoryClaimDTOS.forEach(dto -> factoryClaimItem(dto.getFactoryId(), dto.getSku(), dto.getDesc()));
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
     * 厂家商品信息删除
     *
     * @param itemInfoId
     * @return
     */
    public Integer deleteFactoryItem(Integer itemInfoId) {
        FactoryItemDO factoryItemDO = factoryItemService.getInfoById(itemInfoId);
        factoryItemService.deleteFactoryItem(itemInfoId);

        //刷新缓存
        itemDetailCache.refreshCache(factoryItemDO.getSku());

        return 1;
    }

    /**
     * @param fId
     */
    public void deleteFactory(Integer fId) {
        factoryService.deleteFactory(fId);
    }

    /**
     * 获取订单页面订单
     *
     * @param conditionDTO
     * @return
     */
    public JSONObject htmlOrderList(FactoryOrderConditionDTO conditionDTO) {
        List<FactoryOrderDO> list = factoryOrderService.getOrderByStatusAndFactory(conditionDTO.getOrderStatus(), conditionDTO.getFactoryId());
//        if (conditionDTO.getOrderStatus() == null && conditionDTO.getFactoryId() == null) {
//            list = list.stream().filter(order ->
//                    order.getOrderStatus().equals(OrderStatusEnum.ORDER_START.getCode()) ||
//                            order.getOrderStatus().equals(OrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode()) ||
//                            order.getOrderStatus().equals(OrderStatusEnum.ORDER_DELIVERY.getCode())).collect(Collectors.toList());
//        }
        return processOrderList(list, conditionDTO);
    }


    private JSONObject processOrderList(List<FactoryOrderDO> needDealOrders, FactoryOrderConditionDTO condition) {
        //角色过滤厂家订单
        Set<String> roleSet = HzmContext.get().getRoles();
        if (roleSet.size() == 1 && roleSet.contains(HzmRoleType.ROLE_FACTORY.getRoleId())) {
            needDealOrders = needDealOrders.stream()
                    .filter(order -> !OrderStatusEnum.ORDER_START.getCode().equals(order.getOrderStatus())).collect(Collectors.toList());
        }

        //过滤新建订单之后，可能订单列表为空
        if (CollectionUtils.isEmpty(needDealOrders)) {
            JSONObject respJo = new JSONObject();
            respJo.put("total", 0);
            respJo.put("data", new JSONArray());
            return respJo;
        }

        List<Integer> factoryIds = needDealOrders.stream().map(FactoryOrderDO::getFactoryId).collect(Collectors.toList());
        Map<Integer, FactoryDO> factorys = factoryService.getByIds(factoryIds).stream().collect(Collectors.toMap(FactoryDO::getId, order -> order));


        List<FactoryOrderDTO> dtos = condition.pageResult(needDealOrders).stream().map(orderDO -> {
            FactoryOrderDTO orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDTO.class);
            orderDTO.setStatus(orderDO.getOrderStatus());
            orderDTO.setStatusDesc(OrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());
            orderDTO.setFactoryName(factorys.get(orderDO.getFactoryId()).getFactoryName());
            orderDTO.setOrderDesc(orderDO.getOrderDesc());
            orderDTO.setOrderCreateTime(TimeUtil.getDateFormat(orderDO.getCtime()));

            List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(orderDO.getId());
            orderDTO.setOrderItems(orderItemDOS.stream().map(orderItemDO -> {
                FactoryOrderItemDTO orderItemDTO = JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), FactoryOrderItemDTO.class);
                if (roleSet.size() == 1 && roleSet.contains(HzmRoleType.ROLE_EMPLOYEE.getRoleId())) {
                    orderItemDTO.setItemPrice(HIDE_PRICE_INFO);
                    orderItemDTO.setTotalPrice(HIDE_PRICE_INFO);
                }

                //分批交货，计算剩余未交货数量
                int remainNum = orderItemDTO.getDeliveryNum() - (orderItemDTO.getReceiveNum() == null ? 0 : orderItemDTO.getReceiveNum());
                orderItemDTO.setRemainNum(remainNum);

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

        //check
        List<String> skus = Lists.newArrayList();
        createFactoryOrderDTO.getOrderItems().forEach(item -> checkOrderItem(item.getSku(), item.getOrderNum(), skus));

        //创建工厂订单
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setFactoryId(createFactoryOrderDTO.getFactoryId());
        factoryOrderDO.setOrderDesc(createFactoryOrderDTO.getDesc());
        factoryOrderDO.setReceiveAddress(createFactoryOrderDTO.getReceiveAddress());
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


    private void checkOrderItem(String sku, Integer num, List<String> skus) {
        if (StringUtils.isEmpty(sku)) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_ITEM_MUST);
        }

        if (skus.contains(sku)) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_ITEM_DUPLICATE);
        }
        skus.add(sku);

        if (num == null || num == 0) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_ITEM_NUM_MUST);
        }
    }

    /**
     * 修改厂家订单
     *
     * @return
     */
    @Transactional
    public Integer modOrderItem(ModFactoryOrderDTO modFactoryOrderDTO) {
        if (modFactoryOrderDTO.getFactoryId() == null || factoryService.getByFid(modFactoryOrderDTO.getFactoryId()) == null) {
            throw new RuntimeException("厂家参数非法");
        }

        List<String> skus = Lists.newArrayList();
        modFactoryOrderDTO.getOrderItems().forEach(item -> checkOrderItem(item.getSku(), item.getOrderNum(), skus));

        //创建工厂订单
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(modFactoryOrderDTO.getOrderId());
        factoryOrderDO.setOrderDesc(modFactoryOrderDTO.getDesc());
        factoryOrderDO.setReceiveAddress(modFactoryOrderDTO.getReceiveAddress());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);

        for (FactoryOrderItemDTO orderItemDTO : modFactoryOrderDTO.getOrderItems()) {
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

        //商品订单默认价格
        executor.execute(() -> {
            FactoryOrderDO dbOrder = factoryOrderService.getOrderById(oId);
            List<FactoryOrderItemDO> factoryOrderItems = factoryOrderItemService.getItemsByOrderId(oId);
            factoryOrderItems.forEach(orderItem -> {
                FactoryItemDO factoryItemDO = factoryItemService.getInfoBySkuAndFactoryId(orderItem.getSku(), dbOrder.getFactoryId());
                if (factoryItemDO != null) {
                    orderItem.setItemPrice(factoryItemDO.getFactoryPrice());
                    factoryOrderItemService.updateFactoryOrder(orderItem);
                }
            });
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void factoryConfirmOrder(List<FactoryOrderItemDTO> orderItems, Integer oId, String deliveryDate) {
        FactoryOrderDO old = factoryOrderService.getOrderById(oId);
        if (old.getOrderStatus() > OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode()) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_ILLEGAL);
        }

        if (StringUtils.isEmpty(deliveryDate)) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_CONFIRM_DATE_MUST);
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
            orderItemDO.setFactoryRemark(orderItemDTO.getFactoryRemark());
            factoryOrderItemService.updateFactoryOrder(orderItemDO);

            //存在认领记录，更新最新订价
            FactoryItemDO factoryItemDO = factoryItemService.getInfoBySkuAndFactoryId(orderItemDTO.getSku(), old.getFactoryId());
            if (factoryItemDO != null) {
                factoryItemDO.setFactoryPrice(orderItemDTO.getItemPrice());
                factoryItemService.updateFactoryItem(factoryItemDO);
            } else {
                factoryItemDO = new FactoryItemDO();
                factoryItemDO.setFactoryId(old.getFactoryId());
                factoryItemDO.setSku(orderItemDTO.getSku());
                factoryItemDO.setItemDesc("厂家订单自动同步");
                factoryItemDO.setFactoryPrice(orderItemDTO.getItemPrice());
                factoryItemService.createFactoryItem(factoryItemDO);
            }
        }

        //刷新缓存
        executor.execute(() -> itemDetailCache.refreshCaches(orderItems.stream().map(FactoryOrderItemDTO::getSku).collect(Collectors.toList())));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delivery(FactoryDeliveryDTO factoryDeliveryDTO) {

        for (FactoryOrderItemDTO orderItemDTO : factoryDeliveryDTO.getOrderItems()) {
            FactoryOrderItemDO orderItemDO = new FactoryOrderItemDO();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setDeliveryNum(orderItemDTO.getDeliveryNum());
            orderItemDO.setFactoryRemark(orderItemDTO.getFactoryRemark());
            factoryOrderItemService.updateFactoryOrder(orderItemDO);
        }

        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(factoryDeliveryDTO.getOrderId());
        factoryOrderDO.setWaybillNum(factoryDeliveryDTO.getWaybillNum());
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void rollbackDelivery(Integer oId) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void receive(Integer oId, List<CreateFactoryOrderItemDTO> orderItems) {

        Map<String, Integer> skuMap = orderItems.stream().collect(Collectors.toMap(CreateFactoryOrderItemDTO::getSku, CreateFactoryOrderItemDTO::getOrderNum));
        List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(oId);
        orderItemDOS.forEach(orderItem -> {
            int curReceive = skuMap.getOrDefault(orderItem.getSku(), 0);
            if (curReceive != 0) {
                int dbReceive = orderItem.getReceiveNum() == null ? 0 : orderItem.getReceiveNum();
                orderItem.setReceiveNum(dbReceive + curReceive);
                factoryOrderItemService.updateFactoryOrder(orderItem);
            }
        });

        //同步库存信息
        executor.execute(() -> skuMap.forEach((k, v) -> {
            if (v != 0) {
                itemDealService.dealSkuInventory(k, "mod", v);
            }
        }));
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Integer orderId) {
        int totalNum = 0;
        double totalPrice = 0.0;
        List<FactoryOrderItemDO> orderItemDOS = factoryOrderItemService.getItemsByOrderId(orderId);
        for (FactoryOrderItemDO orderItem : orderItemDOS) {
            orderItem.setTotalPrice(orderItem.getItemPrice() * orderItem.getReceiveNum());
            factoryOrderItemService.updateFactoryOrder(orderItem);

            //统计总量数据
            totalNum += orderItem.getReceiveNum();
            totalPrice += orderItem.getTotalPrice();
        }

        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(orderId);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_DELIVERY.getCode());
        factoryOrderDO.setTotalNum(totalNum);
        factoryOrderDO.setTotalPrice(totalPrice);
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

    public void pay(Integer oId, String paymentVoucher) {
        FactoryOrderDO factoryOrderDO = new FactoryOrderDO();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setPaymentVoucher(paymentVoucher);
        factoryOrderDO.setOrderStatus(OrderStatusEnum.ORDER_PAY.getCode());
        factoryOrderService.updateFactoryOrder(factoryOrderDO);
    }

}
