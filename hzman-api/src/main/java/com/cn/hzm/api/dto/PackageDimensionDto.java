package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author linxingwei
 * @date 26.9.22 11:24 上午
 */
@ApiModel(description = "尺寸DTO")
@Data
public class PackageDimensionDto {

    private String height;

    private String length;

    private String width;

    private String weight;

}
