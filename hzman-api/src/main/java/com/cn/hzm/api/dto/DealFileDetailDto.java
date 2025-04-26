package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@ApiModel(description = "文件处理详情DTO")
public class DealFileDetailDto {

    @ApiModelProperty(value = "记录id")
    private Integer id;

    @ApiModelProperty(value = "文件id")
    private Integer fileId;

    private String asin;

    private String sku;

    private String fnsku;

    private Integer dealNum;

    @ApiModelProperty(value = "处理状态")
    private Integer dealStatus;
    private String strDealStatus;

    private String dealResult;

    private String createTime;
}
