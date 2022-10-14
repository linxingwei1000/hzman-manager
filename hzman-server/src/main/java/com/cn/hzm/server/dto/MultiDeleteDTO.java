package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linxingwei
 * @date 2.10.22 1:59 下午
 */
@Data
public class MultiDeleteDTO {

    @ApiModelProperty(value = "ASIN", example = "B07BGY7HWK")
    private String asin;

    @ApiModelProperty(value = "SKU", example = "N190301")
    private String sku;
}
