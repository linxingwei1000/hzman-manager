package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

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

    @ApiModelProperty(value = "工厂名")
    private String factoryName;

    @ApiModelProperty(value = "厂家交货时间")
    private String deliveryDate;

    @ApiModelProperty(value = "运单编号")
    private String waybillNum;

    @ApiModelProperty(value = "付款凭证")
    private String paymentVoucher;

    @ApiModelProperty(value = "订单状态")
    private Integer status;

    private String statusDesc;

    private String orderDesc;

    @ApiModelProperty(value = "收货地址")
    private String receiveAddress;

    @ApiModelProperty(value = "订单总量")
    private Integer totalNum;

    @ApiModelProperty(value = "订单总价")
    private Double totalPrice;

    List<FactoryOrderItemDTO> orderItems;

    @ApiModelProperty(value = "订单创建时间")
    private String orderCreateTime;

}
