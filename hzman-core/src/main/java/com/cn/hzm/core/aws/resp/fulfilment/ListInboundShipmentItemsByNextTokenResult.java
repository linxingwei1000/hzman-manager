package com.cn.hzm.core.aws.resp.fulfilment;

import com.cn.hzm.core.aws.domain.fulfilment.ItemData;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 12:08 下午
 */
@Data
@XStreamAlias("ListInboundShipmentItemsByNextTokenResult")
public class ListInboundShipmentItemsByNextTokenResult {

    @XStreamAlias(value="NextToken")
    String nextToken;

    @XStreamAlias(value="ItemData")
    ItemData itemData;
}
