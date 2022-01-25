package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "商品条件搜索DTO")
@Data
public class ItemConditionDTO extends PageDTO{

    /**
     * 搜索类型
     */
    private Integer searchType;

    /**
     * key 模糊查询
     */
    private String key;

    /**
     * 排序
     */
    private Integer itemSortType;

    /**
     * 展示类型：1.父类sku，2.子类sku
     */
    private Integer showType;
}
