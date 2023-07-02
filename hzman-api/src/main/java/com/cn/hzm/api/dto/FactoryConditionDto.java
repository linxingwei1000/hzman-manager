package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "厂家条件搜索DTO")
@Data
public class FactoryConditionDto extends PageDto{

    private Integer factoryId;

    private Integer orderStatus;

}
