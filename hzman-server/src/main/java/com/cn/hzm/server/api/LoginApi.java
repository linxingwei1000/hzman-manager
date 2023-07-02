package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.MD5Util;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.service.PassportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@Api(tags = "登录")
@RestController
public class LoginApi {

    @Resource
    private PassportService passportService;

    @ApiOperation(value = "登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public HzmResponse login(String username, String password) {
        HzmPassport hzmPassport = passportService.findPassportByUsername(username);
        if(hzmPassport==null){
            throw new HzmException(ExceptionCode.USER_NO_EXIST);
        }

        String encodePassword = MD5Util.formPassToDb(password);

        if(!hzmPassport.getPassword().equals(encodePassword)){
            throw new HzmException(ExceptionCode.USER_PASSWORD_ERROR);
        }

        String token = passportService.generateToken(hzmPassport.getId());

        //更新token
        hzmPassport.setToken(token);
        passportService.updatePassport(hzmPassport);

        return HzmResponse.success(token);
    }
}
