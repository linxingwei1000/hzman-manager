package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:10 下午
 */
@ApiModel(description = "库存DTO")
@Data
public class InventoryDto {

    @ApiModelProperty(value = "本地自增id")
    private Integer id;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "asin")
    private String asin;

    @ApiModelProperty(value = "仓库唯一编号")
    private String fnsku;

    @ApiModelProperty(value = "总商品量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "亚马逊总商品量")
    private Integer amazonQuantity;

    @ApiModelProperty(value = "亚马逊FBA库存量")
    private Integer fulfillableQuantity;

    @ApiModelProperty(value = "亚马逊途中数量")
    private Integer inboundShippedQuantity;

    @ApiModelProperty(value = "亚马逊预留中数量")
    private Integer totalReservedQuantity;

    @ApiModelProperty(value = "亚马逊预留详情")
    private InventoryReservedDetailDto reservedDetailDto;

    @ApiModelProperty(value = "亚马逊研究中数量")
    private Integer totalResearchingQuantity;

    @ApiModelProperty(value = "亚马逊研究详情")
    private InventoryResearchingDetailDto researchingDetailDto;

    @ApiModelProperty(value = "亚马逊不可售数量")
    private Integer totalUnfulfillableQuantity;

    @ApiModelProperty(value = "亚马逊不可售详情")
    private InventoryUnfulfillableDetailDto unfulfillableDetailDto;

    @ApiModelProperty(value = "本地库存总量")
    private Integer localTotalQuantity;

    @ApiModelProperty(value = "本地库存量")
    private Integer localQuantity;

    @ApiModelProperty(value = "工厂库存量")
    private Integer factoryQuantity;

    @ApiModelProperty(value = "厂家制作数量")
    private List<FactoryQuantityDto> factoryQuantityInfos;
}
