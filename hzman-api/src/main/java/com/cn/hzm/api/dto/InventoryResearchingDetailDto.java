package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:10 下午
 */
@ApiModel(description = "研究详情DTO")
@Data
public class InventoryResearchingDetailDto {

    @ApiModelProperty(value = "预留总数")
    private Integer totalReservedQuantity;

    @ApiModelProperty(value = "配送中心处理")
    private Integer fcProcessingQuantity;

    @ApiModelProperty(value = "卖家订单")
    private Integer pendingCustomerOrderQuantity;

    @ApiModelProperty(value = "配送中心传输")
    private Integer pendingTransshipmentQuantity;
}
