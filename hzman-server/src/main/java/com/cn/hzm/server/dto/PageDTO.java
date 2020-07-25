package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:31 下午
 */
@Data
@ApiModel(description = "分页DTO")
public class PageDTO {

    Integer pageNum;

    Integer pageSize;
}
