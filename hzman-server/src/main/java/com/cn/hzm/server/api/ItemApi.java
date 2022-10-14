package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.enums.ItemTypeEnum;
import com.cn.hzm.server.dto.FactoryClaimDTO;
import com.cn.hzm.server.dto.ItemConditionDTO;
import com.cn.hzm.server.dto.MultiDeleteDTO;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermission;
import com.cn.hzm.server.interceptor.permission.HzmAuthToken;
import com.cn.hzm.server.meta.HzmRoleType;
import com.cn.hzm.server.service.ItemDealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:39 下午
 */
@Api(tags = "商品中心api")
@RestController
@RequestMapping("/item")
@HzmAuthToken
@HzmAuthPermission(needRole = {HzmRoleType.ROLE_ADMIN, HzmRoleType.ROLE_EMPLOYEE, HzmRoleType.ROLE_NEW_EMPLOYEE})
public class ItemApi {

    @Resource
    private ItemDealService itemDealService;

    @ApiOperation("列商品")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody ItemConditionDTO conditionDTO) {
        return HzmResponse.success(itemDealService.processListItem(conditionDTO));
    }

    @ApiOperation("创建商品")
    @RequestMapping(value = "/sync", method = RequestMethod.GET)
    public HzmResponse syncItem(@ApiParam("商品sku") @RequestParam String sku) {
        itemDealService.processSync(sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("删除商品")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public HzmResponse deleteItem(@ApiParam("商品sku") @RequestParam String asin, @ApiParam("商品sku") @RequestParam String sku) {
        itemDealService.deleteItem(asin, sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("批量删除商品")
    @RequestMapping(value = "/multi/delete", method = RequestMethod.POST)
    public HzmResponse deleteItems(@RequestBody List<MultiDeleteDTO> multiDeleteDTOS) {
        multiDeleteDTOS.forEach(md -> itemDealService.deleteItem(md.getAsin(), md.getSku()));
        return HzmResponse.success(true);
    }

    @ApiOperation("刷新商品")
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public HzmResponse createItem(@ApiParam("商品sku") @RequestParam String sku) {
        itemDealService.processSync(sku);
        return HzmResponse.success(true);
    }

    @ApiOperation("商品模糊查询")
    @RequestMapping(value = "/fuzzy", method = RequestMethod.GET)
    public HzmResponse fuzzyQuery(@ApiParam(name = "查询类型", defaultValue = "1") @RequestParam Integer searchType,
                                  @ApiParam("查询值") @RequestParam String value) {
        return HzmResponse.success(itemDealService.fuzzyQuery(searchType, value));
    }

    @ApiOperation("本地商品库存修改")
    @RequestMapping(value = "/inventory/local", method = RequestMethod.GET)
    public HzmResponse inventoryLocal(@ApiParam(name = "sku") @RequestParam String sku,
                                  @ApiParam("修改值") @RequestParam Integer curLocalNum) {
        return HzmResponse.success(itemDealService.modLocalNum(sku, curLocalNum));
    }

    @ApiOperation("智能补货展示接口")
    @RequestMapping(value = "/smart", method = RequestMethod.GET)
    public HzmResponse smart() {
        return HzmResponse.success(itemDealService.querySmartList());
    }

    @ApiOperation("amazon入库订单号爬取接口")
    @RequestMapping(value = "/shipment", method = RequestMethod.GET)
    public HzmResponse shipment(@ApiParam(name = "shipmentId") @RequestParam String shipmentId) {
        return HzmResponse.success(itemDealService.spiderShipment(shipmentId));
    }

    @ApiOperation("FNSKU映射接口")
    @RequestMapping(value = "/fnsku", method = RequestMethod.GET)
    public HzmResponse fnsku(@ApiParam("商品fnsku") @RequestParam String fnsku) {
        return HzmResponse.success(itemDealService.fnskuQuery(fnsku));
    }

    @ApiOperation("子体列表接口")
    @RequestMapping(value = "/children", method = RequestMethod.GET)
    public HzmResponse childrenItem(@ApiParam("商品asin") @RequestParam String asin) {
        return HzmResponse.success(itemDealService.getChildrenItem(asin));
    }

    @ApiOperation("商品类型接口")
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public HzmResponse itemType() {
        return HzmResponse.success(itemDealService.getItemType());
    }

    @ApiOperation("商品备注修改")
    @RequestMapping(value = "/remark", method = RequestMethod.GET)
    public HzmResponse modRemark(@ApiParam(name = "数据库id") @RequestParam Integer id,
                                      @ApiParam("备注") @RequestParam String remark) {
        return HzmResponse.success(itemDealService.modRemark(id, remark));
    }
}
