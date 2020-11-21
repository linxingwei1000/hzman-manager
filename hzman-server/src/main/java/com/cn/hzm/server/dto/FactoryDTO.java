package com.cn.hzm.server.dto;

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
public class FactoryDTO extends RespBaseDTO{

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

    @ApiModelProperty(value = "工厂订单")
    List<FactoryOrderDTO> orderList;

}
