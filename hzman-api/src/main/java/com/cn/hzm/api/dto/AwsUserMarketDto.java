package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
@ApiModel(description = "亚马逊关联市场DTO")
public class AwsUserMarketDto {

    private Integer id;

    @ApiModelProperty(value = "下拉展示文案")
    private String showText;

    @ApiModelProperty(value = "亚马逊账号Id")
    private Integer awsUserId;

    @ApiModelProperty(value = "市场code列表")
    private String marketId;

    @ApiModelProperty(value = "市场国家")
    private String marketCountry;

    @ApiModelProperty(value = "市场区域")
    private String region;

    @ApiModelProperty(value = "refreshToken")
    private String refreshToken;

    List<AwsSpiderTaskDto> awsSpiderTaskDtos;

}
