package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "销量搜索DTO")
@Data
public class SaleConditionDTO{

    /**
     * 搜索类型
     */
    private String sku;

    /**
     * 搜索类型
     */
    private String beginDate;

    /**
     * key 模糊查询
     */
    private String endDate;
}
