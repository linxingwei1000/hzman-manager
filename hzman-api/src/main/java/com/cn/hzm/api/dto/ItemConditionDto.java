package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "商品条件搜索DTO")
@Data
public class ItemConditionDto extends PageDto{

    /**
     * 展示类型：1.父类sku，2.子类sku
     */
    private Integer showType;

    /**
     * 商品状态：0.全部商品，1.补货商品，2.订货商品
     */
    private Integer itemStatusType;

    /**
     * 厂家id
     */
    private Integer factoryId;

    /**
     * sku或asin过滤
     */
    private String key;

    /**
     * title过滤
     */
    private String title;

    /**
     * 商品类型
     */
    private String itemType;

    /**
     * 排序
     */
    private Integer itemSortType;
}
