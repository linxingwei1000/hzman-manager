package com.cn.hzm.server.api;

import com.cn.hzm.core.entity.ItemDO;
import com.cn.hzm.item.service.ItemService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:39 下午
 */
@RestController
@RequestMapping("/item")
public class ItemApi {

    @Resource
    private ItemService itemService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public boolean createItem(@RequestBody ItemDO item){
        itemService.createItem(item);
        return true;
    }
}
