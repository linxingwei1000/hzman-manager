package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
@ApiModel(description = "亚马逊市场爬取DTO")
public class AwsSpiderTaskDto {

    private Integer id;

    @ApiModelProperty(value = "账号市场关联关系id")
    private Integer userMarketId;

    @ApiModelProperty(value = "爬取类型")
    private Integer spiderType;

    @ApiModelProperty(value = "爬取依赖时间：yyyy-mm-ddThh:mm:ssZ")
    private String spiderDepend;

    @ApiModelProperty(value = "激活状态")
    private Integer active;
}