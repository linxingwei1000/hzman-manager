package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "销量搜索DTO")
@Data
public class SaleConditionDto {

    /**
     * 搜索类型
     */
    private String sku;

    /**
     * 用户站点id
     */
    private Integer userMarketId;

    /**
     * 销量数据类型:1.天级别，2.月级别
     */
    private Integer type;

    /**
     * 搜索类型
     */
    private String beginDate;

    /**
     * key 模糊查询
     */
    private String endDate;
}
