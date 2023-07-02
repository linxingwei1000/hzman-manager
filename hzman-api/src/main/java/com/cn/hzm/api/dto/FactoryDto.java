package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 5:05 下午
 */
@Data
@ApiModel(description = "工厂DTO")
public class FactoryDto extends RespBaseDto{

    @ApiModelProperty(value = "工厂id")
    private Integer id;

    @ApiModelProperty(value = "工厂名")
    private String factoryName;

    @ApiModelProperty(value = "工厂地址")
    private String address;

    @ApiModelProperty(value = "联系人")
    private String contactPerson;

    @ApiModelProperty(value = "工厂联系方式")
    private String contactInfo;

    private String wx;

    @ApiModelProperty(value = "收款方式")
    private String collectMethod;

    @ApiModelProperty(value = "工厂订单")
    List<FactoryOrderDto> orderList;

}
