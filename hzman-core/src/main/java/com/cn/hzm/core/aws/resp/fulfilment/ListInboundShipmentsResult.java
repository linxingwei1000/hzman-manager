package com.cn.hzm.core.aws.resp.fulfilment;

import com.cn.hzm.core.aws.domain.fulfilment.ShipmentData;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 31.7.21 11:12 上午
 */
@Data
@XStreamAlias("ListInboundShipmentsResult")
public class ListInboundShipmentsResult {

    @XStreamAlias(value="NextToken")
    String nextToken;

    @XStreamAlias(value="ShipmentData")
    ShipmentData shipmentData;
}
