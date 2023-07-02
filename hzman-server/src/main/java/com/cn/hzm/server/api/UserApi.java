package com.cn.hzm.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.server.dto.HzmUserDTO;
import com.cn.hzm.api.dto.UserConditionDto;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.api.meta.HzmPermission;
import com.cn.hzm.api.meta.HzmRoleType;
import com.cn.hzm.server.service.UserDealService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@HzmAuthToken
public class UserApi {

    @Autowired
    private UserDealService userDealService;

    @ApiOperation(value = "获取当前系统所有角色信息")
    @RequestMapping(value = "/role", method = RequestMethod.GET)
    public HzmResponse getRoleInfo() {
        JSONArray ja = new JSONArray();
        Arrays.stream(HzmRoleType.values()).forEach(enumRole -> {
            JSONObject jo = new JSONObject();
            jo.put("role", enumRole.getRoleId());
            jo.put("desc", enumRole.getDesc());
            ja.add(jo);
        });
        return HzmResponse.success(ja);
    }

    @ApiOperation(value = "用户列表", notes = "查看用户列表", response = HzmPermission.class)
    @ApiResponses({
            @ApiResponse(response = HzmResponse.class, code = 200, message = "success")
    })
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listUser(@RequestBody UserConditionDto userConditionDTO) {
        return HzmResponse.success(userDealService.getUserList(userConditionDTO));
    }

    @ApiOperation(value = "获取用户信息")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public HzmResponse getUserInfo() {
        return HzmResponse.success(userDealService.getUserInfo());
    }


    @ApiOperation(value = "添加用户")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public HzmResponse createUser(@RequestBody HzmUserDTO hzmUserDTO) {
        return HzmResponse.success(userDealService.createUser(hzmUserDTO));
    }

    @ApiOperation(value = "用户信息修改")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public HzmResponse updateUser(@RequestBody HzmUserDTO hzmUserDTO) {
        return HzmResponse.success(userDealService.updateUser(hzmUserDTO));
    }

    @ApiOperation(value = "用户密码修改")
    @RequestMapping(value = "/pswd/update", method = RequestMethod.GET)
    public HzmResponse updateUserPassword(@ApiParam(name = "修改用户id") @RequestParam Long userId,
                                      @ApiParam("修改密码值") @RequestParam String password) {
        return HzmResponse.success(userDealService.updateUserPassword(userId, password));
    }

    @ApiOperation(value = "初始化用户密码")
    @RequestMapping(value = "/pswd/install", method = RequestMethod.GET)
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
    public HzmResponse installPassword(@ApiParam(name = "修改用户id") @RequestParam Long userId) {
        return HzmResponse.success(userDealService.installPassword(userId));
    }

}
