package com.cn.hzm.server.dto;

import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/1 4:06 下午
 */
@Data
public class FactoryClaimDTO {

    private Integer factoryId;

    private String sku;

    private String desc;
}
