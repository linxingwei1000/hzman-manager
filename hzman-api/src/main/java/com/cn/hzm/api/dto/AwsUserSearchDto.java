package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
@ApiModel(description = "亚马逊账号查询DTO")
public class AwsUserSearchDto extends PageDto {

    private String remark;

    private String marketId;

}
