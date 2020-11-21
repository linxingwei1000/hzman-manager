package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.domain.order.Orders;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:09 下午
 */
@Data
@XStreamAlias("ListOrdersResult")
public class ListOrdersResult {

    @XStreamAlias(value="NextToken")
    String nextToken;

    @XStreamAlias(value="Orders")
    Orders orders;

    @XStreamAlias(value="CreatedBefore")
    String createdBefore;
}
