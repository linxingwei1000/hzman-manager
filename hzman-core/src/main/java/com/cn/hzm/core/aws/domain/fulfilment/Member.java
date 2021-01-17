package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 12:00 下午
 */
@Data
@XStreamAlias("member")
public class Member {

    @XStreamAlias(value="QuantityShipped")
    private Integer quantityShipped;

    @XStreamAlias(value="ShipmentId")
    private String shipmentId;

    @XStreamAlias(value="PrepDetailsList")
    private PrepDetailsList prepDetailsList;

    @XStreamAlias(value="FulfillmentNetworkSKU")
    private String fulfillmentNetworkSKU;

    @XStreamAlias(value="SellerSKU")
    private String sellerSKU;

    @XStreamAlias(value="QuantityReceived")
    private Integer quantityReceived;

    @XStreamAlias(value="QuantityInCase")
    private Integer quantityInCase;

}
