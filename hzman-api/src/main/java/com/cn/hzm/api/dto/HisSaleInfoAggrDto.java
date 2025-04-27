package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linxingwei
 * @date 27.4.25 6:10 下午
 */
@Data
public class HisSaleInfoAggrDto {

    @ApiModelProperty(value = "最近30天数据")
    private SaleInfoDto duration30Day;

    @ApiModelProperty(value = "最近30天到60数据")
    private SaleInfoDto duration3060Day;

    @ApiModelProperty(value = "去年同期30天数据")
    private SaleInfoDto lastYearDuration30Day;

}
