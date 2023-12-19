package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.api.dto.AwsSpiderTaskDto;
import com.cn.hzm.api.dto.AwsUserMarketDto;
import com.cn.hzm.api.dto.AwsUserSearchDto;
import com.cn.hzm.server.service.AwsService;
import com.cn.hzm.api.dto.AwsUserDto;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.api.meta.HzmRoleType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/29 11:23 上午
 */
@Api(tags = "亚马逊账号管理api")
@RestController
@RequestMapping("/aws/user")
@HzmAuthToken
@HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN})
public class AwsUserApi {

    @Autowired
    private AwsService awsService;

    @ApiOperation("亚马逊账号列表")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse curDate(@RequestBody AwsUserSearchDto awsUserSearchDto) {
        return HzmResponse.success(awsService.getAwsUserList(awsUserSearchDto));
    }

    @ApiOperation("创建aws账号")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public HzmResponse create(@RequestBody AwsUserDto awsUserDto) {
        return HzmResponse.success(awsService.createAwsUser(awsUserDto));
    }

    @ApiOperation("修改aws账号")
    @RequestMapping(value = "/mod", method = RequestMethod.POST)
    public HzmResponse mod(@RequestBody AwsUserDto awsUserDto) {
        return HzmResponse.success(awsService.modAwsUser(awsUserDto));
    }

    @ApiOperation("删除aws账号")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public HzmResponse delete(@ApiParam("账号id") @RequestParam Integer awsUserId) {
        return HzmResponse.success(awsService.deleteAwsUser(awsUserId));
    }

    @ApiOperation("用户关联亚马逊市场")
    @RequestMapping(value = "/market/list", method = RequestMethod.GET)
    public HzmResponse relationMarket() throws Exception {
        return HzmResponse.success(awsService.marketList());
    }

    @ApiOperation("用户关联亚马逊市场token刷新报警")
    @RequestMapping(value = "/market/warn", method = RequestMethod.GET)
    public HzmResponse marketWarnMsg() throws Exception {
        return HzmResponse.success(awsService.marketWarnMsg());
    }

    @ApiOperation("关联亚马逊市场")
    @RequestMapping(value = "/market/relation/create", method = RequestMethod.POST)
    public HzmResponse relationMarket(@RequestBody AwsUserMarketDto awsUserMarketDto) throws Exception {
        return HzmResponse.success(awsService.createMarketRelation(awsUserMarketDto));
    }

    @ApiOperation("亚马逊市场编辑")
    @RequestMapping(value = "/market/relation/mod", method = RequestMethod.POST)
    public HzmResponse modMarket(@RequestBody AwsUserMarketDto awsUserMarketDto) throws Exception {
        return HzmResponse.success(awsService.modMarketRelation(awsUserMarketDto));
    }

    @ApiOperation("删除亚马逊市场")
    @RequestMapping(value = "/market/relation/delete", method = RequestMethod.GET)
    public HzmResponse deleteMarketRelation(@ApiParam("账号市场关系id") @RequestParam Integer userMarketId) throws Exception {
        return HzmResponse.success(awsService.deleteMarketRelation(userMarketId));
    }

    @ApiOperation("创建市场爬取任务")
    @RequestMapping(value = "/market/spider/create", method = RequestMethod.POST)
    public HzmResponse createSpiderTask(@RequestBody AwsSpiderTaskDto awsSpiderTaskDto) throws Exception {
        return HzmResponse.success(awsService.createSpiderTask(awsSpiderTaskDto));
    }

    @ApiOperation("删除市场爬取任务")
    @RequestMapping(value = "/market/spider/delete", method = RequestMethod.GET)
    public HzmResponse deleteSpiderTask(@ApiParam("爬取任务id") @RequestParam Integer spiderTaskId) {
        return HzmResponse.success(awsService.deleteSpiderTask(spiderTaskId));
    }

    @ApiOperation("修改市场爬取任务爬取依赖时间")
    @RequestMapping(value = "/market/spider/mod/depend", method = RequestMethod.GET)
    public HzmResponse modSpiderTaskDepend(@ApiParam("爬取任务id") @RequestParam Integer spiderTaskId,
                                           @ApiParam("爬取依赖时间") @RequestParam String spiderDepend) throws Exception {
        return HzmResponse.success(awsService.modSpiderTaskDepend(spiderTaskId, spiderDepend));
    }

    @ApiOperation("修改市场爬取是否激活")
    @RequestMapping(value = "/market/spider/mod/active", method = RequestMethod.GET)
    public HzmResponse modSpiderTaskStatus(@ApiParam("爬取任务id") @RequestParam Integer spiderTaskId,
                                           @ApiParam("激活状态:1.激活，0。否") @RequestParam Integer active) throws Exception {
        return HzmResponse.success(awsService.modSpiderTaskStatus(spiderTaskId, active));
    }

    @ApiOperation("亚马逊市场枚举")
    @RequestMapping(value = "/market", method = RequestMethod.GET)
    public HzmResponse marketList() {
        return HzmResponse.success(AwsMarket.jsonEnum());
    }

    @ApiOperation("爬取信息枚举")
    @RequestMapping(value = "/spider/task/type", method = RequestMethod.GET)
    public HzmResponse SpiderList() {
        return HzmResponse.success(SpiderType.jsonEnum());
    }
}
