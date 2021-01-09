package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/12/21 11:23 上午
 */
@Data
@ApiModel(description = "工厂制作商品数量")
public class FactoryQuantityDTO {

    private Integer num;

    @ApiModelProperty(value = "厂家交货时间")
    private String deliveryDate;
}
