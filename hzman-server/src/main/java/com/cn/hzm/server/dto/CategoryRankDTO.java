package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 25.9.22 5:18 下午
 */
@Data
@ApiModel(description = "商品类目排名")
public class CategoryRankDTO {

    private String productCategoryId;

    private String productCategoryName;

    private Integer rank;

    private Map<String, CategoryRankDTO> childCategoryMap;

    private List<CategoryRankDTO> childCategory;
}
