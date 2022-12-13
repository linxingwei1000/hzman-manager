package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/9 6:50 下午
 */
@ApiModel(description = "销量DTO")
@Data
public class SaleInfoDescDTO {

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "日期")
    private String saleDate;

    @ApiModelProperty(value = "订单数量")
    private Integer orderNum;

    @ApiModelProperty(value = "销量")
    private Integer saleNum;

    @ApiModelProperty(value = "销售额")
    private Double saleVolume;

    @ApiModelProperty(value = "税费")
    private Double saleTax;

    @ApiModelProperty(value = "仓储服务费")
    private Double fbaFulfillmentFee;

    @ApiModelProperty(value = "佣金")
    private Double commission;

    @ApiModelProperty(value = "均价")
    private Double unitPrice;

    @ApiModelProperty(value = "净收入")
    private Double income;
}
