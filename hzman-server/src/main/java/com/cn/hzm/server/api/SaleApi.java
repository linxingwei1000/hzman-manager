package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.api.dto.SaleConditionDto;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.api.meta.HzmRoleType;
import com.cn.hzm.server.service.SaleInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/29 11:23 上午
 */
@Api(tags = "销量数据api")
@RestController
@RequestMapping("/sale")
@HzmAuthToken
@HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
public class SaleApi {

    @Autowired
    private SaleInfoService saleInfoService;

    @ApiOperation("当日销量")
    @RequestMapping(value = "/curdate", method = RequestMethod.GET)
    public HzmResponse curDate() {
        return HzmResponse.success(saleInfoService.getCurSaleInfo());
    }


    @ApiOperation("销量数据")
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody SaleConditionDto saleConditionDTO) {
        return HzmResponse.success(saleInfoService.getSaleInfo(saleConditionDTO));
    }
}
