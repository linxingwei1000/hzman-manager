package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/2 3:40 下午
 */
@Data
@ApiModel(description = "工厂订单添加DTO")
public class AddFactoryOrderDTO {

    @ApiModelProperty(value = "工厂订单id")
    private Integer factoryOrderId;

    List<CreateFactoryOrderItemDTO> orderItems;
}
