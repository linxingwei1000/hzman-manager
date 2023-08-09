package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:10 下午
 */
@ApiModel(description = "不可售详情DTO")
@Data
public class InventoryUnfulfillableDetailDto {

    @ApiModelProperty(value = "不可售总数")
    private Integer totalUnfulfillableQuantity;

    @ApiModelProperty(value = "运输中损坏")
    private Integer carrierDamagedQuantity;

    @ApiModelProperty(value = "买家损坏")
    private Integer customerDamagedQuantity;

    @ApiModelProperty(value = "有瑕疵")
    private Integer defectiveQuantity;

    @ApiModelProperty(value = "平台损坏")
    private Integer distributorDamagedQuantity;

    @ApiModelProperty(value = "过期")
    private Integer expiredQuantity;

    @ApiModelProperty(value = "仓库损坏")
    private Integer warehouseDamagedQuantity;

//    public InventoryUnfulfillableDetailDto(Integer initNum){
//        this.totalUnfulfillableQuantity = initNum;
//        this.carrierDamagedQuantity = initNum;
//        this.customerDamagedQuantity = initNum;
//        this.defectiveQuantity = initNum;
//        this.distributorDamagedQuantity = initNum;
//        this.expiredQuantity = initNum;
//        this.warehouseDamagedQuantity = initNum;
//    }

    public void checkNull() {
        if (totalUnfulfillableQuantity == null) {
            totalUnfulfillableQuantity = 0;
        }

        if (carrierDamagedQuantity == null) {
            carrierDamagedQuantity = 0;
        }

        if (customerDamagedQuantity == null) {
            customerDamagedQuantity = 0;
        }

        if (defectiveQuantity == null) {
            defectiveQuantity = 0;
        }

        if (distributorDamagedQuantity == null) {
            distributorDamagedQuantity = 0;
        }

        if (expiredQuantity == null) {
            expiredQuantity = 0;
        }

        if (warehouseDamagedQuantity == null) {
            warehouseDamagedQuantity = 0;
        }
    }

}
