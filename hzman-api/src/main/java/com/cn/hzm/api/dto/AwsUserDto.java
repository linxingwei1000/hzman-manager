package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
@ApiModel(description = "亚马逊账号DTO")
public class AwsUserDto {

    private Integer id;

    @ApiModelProperty(value = "账号备注，唯一")
    private String remark;

    @ApiModelProperty(value = "sellerId")
    private String sellerId;

    @ApiModelProperty(value = "accessKeyId")
    private String accessKeyId;

    @ApiModelProperty(value = "secretKey")
    private String secretKey;

    @ApiModelProperty(value = "roleArn")
    private String roleArn;

    @ApiModelProperty(value = "clientId")
    private String clientId;

    @ApiModelProperty(value = "clientSecret")
    private String clientSecret;

    @ApiModelProperty(value = "关联市场列表")
    List<AwsUserMarketDto> marketDtos;

    private Date ctime;

    private Date utime;

}
