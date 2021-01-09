package com.cn.hzm.server.api;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.*;
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
public class FactoryApi {


    @Autowired
    private FactoryDealService factoryDealService;

    @ApiOperation("列厂家")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody FactoryConditionDTO factoryConditionDTO) {
        return HzmResponse.success(factoryDealService.processFactoryList(factoryConditionDTO));
    }

    @ApiOperation("创建厂家")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public HzmResponse createItem(@RequestBody FactoryDTO factoryDTO) throws Exception {
        factoryDealService.dealFactory(factoryDTO, true);
        return HzmResponse.success(true);
    }

    @ApiOperation("修改厂家信息")
    @RequestMapping(value = "/mod", method = RequestMethod.POST)
    public HzmResponse modifyItem(@RequestBody FactoryDTO factoryDTO) throws Exception {
        factoryDealService.dealFactory(factoryDTO, false);
        return HzmResponse.success(true);
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
    @RequestMapping(value = "/order/mod", method = RequestMethod.GET)
    public HzmResponse modOrder(
            @ApiParam("工厂Id") @RequestParam Integer factoryId,
            @ApiParam("工厂订单Id") @RequestParam Integer factoryOrderId,
            @ApiParam("商品sku") @RequestParam String sku,
            @ApiParam("商品数量") @RequestParam Integer orderNum,
            @ApiParam("备注") @RequestParam String remark) {
        return HzmResponse.success(factoryDealService.addOrderItem(factoryId, factoryOrderId, sku, orderNum, remark));
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
    public HzmResponse factoryConfirmOrder(@RequestBody List<FactoryOrderItemDTO> orderItems,
            @ApiParam("交货日期yyyy-mm-dd") @RequestParam String deliveryDate,
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.factoryConfirmOrder(orderItems, oId, deliveryDate);
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
    @RequestMapping(value = "/order/hzm/complete", method = RequestMethod.GET)
    public HzmResponse complete(
            @ApiParam("实收商品数量") @RequestParam Integer receiveNum,
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.complete(oId, receiveNum);
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
