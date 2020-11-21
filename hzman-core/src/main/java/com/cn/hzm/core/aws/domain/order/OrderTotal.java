package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:23 下午
 */
@Data
@XStreamAlias("OrderTotal")
public class OrderTotal {

    @XStreamAlias(value="Amount")
    private String amount;

    @XStreamAlias(value="CurrencyCode")
    private String currencyCode;
}
