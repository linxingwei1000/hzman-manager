package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:10 下午
 */
@ApiModel(description = "库存DTO")
@Data
public class InventoryDTO {

    @ApiModelProperty(value = "商品表id")
    private Integer itemId;

    @ApiModelProperty(value = "总商品量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "亚马逊总商品量")
    private Integer awsQuantity;

    @ApiModelProperty(value = "亚马逊库存量")
    private Integer awsStockQuantity;

    @ApiModelProperty(value = "本地库存量")
    private Integer localQuantity;
}
