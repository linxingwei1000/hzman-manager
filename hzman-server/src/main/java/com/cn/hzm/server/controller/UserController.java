package com.cn.hzm.server.controller;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.meta.HzmPermission;
import io.swagger.annotations.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@Api(tags = "用户管理")
@RestController("/user")
public class UserController {

    @PreAuthorize("hasRole('role_admin') || hasPermission('" + HzmPermission.PermissionType.USER_MANAGER + "', '" + HzmPermission.UserManager.LIST_USER + "')")
    @ApiOperation(value = "1.1 用户列表", notes = "查看用户列表", response = HzmPermission.class)
    @ApiImplicitParams({
            @ApiImplicitParam(value = "pageSize", name = "分页参数", dataType = "Integer", paramType = "body", defaultValue = "20"),
            @ApiImplicitParam(value = "pageIndex", name = "分页页码", dataType = "Integer", paramType = "body", defaultValue = "0")
    })
    @ApiResponses({
            @ApiResponse(response = HzmResponse.class, code = 200, message = "success")
    })
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listUser() {
        return HzmResponse.generateOkResponse(null);
    }
}
