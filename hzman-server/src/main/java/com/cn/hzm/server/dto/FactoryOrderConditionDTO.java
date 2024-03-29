package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "厂家订单条件搜索DTO")
@Data
public class FactoryOrderConditionDTO extends PageDTO{

    private Integer factoryId;

    private Integer orderStatus;
}
