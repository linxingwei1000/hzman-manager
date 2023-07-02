package com.cn.hzm.api.dto;

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
public class ItemDto extends RespBaseDto{

    @ApiModelProperty(value = "ID", example = "100")
    private Integer id;

    @ApiModelProperty(value = "用户市场id")
    private Integer userMarketId;

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

    @ApiModelProperty(value = "商品成本")
    private Double cost;

    @ApiModelProperty(value = "商品属性")
    private String attributeSet;

    @ApiModelProperty(value = "商品相关属性")
    private String relationship;

    @ApiModelProperty(value = "商品尺寸")
    private PackageDimensionDto dimension;

    @ApiModelProperty(value = "商品类型")
    private String itemType;

    @ApiModelProperty(value = "商品备注")
    private String itemRemark;

    @ApiModelProperty(value = "商品类目排名")
    private List<CategoryRankDto> categoryRankDTOS;

    @ApiModelProperty(value = "最高排名")
    private Integer maxRank;

    @ApiModelProperty(value = "今日销量数据")
    private SaleInfoDto today;

    @ApiModelProperty(value = "昨日销量数据")
    private SaleInfoDto yesterday;

    @ApiModelProperty(value = "最近30天数据")
    private SaleInfoDto duration30Day;

    @ApiModelProperty(value = "最近30天到60数据")
    private SaleInfoDto duration3060Day;

    @ApiModelProperty(value = "去年同期30天数据")
    private SaleInfoDto lastYearDuration30Day;

    @ApiModelProperty(value = "库存信息", example = "N190301")
    private InventoryDto inventoryDTO;

    @ApiModelProperty(value = "商品工厂信息")
    private List<FactoryItemDto> factoryItemDTOS;

    @ApiModelProperty(value = "智能补货标")
    private Integer replenishmentCode;

    @ApiModelProperty(value = "智能补货数量")
    private Integer replenishmentNum;

    @ApiModelProperty("创建时间")
    private Date ctime;

    @ApiModelProperty("更新时间")
    private Date utime;

    @ApiModelProperty(value = "子体个数")
    private Integer childrenNum;

    @ApiModelProperty(value = "是否有子体")
    private Boolean haveChildren;

    @ApiModelProperty(value = "上架时间")
    private Date listingTime;

    @ApiModelProperty(value = "备注列表")
    private List<ItemRemarkDto> remarkDtos;
}
