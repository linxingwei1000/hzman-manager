package com.cn.hzm.server.controller;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.config.security.PasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@RestController
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public HzmResponse login(String username, String password) {
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(new PasswordAuthenticationToken(username, password)));
        return HzmResponse.generateOkResponse(null);
    }
}
