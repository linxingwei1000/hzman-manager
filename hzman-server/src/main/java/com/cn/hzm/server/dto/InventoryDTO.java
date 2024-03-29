package com.cn.hzm.server.dto;

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
public class InventoryDTO {

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

    @ApiModelProperty(value = "亚马逊库存量")
    private Integer amazonStockQuantity;

    @ApiModelProperty(value = "亚马逊库转移中数量")
    private Integer amazonTransferQuantity;

    @ApiModelProperty(value = "亚马逊库入库中的数量")
    private Integer amazonInboundQuantity;

    @ApiModelProperty(value = "本地库存总量")
    private Integer localTotalQuantity;

    @ApiModelProperty(value = "本地库存量")
    private Integer localQuantity;

    @ApiModelProperty(value = "工厂库存量")
    private Integer factoryQuantity;

    @ApiModelProperty(value = "厂家制作数量")
    private List<FactoryQuantityDTO> factoryQuantityInfos;
}
