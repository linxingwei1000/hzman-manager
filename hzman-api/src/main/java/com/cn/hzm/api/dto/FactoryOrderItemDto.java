package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:45 下午
 */
@Data
@ApiModel(description = "工厂订单商品DTO")
public class FactoryOrderItemDto {

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

    @ApiModelProperty(value = "厂家备注")
    private String factoryRemark;

    @ApiModelProperty(value = "商品单价")
    private Double itemPrice;

    @ApiModelProperty(value = "交货数量")
    private Integer deliveryNum;

    @ApiModelProperty(value = "总价")
    private Double totalPrice;

    @ApiModelProperty(value = "实际收货数量")
    private Integer receiveNum;

    @ApiModelProperty(value = "剩余运输中数量")
    private Integer remainNum;

}
