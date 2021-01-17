package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:45 下午
 */
@Data
@ApiModel(description = "工厂订单商品DTO")
public class FactoryOrderItemDTO {

    @ApiModelProperty(value = "商品订单id")
    private Integer id;

    @ApiModelProperty(value = "工厂订单id")
    private Integer factoryOrderId;

    @ApiModelProperty(value = "商品sku")
    private String sku;

    private String title;

    private String icon;

    @ApiModelProperty(value = "订单数量")
    private Integer orderNum;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商品单价")
    private Double itemPrice;

}
