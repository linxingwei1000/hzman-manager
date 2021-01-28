package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.server.meta.HzmRoleType;
import com.cn.hzm.server.service.FactoryDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:49 下午
 */
@Api(tags = "厂家中心api")
@RestController
@RequestMapping("/factory")
@Slf4j
@HzmAuthToken

public class FactoryApi {


    @Autowired
    private FactoryDealService factoryDealService;

    @ApiOperation("列厂家")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_EMPLOYEE})
    public HzmResponse listItem(@RequestBody FactoryConditionDTO factoryConditionDTO) {
        return HzmResponse.success(factoryDealService.processFactoryList(factoryConditionDTO));
    }

    @ApiOperation("创建厂家")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    public HzmResponse createItem(@RequestBody FactoryDTO factoryDTO) throws Exception {
        factoryDealService.dealFactory(factoryDTO, true);
        return HzmResponse.success(true);
    }

    @ApiOperation("修改厂家信息")
    @RequestMapping(value = "/mod", method = RequestMethod.POST)
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    public HzmResponse modifyItem(@RequestBody FactoryDTO factoryDTO) throws Exception {
        factoryDealService.dealFactory(factoryDTO, false);
        return HzmResponse.success(true);
    }

    @ApiOperation("商品厂家认领")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    @RequestMapping(value = "/item/claim", method = RequestMethod.GET)
    public HzmResponse modifyItem(@ApiParam("工厂Id") @RequestParam Integer factoryId,
                                  @ApiParam("sku") @RequestParam String sku,
                                  @ApiParam("desc") @RequestParam String desc) throws Exception {
        factoryDealService.factoryClaimItem(factoryId, sku, desc);
        return HzmResponse.success(true);
    }

    @ApiOperation("厂家外链查看订单")
    @RequestMapping(value = "/out/order/list", method = RequestMethod.POST)
    public HzmResponse outList(@ApiParam("订单id") FactoryOrderConditionDTO factoryOrderConditionDTO) {
        return HzmResponse.success(factoryDealService.outOrderList(factoryOrderConditionDTO));
    }

//    @ApiOperation("删除厂家")
//    @RequestMapping(value = "/del/{fId}", method = RequestMethod.GET)
//    public HzmResponse deleteItem(@PathVariable Integer fId) {
//        factoryDealService.deleteFactory(fId);
//        return HzmResponse.success(true);
//    }


    @ApiOperation("例举厂家订单")
    @RequestMapping(value = "/order/list", method = RequestMethod.POST)
    public HzmResponse listOrder(@RequestBody FactoryOrderConditionDTO factoryOrderConditionDTO) {
        return HzmResponse.success(factoryDealService.orderList(factoryOrderConditionDTO));
    }

    @ApiOperation("根据订单状态获取订单")
    @RequestMapping(value = "/order/list/status", method = RequestMethod.GET)
    public HzmResponse listOrder(@ApiParam("订单状态") @RequestParam Integer orderStatus) {
        return HzmResponse.success(factoryDealService.getOrderByStatus(orderStatus));
    }

    @ApiOperation("删除厂家订单")
    @RequestMapping(value = "/order/delete", method = RequestMethod.GET)
    public HzmResponse deleteOrder(@ApiParam("订单id") @RequestParam Integer orderId) {
        return HzmResponse.success(factoryDealService.deleteOrder(orderId));
    }

    @ApiOperation("创建厂家订单商品")
    @RequestMapping(value = "/order/add", method = RequestMethod.POST)
    public HzmResponse createOrder(@RequestBody CreateFactoryOrderDTO createFactoryOrderDTO) {
        return HzmResponse.success(factoryDealService.createOrder(createFactoryOrderDTO));
    }

    @ApiOperation("修改厂家订单商品")
    @RequestMapping(value = "/order/mod", method = RequestMethod.POST)
    public HzmResponse modOrder(@RequestBody List<FactoryOrderItemDTO> orderItems) {
        return HzmResponse.success(factoryDealService.modOrderItem(orderItems));
    }

    @ApiOperation("添加厂家订单商品")
    @RequestMapping(value = "/order/item/add", method = RequestMethod.POST)
    public HzmResponse orderItemAdd(@RequestBody AddFactoryOrderDTO addFactoryOrderDTO) {
        return HzmResponse.success(factoryDealService.addOrderItem(addFactoryOrderDTO));
    }

    @ApiOperation("公司确认下单")
    @RequestMapping(value = "/order/hzm/confirm/place", method = RequestMethod.GET)
    public HzmResponse hzmConfirmPlace(
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.hzmConfirmPlace(oId);
        return HzmResponse.success(true);
    }

    @ApiOperation("厂家确认订单")
    @RequestMapping(value = "/order/factory/confirm", method = RequestMethod.POST)
    public HzmResponse factoryConfirmOrder(@RequestBody FactoryConfirmDTO factoryConfirmDTO) {
        factoryDealService.factoryConfirmOrder(factoryConfirmDTO.getOrderItems(), factoryConfirmDTO.getOrderId(), factoryConfirmDTO.getDeliveryDate());
        return HzmResponse.success(true);
    }

    @ApiOperation("公司确认订单")
    @RequestMapping(value = "/order/hzm/confirm", method = RequestMethod.GET)
    public HzmResponse hzmConfirm(
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.hzmConfirm(oId);
        return HzmResponse.success(true);
    }

    @ApiOperation("厂家确认交货")
    @RequestMapping(value = "/order/factory/delivery", method = RequestMethod.GET)
    public HzmResponse delivery(
            @ApiParam("运单编号") @RequestParam String waybillNum,
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.delivery(oId, waybillNum);
        return HzmResponse.success(true);
    }

    @ApiOperation("公司完成订单")
    @RequestMapping(value = "/order/hzm/complete", method = RequestMethod.POST)
    public HzmResponse complete(@RequestBody AddFactoryOrderDTO addFactoryOrderDTO) {
        factoryDealService.complete(addFactoryOrderDTO.getFactoryOrderId(), addFactoryOrderDTO.getOrderItems());
        return HzmResponse.success(true);
    }

    @ApiOperation("财务支付订单")
    @RequestMapping(value = "/order/pay", method = RequestMethod.GET)
    public HzmResponse pay(
            @ApiParam("付款凭证") @RequestParam String paymentVoucher,
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.pay(oId, paymentVoucher);
        return HzmResponse.success(true);
    }
}
