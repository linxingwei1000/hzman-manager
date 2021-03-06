package com.cn.hzm.server.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/6/23 8:43 下午
 */
@Api(tags = "测试api")
@RestController
public class TestApi {

    @ApiOperation("测试demo")
    @RequestMapping(value = "/demo", method = RequestMethod.GET)
    public String demo(@RequestParam(name = "scene") String scene) {
        return " scene: " + scene;
    }
}
