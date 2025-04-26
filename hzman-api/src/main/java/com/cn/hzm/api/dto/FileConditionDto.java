package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "上传文件条件搜索DTO")
@Data
public class FileConditionDto extends PageDto{

    /**
     * 文件类型
     */
    private Integer fileType;
}
