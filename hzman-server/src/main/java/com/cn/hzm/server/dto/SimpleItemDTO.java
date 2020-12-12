package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/17 9:50 下午
 */
@ApiModel(description = "简单商品DTO")
@Data
public class SimpleItemDTO{

    @ApiModelProperty(value = "ASIN", example = "B07BGY7HWK")
    private String asin;

    @ApiModelProperty(value = "商品名称", example = "N190301")
    private String title;

    @ApiModelProperty(value = "商品图标", example = "N190301")
    private String icon;

    @ApiModelProperty(value = "SKU", example = "N190301")
    private String sku;
}
