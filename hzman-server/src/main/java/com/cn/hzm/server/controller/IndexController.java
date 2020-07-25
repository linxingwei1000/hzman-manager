package com.cn.hzm.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@RestController
public class IndexController {

    @RequestMapping(value = {"/", "/index", "/home"})
    public String index() {
        return "hello!</br>" +
                "<form action='/login' method='POST'>" +
                "<div><label>username: <input type='text' name='username'/></label></div>" +
                "<div><label>password: <input type='password' name='password'/></label></div>" +
                "<div><input type='submit' value='sign in'/></div>" +
                "</form>";
    }
}
