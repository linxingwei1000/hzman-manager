package com.cn.hzm.api.dto;

import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/2 11:26 上午
 */
@Data
public class SmartReplenishmentDto {

    String sku;

    Long needNum;

    Integer replenishmentCode;

    String replenishmentDesc;
}
