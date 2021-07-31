package com.cn.hzm.core.aws.resp.fulfilment;

import com.cn.hzm.core.aws.domain.fulfilment.ShipmentData;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 12:08 下午
 */
@Data
@XStreamAlias("ListInboundShipmentsByNextTokenResult")
public class ListInboundShipmentsByNextTokenResult {

    @XStreamAlias(value="NextToken")
    private String nextToken;

    @XStreamAlias(value="ShipmentData")
    private ShipmentData shipmentData;
}
