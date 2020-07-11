package com.cn.hzm.server.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:39 下午
 */
@RestController
public class ItemApi {



    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void createItem(){

    }
}
