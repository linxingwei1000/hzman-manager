package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.FactoryOrderConditionDTO;
import com.cn.hzm.server.dto.OrderConditionDTO;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.server.service.AmazonOrderService;
import com.cn.hzm.server.service.FactoryDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 1:45 下午
 */
@Api(tags = "订单中心api")
@RestController
@RequestMapping("/order")
@HzmAuthToken
public class OrderApi {

    @Autowired
    private FactoryDealService orderDealService;

    @Autowired
    private AmazonOrderService amazonOrderService;

    @ApiOperation("列订单")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody FactoryOrderConditionDTO conditionDTO){
        return HzmResponse.success(orderDealService.htmlOrderList(conditionDTO));
    }

    @ApiOperation("亚马逊订单")
    @RequestMapping(value = "/amazon/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody OrderConditionDTO conditionDTO){
        return HzmResponse.success(amazonOrderService.processListOrder(conditionDTO));
    }

    @ApiOperation("删除僵尸订单")
    @RequestMapping(value = "/amazon/delete", method = RequestMethod.GET)
    public HzmResponse localDelete(@ApiParam("amazonId") @RequestParam Integer amazonId){
        return HzmResponse.success(amazonOrderService.localDeleteAmazonOrder(amazonId));
    }
}
