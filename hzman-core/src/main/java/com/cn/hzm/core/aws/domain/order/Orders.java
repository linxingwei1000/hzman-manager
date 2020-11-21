package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:15 下午
 */
@Data
@XStreamAlias("Orders")
public class Orders {

    @XStreamImplicit(itemFieldName="Order")
    private List<Order> list;
}
