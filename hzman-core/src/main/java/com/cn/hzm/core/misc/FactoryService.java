package com.cn.hzm.core.misc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.*;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.repository.dao.*;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.api.enums.FactoryOrderStatusEnum;
import com.cn.hzm.core.cache.ItemDetailCache;
import com.cn.hzm.api.meta.HzmRoleType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class FactoryService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private FactoryDao factoryDao;

    @Autowired
    private FactoryOrderDao factoryOrderDao;

    @Autowired
    private FactoryOrderItemDao factoryOrderItemDao;

    @Autowired
    private FactoryItemDao factoryItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemDetailCache itemDetailCache;

    @Resource(name = "backTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    private static final String HIDE_INFO = "*******";

    private static final Double HIDE_PRICE_INFO = 999999.99;

    public JSONObject processFactoryList(FactoryConditionDto factoryConditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(factoryConditionDTO);
        List<FactoryDo> list = factoryDao.getListByCondition(condition);

        List<FactoryDto> factoryDTOS = factoryConditionDTO.pageResult(list).stream().map(factoryDO -> {
            FactoryDto factoryDTO = roleControlFactoryInfo(factoryDO);
            List<FactoryOrderDo> orderDOS = factoryOrderDao.getOrderByFactoryId(factoryDO.getId());
            factoryDTO.setOrderList(orderDOS.stream().map(orderDO -> {
                FactoryOrderDto orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDto.class);
                orderDTO.setStatus(orderDO.getOrderStatus());
                orderDTO.setStatusDesc(FactoryOrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());

                List<FactoryOrderItemDo> orderItemDOS = factoryOrderItemDao.getItemsByOrderId(orderDO.getId());
                orderDTO.setOrderItems(orderItemDOS.stream().map(orderItemDO -> {
                    FactoryOrderItemDto orderItemDTO = JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), FactoryOrderItemDto.class);
                    ItemDo itemDO = itemDao.getItemDOBySku(ThreadLocalCache.getUser().getUserMarketId(), orderItemDO.getSku());
                    if (itemDO != null) {
                        orderItemDTO.setTitle(itemDO.getTitle());
                        orderItemDTO.setIcon(itemDO.getIcon());
                    }
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

    private FactoryDto roleControlFactoryInfo(FactoryDo factoryDO) {
        FactoryDto factoryDTO;
        Set<String> roleSet = HzmContext.get().getRoles();
        if (!roleSet.contains(HzmRoleType.ROLE_ADMIN.getRoleId())) {
            factoryDTO = new FactoryDto();
            factoryDTO.setFactoryName(factoryDO.getFactoryName());
            factoryDTO.setAddress(HIDE_INFO);
            factoryDTO.setCollectMethod(HIDE_INFO);
            factoryDTO.setContactPerson(HIDE_INFO);
            factoryDTO.setContactInfo(HIDE_INFO);
            factoryDTO.setWx(HIDE_INFO);
        } else {
            factoryDTO = JSONObject.parseObject(JSONObject.toJSONString(factoryDO), FactoryDto.class);
        }
        return factoryDTO;
    }

    public void dealFactory(FactoryDto factoryDTO, Boolean isCreateMode) throws Exception {
        if (StringUtils.isEmpty(factoryDTO.getFactoryName())) {
            throw new RuntimeException("厂家名为空");
        }

        FactoryDo old = factoryDao.getByName(factoryDTO.getFactoryName());
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
        FactoryDo factoryDO = JSONObject.parseObject(JSONObject.toJSONString(factoryDTO), FactoryDo.class);

        if (isCreateMode) {
            factoryDao.createFactory(factoryDO);
        } else {
            factoryDao.updateFactory(factoryDO);
        }
    }

    public JSONObject factoryItemList(FactoryConditionDto factoryConditionDTO) {
        List<FactoryItemDo> factoryItemDOS = factoryItemDao.getInfoByFactoryId(factoryConditionDTO.getFactoryId());

        List<ItemDto> ItemDTOs = factoryConditionDTO.pageResult(factoryItemDOS).stream()
                .map(factoryItemDO -> itemDetailCache.getSingleCache(ThreadLocalCache.getUser().getUserMarketId(), factoryItemDO.getSku()))
                .collect(Collectors.toList());
        JSONObject respJo = new JSONObject();
        respJo.put("total", factoryItemDOS.size());
        respJo.put("data", JSONObject.toJSON(ItemDTOs));
        return respJo;
    }

    /**
     * 批量添加厂家商品信息
     *
     * @param factoryClaimDTOS
     */
    @Transactional
    public void factoryBatchClaimItem(List<FactoryClaimDto> factoryClaimDTOS) {
        factoryClaimDTOS.forEach(dto -> factoryClaimItem(dto.getFactoryId(), dto.getSku(), dto.getDesc()));
    }

    /**
     * 厂家认领商品
     *
     * @param factoryId
     * @param sku
     */
    public void factoryClaimItem(Integer factoryId, String sku, String desc) {
        if (factoryId == null || factoryDao.getByFid(factoryId) == null) {
            return;
        }

        FactoryItemDo factoryItemDO = new FactoryItemDo();
        factoryItemDO.setFactoryId(factoryId);
        factoryItemDO.setSku(sku);
        factoryItemDO.setItemDesc(desc);

        List<FactoryOrderDo> orders = factoryOrderDao.getOrderByFactoryId(factoryId);
        if (!CollectionUtils.isEmpty(orders)) {
            //从厂家确认订单开始
            Set<Integer> orderIds = orders.stream()
                    .filter(order -> order.getOrderStatus() >= FactoryOrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode())
                    .map(FactoryOrderDo::getId)
                    .collect(Collectors.toSet());

            List<FactoryOrderItemDo> orderItems = factoryOrderItemDao.getOrderBySku(sku);
            for (FactoryOrderItemDo orderItemDO : orderItems) {
                if (orderIds.contains(orderItemDO.getFactoryOrderId())) {
                    factoryItemDO.setFactoryPrice(orderItemDO.getItemPrice());
                    break;
                }
            }
        }

        FactoryItemDo old = factoryItemDao.getInfoBySkuAndFactoryId(sku, factoryId);
        if (old != null) {
            factoryItemDO.setId(old.getId());
            factoryItemDao.updateFactoryItem(factoryItemDO);
        } else {
            factoryItemDao.createFactoryItem(factoryItemDO);
        }
        //刷新缓存
        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), sku);
    }

    /**
     * 厂家商品信息删除
     *
     * @param itemInfoId
     * @return
     */
    public Integer deleteFactoryItem(Integer itemInfoId) {
        FactoryItemDo factoryItemDO = factoryItemDao.getInfoById(itemInfoId);
        factoryItemDao.deleteFactoryItem(itemInfoId);

        //刷新缓存
        itemDetailCache.refreshCache(ThreadLocalCache.getUser().getUserMarketId(), factoryItemDO.getSku());

        return 1;
    }

    /**
     * @param fId
     */
    public void deleteFactory(Integer fId) {
        factoryDao.deleteFactory(fId);
    }

    /**
     * 获取订单页面订单
     *
     * @param conditionDTO
     * @return
     */
    public JSONObject htmlOrderList(FactoryConditionDto conditionDTO) {
        List<FactoryOrderDo> list = factoryOrderDao.getOrderByStatusAndFactory(conditionDTO.getOrderStatus(), conditionDTO.getFactoryId());
        return processOrderList(list, conditionDTO);
    }


    private JSONObject processOrderList(List<FactoryOrderDo> needDealOrders, FactoryConditionDto condition) {
        //角色过滤厂家订单
        Set<String> roleSet = HzmContext.get().getRoles();
        if (roleSet.size() == 1 && roleSet.contains(HzmRoleType.ROLE_FACTORY.getRoleId())) {
            needDealOrders = needDealOrders.stream()
                    .filter(order -> !FactoryOrderStatusEnum.ORDER_START.getCode().equals(order.getOrderStatus())).collect(Collectors.toList());
        }

        //过滤新建订单之后，可能订单列表为空
        if (CollectionUtils.isEmpty(needDealOrders)) {
            JSONObject respJo = new JSONObject();
            respJo.put("total", 0);
            respJo.put("data", new JSONArray());
            return respJo;
        }

        List<Integer> factoryIds = needDealOrders.stream().map(FactoryOrderDo::getFactoryId).collect(Collectors.toList());
        Map<Integer, FactoryDo> factorys = factoryDao.getByIds(factoryIds).stream().collect(Collectors.toMap(FactoryDo::getId, order -> order));


        List<FactoryOrderDto> dtos = condition.pageResult(needDealOrders).stream().map(orderDO -> {
            FactoryOrderDto orderDTO = JSONObject.parseObject(JSONObject.toJSONString(orderDO), FactoryOrderDto.class);
            orderDTO.setStatus(orderDO.getOrderStatus());
            orderDTO.setStatusDesc(FactoryOrderStatusEnum.getEnumByCode(orderDO.getOrderStatus()).getDesc());
            orderDTO.setFactoryName(factorys.get(orderDO.getFactoryId()).getFactoryName());
            orderDTO.setOrderDesc(orderDO.getOrderDesc());
            orderDTO.setOrderCreateTime(TimeUtil.getDateFormat(orderDO.getCtime()));

            List<FactoryOrderItemDo> orderItemDOS = factoryOrderItemDao.getItemsByOrderId(orderDO.getId());
            orderDTO.setOrderItems(orderItemDOS.stream().map(orderItemDO -> {
                FactoryOrderItemDto orderItemDTO = JSONObject.parseObject(JSONObject.toJSONString(orderItemDO), FactoryOrderItemDto.class);
                if (roleSet.size() == 1 && roleSet.contains(HzmRoleType.ROLE_EMPLOYEE.getRoleId())) {
                    orderItemDTO.setItemPrice(HIDE_PRICE_INFO);
                    orderItemDTO.setTotalPrice(HIDE_PRICE_INFO);
                }

                //分批交货，计算剩余未交货数量
                int remainNum = orderItemDTO.getDeliveryNum() - (orderItemDTO.getReceiveNum() == null ? 0 : orderItemDTO.getReceiveNum());
                orderItemDTO.setRemainNum(remainNum);

                ItemDo itemDO = itemDao.getItemDOBySku(ThreadLocalCache.getUser().getUserMarketId(), orderItemDO.getSku());
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
    public JSONObject orderList(FactoryConditionDto factoryOrderConditionDTO) {
        List<FactoryOrderDo> orderDOS = factoryOrderDao.getOrderByFactoryId(factoryOrderConditionDTO.getFactoryId());
        return processOrderList(orderDOS, factoryOrderConditionDTO);
    }

    public JSONArray getOrderByStatus(Integer orderStatus) {
        List<FactoryOrderDo> orderDOS = factoryOrderDao.getOrderByStatus(orderStatus);
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
    public Integer createOrder(CreateFactoryOrderDto createFactoryOrderDTO) {
        if (createFactoryOrderDTO.getFactoryId() == null || factoryDao.getByFid(createFactoryOrderDTO.getFactoryId()) == null) {
            throw new RuntimeException("厂家参数非法");
        }

        //check
        List<String> skus = Lists.newArrayList();
        createFactoryOrderDTO.getOrderItems().forEach(item -> checkOrderItem(item.getSku(), item.getOrderNum(), skus));

        //创建工厂订单
        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setFactoryId(createFactoryOrderDTO.getFactoryId());
        factoryOrderDO.setOrderDesc(createFactoryOrderDTO.getDesc());
        factoryOrderDO.setReceiveAddress(createFactoryOrderDTO.getReceiveAddress());
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_START.getCode());
        factoryOrderDao.createFactoryOrder(factoryOrderDO);

        List<CreateFactoryOrderItemDto> orderItems = createFactoryOrderDTO.getOrderItems();
        for (CreateFactoryOrderItemDto orderItemDTO : orderItems) {
            FactoryOrderItemDo orderItemDO = new FactoryOrderItemDo();
            orderItemDO.setFactoryOrderId(factoryOrderDO.getId());
            orderItemDO.setSku(orderItemDTO.getSku());
            orderItemDO.setOrderNum(orderItemDTO.getOrderNum());
            orderItemDO.setRemark(orderItemDTO.getRemark());
            factoryOrderItemDao.createFactoryOrderItem(orderItemDO);
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
        FactoryOrderDo factoryOrderDO = factoryOrderDao.getOrderById(orderId);
        if (factoryOrderDO.getOrderStatus() > FactoryOrderStatusEnum.ORDER_CONFIRM_PLACE.getCode()) {
            throw new HzmException(ExceptionCode.ORDER_DELETE_ILLEGAL);
        }

        //删除订单
        factoryOrderDao.deleteFactoryOrder(orderId);

        //删除订单商品
        factoryOrderItemDao.deleteByOrderId(orderId);

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
    public Integer modOrderItem(ModFactoryOrderDto modFactoryOrderDTO) {
        if (modFactoryOrderDTO.getFactoryId() == null || factoryDao.getByFid(modFactoryOrderDTO.getFactoryId()) == null) {
            throw new RuntimeException("厂家参数非法");
        }

        List<String> skus = Lists.newArrayList();
        modFactoryOrderDTO.getOrderItems().forEach(item -> checkOrderItem(item.getSku(), item.getOrderNum(), skus));

        //创建工厂订单
        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(modFactoryOrderDTO.getOrderId());
        factoryOrderDO.setOrderDesc(modFactoryOrderDTO.getDesc());
        factoryOrderDO.setReceiveAddress(modFactoryOrderDTO.getReceiveAddress());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);

        for (FactoryOrderItemDto orderItemDTO : modFactoryOrderDTO.getOrderItems()) {
            FactoryOrderItemDo orderItemDO = new FactoryOrderItemDo();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setSku(orderItemDTO.getSku());
            orderItemDO.setOrderNum(orderItemDTO.getOrderNum());
            orderItemDO.setRemark(orderItemDTO.getRemark());
            factoryOrderItemDao.updateFactoryOrder(orderItemDO);
        }
        return 1;
    }

    /**
     * 添加厂家订单
     */
    @Transactional
    public Integer addOrderItem(AddFactoryOrderDto addFactoryOrderDTO) {
        addFactoryOrderDTO.getOrderItems().forEach(orderItem -> {
            FactoryOrderItemDo old = factoryOrderItemDao.getItemByOrderIdAndSku(addFactoryOrderDTO.getFactoryOrderId(), orderItem.getSku());
            if (old != null) {
                old.setOrderNum(orderItem.getOrderNum() + old.getOrderNum());
                old.setRemark(orderItem.getRemark());
                factoryOrderItemDao.updateFactoryOrder(old);
            } else {
                FactoryOrderItemDo orderItemDO = new FactoryOrderItemDo();
                orderItemDO.setFactoryOrderId(addFactoryOrderDTO.getFactoryOrderId());
                orderItemDO.setSku(orderItem.getSku());
                orderItemDO.setOrderNum(orderItem.getOrderNum());
                orderItemDO.setRemark(orderItem.getRemark());
                factoryOrderItemDao.createFactoryOrderItem(orderItemDO);
            }
        });

        return 1;
    }

    public void hzmConfirmPlace(Integer oId) {
        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_CONFIRM_PLACE.getCode());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);

        //商品订单默认价格
        executor.execute(() -> {
            FactoryOrderDo dbOrder = factoryOrderDao.getOrderById(oId);
            List<FactoryOrderItemDo> factoryOrderItems = factoryOrderItemDao.getItemsByOrderId(oId);
            factoryOrderItems.forEach(orderItem -> {
                FactoryItemDo factoryItemDO = factoryItemDao.getInfoBySkuAndFactoryId(orderItem.getSku(), dbOrder.getFactoryId());
                if (factoryItemDO != null) {
                    orderItem.setItemPrice(factoryItemDO.getFactoryPrice());
                    factoryOrderItemDao.updateFactoryOrder(orderItem);
                }
            });
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void factoryConfirmOrder(List<FactoryOrderItemDto> orderItems, Integer oId, String deliveryDate) {
        FactoryOrderDo old = factoryOrderDao.getOrderById(oId);
        if (old.getOrderStatus() > FactoryOrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode()) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_ILLEGAL);
        }

        if (StringUtils.isEmpty(deliveryDate)) {
            throw new HzmException(ExceptionCode.FACTORY_ORDER_CONFIRM_DATE_MUST);
        }

        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setDeliveryDate(deliveryDate);
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);

        for (FactoryOrderItemDto orderItemDTO : orderItems) {
            FactoryOrderItemDo orderItemDO = new FactoryOrderItemDo();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setItemPrice(orderItemDTO.getItemPrice());
            orderItemDO.setFactoryRemark(orderItemDTO.getFactoryRemark());
            factoryOrderItemDao.updateFactoryOrder(orderItemDO);

            //存在认领记录，更新最新订价
            FactoryItemDo factoryItemDO = factoryItemDao.getInfoBySkuAndFactoryId(orderItemDTO.getSku(), old.getFactoryId());
            if (factoryItemDO != null) {
                factoryItemDO.setFactoryPrice(orderItemDTO.getItemPrice());
                factoryItemDao.updateFactoryItem(factoryItemDO);
            } else {
                factoryItemDO = new FactoryItemDo();
                factoryItemDO.setFactoryId(old.getFactoryId());
                factoryItemDO.setSku(orderItemDTO.getSku());
                factoryItemDO.setItemDesc("厂家订单自动同步");
                factoryItemDO.setFactoryPrice(orderItemDTO.getItemPrice());
                factoryItemDao.createFactoryItem(factoryItemDO);
            }
        }

        Integer userMarketId = ThreadLocalCache.getUser().getUserMarketId();
        //刷新缓存
        executor.execute(() -> itemDetailCache.refreshCaches(userMarketId, orderItems.stream().map(FactoryOrderItemDto::getSku).collect(Collectors.toList())));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delivery(FactoryDeliveryDto factoryDeliveryDTO) {

        for (FactoryOrderItemDto orderItemDTO : factoryDeliveryDTO.getOrderItems()) {
            FactoryOrderItemDo orderItemDO = new FactoryOrderItemDo();
            orderItemDO.setId(orderItemDTO.getId());
            orderItemDO.setDeliveryNum(orderItemDTO.getDeliveryNum());
            orderItemDO.setFactoryRemark(orderItemDTO.getFactoryRemark());
            factoryOrderItemDao.updateFactoryOrder(orderItemDO);
        }

        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(factoryDeliveryDTO.getOrderId());
        factoryOrderDO.setWaybillNum(factoryDeliveryDTO.getWaybillNum());
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_FACTORY_DELIVERY.getCode());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);
    }

    public void rollbackDelivery(Integer oId) {
        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_FACTORY_CONFIRM.getCode());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);
    }

    public void receive(Integer oId, List<CreateFactoryOrderItemDto> orderItems) {

        Map<String, Integer> skuMap = orderItems.stream().collect(Collectors.toMap(CreateFactoryOrderItemDto::getSku, CreateFactoryOrderItemDto::getOrderNum));
        List<FactoryOrderItemDo> orderItemDOS = factoryOrderItemDao.getItemsByOrderId(oId);
        orderItemDOS.forEach(orderItem -> {
            int curReceive = skuMap.getOrDefault(orderItem.getSku(), 0);
            if (curReceive != 0) {
                int dbReceive = orderItem.getReceiveNum() == null ? 0 : orderItem.getReceiveNum();
                orderItem.setReceiveNum(dbReceive + curReceive);
                factoryOrderItemDao.updateFactoryOrder(orderItem);
            }
        });

        //同步库存信息
        executor.execute(() -> skuMap.forEach((k, v) -> {
            if (v != 0) {
                itemService.dealSkuInventory(k, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId(), "mod", v);
            }
        }));
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Integer orderId) {
        int totalNum = 0;
        double totalPrice = 0.0;
        List<FactoryOrderItemDo> orderItemDOS = factoryOrderItemDao.getItemsByOrderId(orderId);
        for (FactoryOrderItemDo orderItem : orderItemDOS) {
            orderItem.setTotalPrice(orderItem.getItemPrice() * orderItem.getReceiveNum());
            factoryOrderItemDao.updateFactoryOrder(orderItem);

            //统计总量数据
            totalNum += orderItem.getReceiveNum();
            totalPrice += orderItem.getTotalPrice();
        }

        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(orderId);
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_DELIVERY.getCode());
        factoryOrderDO.setTotalNum(totalNum);
        factoryOrderDO.setTotalPrice(totalPrice);
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);
    }

    public void pay(Integer oId, String paymentVoucher) {
        FactoryOrderDo factoryOrderDO = new FactoryOrderDo();
        factoryOrderDO.setId(oId);
        factoryOrderDO.setPaymentVoucher(paymentVoucher);
        factoryOrderDO.setOrderStatus(FactoryOrderStatusEnum.ORDER_PAY.getCode());
        factoryOrderDao.updateFactoryOrder(factoryOrderDO);
    }

}
