package com.cn.hzm.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linxingwei
 * @date 14.5.23 6:07 下午
 */
@Data
public class CostDealDto implements Serializable {

    private String asin;

    private String sku;

    private Double cost;
}
