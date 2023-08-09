package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:10 下午
 */
@ApiModel(description = "预留详情DTO")
@Data
public class InventoryReservedDetailDto {

    @ApiModelProperty(value = "预留总数")
    private Integer totalReservedQuantity;

    @ApiModelProperty(value = "配送中心处理")
    private Integer fcProcessingQuantity;

    @ApiModelProperty(value = "卖家订单")
    private Integer pendingCustomerOrderQuantity;

    @ApiModelProperty(value = "配送中心传输")
    private Integer pendingTransshipmentQuantity;

//    public InventoryReservedDetailDto(Integer initNum) {
//        this.totalReservedQuantity = initNum;
//        this.fcProcessingQuantity = initNum;
//        this.pendingCustomerOrderQuantity = initNum;
//        this.pendingTransshipmentQuantity = initNum;
//    }

    public void checkNull() {
        if (totalReservedQuantity == null) {
            totalReservedQuantity = 0;
        }

        if (fcProcessingQuantity == null) {
            fcProcessingQuantity = 0;
        }

        if (pendingCustomerOrderQuantity == null) {
            pendingCustomerOrderQuantity = 0;
        }

        if (pendingTransshipmentQuantity == null) {
            pendingTransshipmentQuantity = 0;
        }
    }
}
