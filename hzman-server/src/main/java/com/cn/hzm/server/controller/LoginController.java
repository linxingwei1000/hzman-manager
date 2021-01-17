package com.cn.hzm.server.controller;

import com.cn.hzm.core.common.HzmResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@RestController
public class LoginController {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public HzmResponse login(String username, String password) {
        return HzmResponse.generateOkResponse(null);
    }
}
