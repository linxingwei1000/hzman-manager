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
public class AsinItemDto extends RespBaseDto{

    @ApiModelProperty(value = "ID", example = "100")
    private Integer id;

    @ApiModelProperty(value = "ASIN", example = "B07BGY7HWK")
    private String asin;

    @ApiModelProperty(value = "本地库存修改展示sku", example = "B07BGY7HWK")
    private String showSku;

    @ApiModelProperty(value = "商品名称", example = "N190301")
    private String title;

    @ApiModelProperty(value = "商品图标", example = "N190301")
    private String icon;

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

    @ApiModelProperty(value = "本地库存量")
    private Integer localQuantity;

    @ApiModelProperty(value = "商品尺寸")
    private PackageDimensionDto dimension;

    @ApiModelProperty(value = "各站点商品")
    private List<ItemDto> siteItems;

    private SaleInfoDto sortSaleInfo;

}
