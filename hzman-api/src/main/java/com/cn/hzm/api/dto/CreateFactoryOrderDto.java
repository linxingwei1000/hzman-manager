package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/2 3:40 下午
 */
@Data
@ApiModel(description = "工厂订单创建DTO")
public class CreateFactoryOrderDto {

    @ApiModelProperty(value = "工厂id")
    private Integer factoryId;

    @ApiModelProperty(value = "订单描述")
    private String desc;

    @ApiModelProperty(value = "收货地址")
    private String receiveAddress;

    List<CreateFactoryOrderItemDto> orderItems;
}
