package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/11 1:50 下午
 */
@ApiModel(description = "厂家确认订单DTO")
@Data
public class FactoryConfirmDTO {

    @ApiModelProperty(value = "订单id")
    private Integer orderId;

    @ApiModelProperty(value = "交货日期yyyy-mm-dd")
    private String deliveryDate;

    @ApiModelProperty(value = "订单商品")
    private List<FactoryOrderItemDTO> orderItems;
}
