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
@ApiModel(description = "工厂订单创建DTO")
public class CreateFactoryOrderDTO {

    @ApiModelProperty(value = "工厂id")
    private Integer factoryId;

    List<CreateFactoryOrderItemDTO> orderItems;
}
