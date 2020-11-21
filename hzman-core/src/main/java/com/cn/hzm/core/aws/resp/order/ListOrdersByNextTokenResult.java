package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.domain.order.Orders;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:55 下午
 */
@Data
@XStreamAlias("ListOrdersByNextTokenResult")
public class ListOrdersByNextTokenResult {

    @XStreamAlias(value="NextToken")
    String nextToken;

    @XStreamAlias(value="Orders")
    Orders orders;

    @XStreamAlias(value="CreatedBefore")
    String createdBefore;
}
