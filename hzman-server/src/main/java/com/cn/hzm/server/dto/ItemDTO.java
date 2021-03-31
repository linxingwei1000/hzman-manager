package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/17 9:50 下午
 */
@ApiModel(description = "商品DTO")
@Data
public class ItemDTO extends RespBaseDTO{

    @ApiModelProperty(value = "ID", example = "100")
    private Integer id;

    @ApiModelProperty(value = "ASIN", example = "B07BGY7HWK")
    private String asin;

    @ApiModelProperty(value = "商品名称", example = "N190301")
    private String title;

    @ApiModelProperty(value = "商品图标", example = "N190301")
    private String icon;

    @ApiModelProperty(value = "SKU", example = "N190301")
    private String sku;

    @ApiModelProperty(value = "商品单价")
    private Double itemPrice;

    @ApiModelProperty(value = "商品属性")
    private String attributeSet;

    @ApiModelProperty(value = "商品相关属性")
    private String relationship;

    @ApiModelProperty(value = "今日销量数据")
    private SaleInfoDTO today;

    @ApiModelProperty(value = "昨日销量数据")
    private SaleInfoDTO yesterday;

    @ApiModelProperty(value = "最近30天数据")
    private SaleInfoDTO duration30Day;

    @ApiModelProperty(value = "去年同期30天数据")
    private SaleInfoDTO lastYearDuration30Day;

    @ApiModelProperty(value = "库存信息", example = "N190301")
    private InventoryDTO inventoryDTO;

    @ApiModelProperty(value = "商品工厂信息")
    private List<FactoryItemDTO> factoryItemDTOS;

    @ApiModelProperty(value = "智能补货标")
    private Integer replenishmentCode;

    @ApiModelProperty(value = "智能补货数量")
    private Integer replenishmentNum;

    @ApiModelProperty("创建时间")
    private Date ctime;

    @ApiModelProperty("更新时间")
    private Date utime;
}
