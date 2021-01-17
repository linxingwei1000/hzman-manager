package com.cn.hzm.core.aws.resp.fulfilment;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 11:51 上午
 */
@Data
@XStreamAlias("ListInboundShipmentItemsResponse")
public class ListInboundShipmentItemsResponse {

    @XStreamAlias(value = "ListInboundShipmentItemsResult")
    ListInboundShipmentItemsResult listInboundShipmentItemsResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
