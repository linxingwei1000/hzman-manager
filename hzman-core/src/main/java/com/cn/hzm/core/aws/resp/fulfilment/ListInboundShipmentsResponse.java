package com.cn.hzm.core.aws.resp.fulfilment;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 31.7.21 11:08 上午
 */
@Data
@XStreamAlias("ListInboundShipmentsResponse")
public class ListInboundShipmentsResponse {

    @XStreamAlias(value = "ListInboundShipmentsResult")
    ListInboundShipmentsResult listInboundShipmentsResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
