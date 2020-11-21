package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:04 上午
 */
@Data
@XStreamAlias("OrderItems")
public class OrderItems {

    @XStreamImplicit(itemFieldName="OrderItem")
    private List<OrderItem> list;
}
