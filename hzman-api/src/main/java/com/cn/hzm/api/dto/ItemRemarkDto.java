package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
public class ItemRemarkDto {

    private Integer id;

    @ApiModelProperty(value = "商品id")
    private Integer itemId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date ctime;

    @ApiModelProperty(value = "修改时间")
    private Date utime;

}
