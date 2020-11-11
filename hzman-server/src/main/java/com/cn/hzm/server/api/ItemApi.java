package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.ItemConditionDTO;
import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.service.ItemDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public HzmResponse listItem(@RequestBody ItemConditionDTO conditionDTO){
        return HzmResponse.success(itemDealService.processListItem(conditionDTO));
    }

    @ApiOperation("创建商品")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public HzmResponse createItem(@RequestBody ItemDTO item){
        itemDealService.processItemCreate(item);
        return HzmResponse.success(true);
    }
}
