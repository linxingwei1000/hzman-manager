package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:45 下午
 */
@Data
@ApiModel(description = "创建工厂订单商品DTO")
public class CreateFactoryOrderItemDto {

    @ApiModelProperty(value = "商品sku")
    private String sku;

    @ApiModelProperty(value = "订单数量")
    private Integer orderNum;

    @ApiModelProperty(value = "备注")
    private String remark;
}
