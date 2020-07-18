package com.cn.hzm.server.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import com.cn.hzm.server.dto.ItemDTO;
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
    private ItemService itemService;

    @ApiOperation("创建商品")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public boolean createItem(@RequestBody ItemDTO item){
        itemService.createItem(JSON.toJavaObject((JSON) JSONObject.toJSON(item), ItemDO.class));
        return true;
    }
}
