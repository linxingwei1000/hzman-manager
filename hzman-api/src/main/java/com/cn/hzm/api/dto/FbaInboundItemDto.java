package com.cn.hzm.api.dto;

import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:06 下午
 */
@Data
public class FbaInboundItemDto {

    private Integer quantityShipped;

    private String fulfillmentNetworkSKU;

    private String sellerSKU;

    private Integer quantityReceived;

    private Integer quantityInCase;

}
