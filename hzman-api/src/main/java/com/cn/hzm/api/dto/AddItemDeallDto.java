package com.cn.hzm.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linxingwei
 * @date 14.5.23 6:07 下午
 */
@Data
public class AddItemDeallDto implements Serializable {

    private String sku;

    private String asin;

    private Integer FactoryId;

    private Double cost;

    private String remark;
}
