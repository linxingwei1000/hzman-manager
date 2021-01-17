package com.cn.hzm.server.controller;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.server.meta.HzmPermission;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@HzmAuthToken
public class UserController {

    @ApiOperation(value = "1.1 用户列表", notes = "查看用户列表", response = HzmPermission.class)
    @ApiResponses({
            @ApiResponse(response = HzmResponse.class, code = 200, message = "success")
    })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listUser(Integer pageSize, Integer pageIndex) {
        return HzmResponse.generateOkResponse(null);
    }
}
