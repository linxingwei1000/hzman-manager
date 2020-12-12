package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:45 下午
 */
@Data
@ApiModel(description = "工厂订单DTO")
public class FactoryOrderDTO {

    @ApiModelProperty(value = "订单id")
    private Integer id;

    @ApiModelProperty(value = "工厂id")
    private Integer factoryId;

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

    @ApiModelProperty(value = "厂家交货时间")
    private String deliveryDate;

    @ApiModelProperty(value = "运单编号")
    private String waybillNum;

    @ApiModelProperty(value = "实收数量")
    private Integer receiveNum;

    @ApiModelProperty(value = "付款凭证")
    private String paymentVoucher;

    @ApiModelProperty(value = "订单状态")
    private Integer status;

    private String statusDesc;

}
