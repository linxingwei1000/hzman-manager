package com.cn.hzm.server.api;

import com.cn.hzm.api.dto.*;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.api.dto.AddItemRemarkDto;
import com.cn.hzm.server.dto.MultiDeleteDTO;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.api.meta.HzmRoleType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:39 下午
 */
@Api(tags = "商品中心api")
@RestController
@RequestMapping("/item")
@HzmAuthToken
public class ItemApi {

    @Resource
    private ItemService itemService;

    @ApiOperation("列商品")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody ItemConditionDto conditionDTO) {
        return HzmResponse.success(itemService.processListItem(conditionDTO));
    }

    @ApiOperation("创建商品")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/sync", method = RequestMethod.GET)
    public HzmResponse syncItem(@ApiParam("商品sku") @RequestParam String sku) throws Exception {
        itemService.processSync(sku, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId());
        return HzmResponse.success(true);
    }

    @ApiOperation("删除商品")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public HzmResponse deleteItem(@ApiParam("商品sku") @RequestParam String asin, @ApiParam("商品sku") @RequestParam String sku) {
        itemService.deleteItem(asin, sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("批量删除商品")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/multi/delete", method = RequestMethod.POST)
    public HzmResponse deleteItems(@RequestBody List<MultiDeleteDTO> multiDeleteDTOS) {
        multiDeleteDTOS.forEach(md -> itemService.deleteItem(md.getAsin(), md.getSku()));
        return HzmResponse.success(true);
    }

    @ApiOperation("刷新商品")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public HzmResponse createItem(@ApiParam("商品sku") @RequestParam String sku) throws Exception {
        itemService.processSync(sku, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId());
        return HzmResponse.success(true);
    }

    @ApiOperation("商品模糊查询")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
    @RequestMapping(value = "/fuzzy", method = RequestMethod.GET)
    public HzmResponse fuzzyQuery(@ApiParam(name = "查询类型", defaultValue = "1") @RequestParam Integer searchType,
                                  @ApiParam("查询值") @RequestParam String value) {
        return HzmResponse.success(itemService.fuzzyQuery(searchType, value));
    }

    @ApiOperation("本地商品库存修改")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/inventory/local", method = RequestMethod.GET)
    public HzmResponse inventoryLocal(@ApiParam(name = "sku") @RequestParam String sku,
                                  @ApiParam("修改值") @RequestParam Integer curLocalNum) {
        return HzmResponse.success(itemService.modLocalNum(sku, curLocalNum, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId()));
    }

    @ApiOperation("商品成本修改")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/cost/mod", method = RequestMethod.GET)
    public HzmResponse ModCost(@ApiParam(name = "asin") @RequestParam String asin,
                                @ApiParam(name = "sku") @RequestParam String sku,
                                      @ApiParam("修改值") @RequestParam Double cost) {
        return HzmResponse.success(itemService.modSkuCost(asin, sku, cost, ThreadLocalCache.getUser().getUserMarketId()));
    }

    @ApiOperation("智能补货展示接口")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
    @RequestMapping(value = "/smart", method = RequestMethod.GET)
    public HzmResponse smart() {
        return HzmResponse.success(itemService.querySmartList());
    }

    @ApiOperation("amazon入库订单号爬取接口")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/shipment", method = RequestMethod.GET)
    public HzmResponse shipment(@ApiParam(name = "shipmentId") @RequestParam String shipmentId) {
        return HzmResponse.success(itemService.spiderShipment(ThreadLocalCache.getUser().getUserMarketId(), shipmentId));
    }

    @ApiOperation("FNSKU映射接口")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
    @RequestMapping(value = "/fnsku", method = RequestMethod.GET)
    public HzmResponse fnsku(@ApiParam("商品fnsku") @RequestParam String fnsku) {
        return HzmResponse.success(itemService.fnskuQuery(fnsku));
    }

    @ApiOperation("子体列表接口")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
    @RequestMapping(value = "/children", method = RequestMethod.GET)
    public HzmResponse childrenItem(@ApiParam("商品asin") @RequestParam String asin) {
        return HzmResponse.success(itemService.getChildrenItem(ThreadLocalCache.getUser().getUserMarketId(), asin));
    }

    @ApiOperation("商品类型接口")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public HzmResponse itemType() {
        return HzmResponse.success(itemService.getItemType());
    }

    @ApiOperation("添加商品备注")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/remark/add", method = RequestMethod.POST)
    public HzmResponse addRemark(@RequestBody AddItemRemarkDto remarkDto) {
        return HzmResponse.success(itemService.addRemark(remarkDto));
    }

    @ApiOperation("修改商品备注")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/remark/mod", method = RequestMethod.POST)
    public HzmResponse modRemark(@RequestBody AddItemRemarkDto remarkDto) {
        return HzmResponse.success(itemService.modRemark(remarkDto));
    }

    @ApiOperation("删除商品备注")
    @HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_OPERATE, HzmRoleType.ROLE_EMPLOYEE})
    @RequestMapping(value = "/remark/del", method = RequestMethod.GET)
    public HzmResponse delRemark(@ApiParam(name = "数据库id") @RequestParam Integer id) {
        return HzmResponse.success(itemService.delRemark(id));
    }
}
