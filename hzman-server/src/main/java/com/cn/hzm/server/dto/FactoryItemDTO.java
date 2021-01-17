package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 3:44 下午
 */
@Data
@ApiModel(description = "工厂商品DTO")
public class FactoryItemDTO {

    @ApiModelProperty(value = "订单id")
    private Integer factoryId;

    @ApiModelProperty(value = "工厂名")
    private String factoryName;

    private String sku;

    @ApiModelProperty(value = "订单价")
    private Double factoryPrice;

}
