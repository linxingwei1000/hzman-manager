package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.ItemConditionDTO;
import com.cn.hzm.server.service.ItemDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:39 下午
 */
@Api(tags = "商品中心api")
@RestController
@RequestMapping("/item")
public class ItemApi {

    @Resource
    private ItemDealService itemDealService;

    @ApiOperation("列商品")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody ItemConditionDTO conditionDTO) {
        return HzmResponse.success(itemDealService.processListItem(conditionDTO));
    }

    @ApiOperation("创建商品")
    @RequestMapping(value = "/sync", method = RequestMethod.GET)
    public HzmResponse syncItem(@ApiParam("商品sku") @RequestParam String sku) {
        itemDealService.processSync(sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("刷新商品")
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public HzmResponse createItem(@ApiParam("商品sku") @RequestParam String sku) {
        itemDealService.processSync(sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("商品模糊查询")
    @RequestMapping(value = "/fuzzy", method = RequestMethod.GET)
    public HzmResponse fuzzyQuery(@ApiParam(name = "查询类型", defaultValue = "1") @RequestParam Integer searchType,
                                  @ApiParam("查询值") @RequestParam String value) {
        return HzmResponse.success(itemDealService.fuzzyQuery(searchType, value));
    }
}
