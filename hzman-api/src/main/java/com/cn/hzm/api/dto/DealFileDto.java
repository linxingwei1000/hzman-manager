package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@ApiModel(description = "文件DTO")
public class DealFileDto extends RespBaseDto{

    @ApiModelProperty(value = "文件id")
    private Integer id;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件类型")
    private Integer fileType;
    private String strFileType;

    @ApiModelProperty(value = "文件处理状态")
    private Integer dealStatus;
    private String strDealStatus;

    private String createTime;
}
