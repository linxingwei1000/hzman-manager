package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.domain.order.OrderItems;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:09 下午
 */
@Data
@XStreamAlias("ListOrderItemsResult")
public class ListOrderItemsResult {

    @XStreamAlias(value="NextToken")
    String nextToken;

    @XStreamAlias(value="AmazonOrderId")
    String amazonOrderId;

    @XStreamAlias(value="OrderItems")
    OrderItems orderItems;

}
