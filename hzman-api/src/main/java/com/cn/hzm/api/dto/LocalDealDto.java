package com.cn.hzm.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linxingwei
 * @date 14.5.23 6:03 下午
 */
@Data
public class LocalDealDto implements Serializable {

    private String sku;

    private Integer localNum;
}
