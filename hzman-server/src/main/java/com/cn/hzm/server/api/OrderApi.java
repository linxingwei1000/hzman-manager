package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.OrderConditionDTO;
import com.cn.hzm.server.service.OrderDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 1:45 下午
 */
@Api(tags = "订单中心api")
@RestController
@RequestMapping("/order")
public class OrderApi {

    @Resource
    private OrderDealService orderDealService;

    @ApiOperation("列订单")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody OrderConditionDTO conditionDTO){
        return HzmResponse.success(orderDealService.processListOrder(conditionDTO));
    }

    @ApiOperation("查询指定amazonId订单")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public HzmResponse createItem(@ApiParam("amazonId") @RequestParam String amazonId){
        return HzmResponse.success(orderDealService.searchByAmazonId(amazonId));
    }




}
