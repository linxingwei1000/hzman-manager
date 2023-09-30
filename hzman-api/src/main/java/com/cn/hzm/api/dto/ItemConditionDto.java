package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:57 下午
 */
@ApiModel(description = "商品条件搜索DTO")
@Data
public class ItemConditionDto extends PageDto{

    /**
     * 状态类型
     * 0：全部商品（子体）
     * 1：补货商品
     * 2：订货商品
     * 3：父体
     *
     * 4：备注
     * 5：未备注
     *
     * 6：透明计划（代开发）
     * 7：被跟卖（代开发）
     *
     *
     * 8：在售（待定）
     * 9：停售（待定）
     *
     */
    private Integer statusType;

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
     * 上架时间过滤排序
     */
    private String startListingTime;
    private String endListingTime;
    private Integer listingTimeSortType;

    /**
     * 排序
     */
    private Integer itemSortType;
}
