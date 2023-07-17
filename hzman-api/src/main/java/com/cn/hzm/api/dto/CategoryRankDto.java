package com.cn.hzm.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 25.9.22 5:18 下午
 */
@Data
@ApiModel(description = "商品类目排名")
public class CategoryRankDto {

    private String categoryTitle;

    private String categoryLink;

    private Integer categoryRank;

}
