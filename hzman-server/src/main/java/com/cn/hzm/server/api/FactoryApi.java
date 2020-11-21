package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.FactoryConditionDTO;
import com.cn.hzm.server.dto.FactoryDTO;
import com.cn.hzm.server.service.FactoryDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:49 下午
 */
@Api(tags = "厂家中心api")
@RestController
@RequestMapping("/factory")
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

    @ApiOperation("创建厂家订单")
    @RequestMapping(value = "/order/add", method = RequestMethod.GET)
    public HzmResponse createOrder(
            @ApiParam("厂家id") @RequestParam Integer factoryId,
            @ApiParam("商品sku") @RequestParam String sku,
            @ApiParam("商品数量") @RequestParam Integer orderNum,
            @ApiParam("备注") @RequestParam String remark) {
        return HzmResponse.success(factoryDealService.createOrder(factoryId, sku, orderNum, remark));
    }

    @ApiOperation("厂家确认订单")
    @RequestMapping(value = "/order/factory/confirm", method = RequestMethod.GET)
    public HzmResponse factoryConfirmOrder(
            @ApiParam("商品单价") @RequestParam Double itemPrice,
            @ApiParam("交货日期yyyy-mm-dd") @RequestParam String deliveryDate,
            @ApiParam("订单id") @RequestParam Integer oId) {
        factoryDealService.factoryConfirmOrder(oId, itemPrice, deliveryDate);
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
