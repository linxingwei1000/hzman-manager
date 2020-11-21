package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:24 上午
 */
@Data
@XStreamAlias("ItemPrice")
public class MoneyParam {

    @XStreamAlias(value="Amount")
    private Double amount;

    @XStreamAlias(value="CurrencyCode")
    private String currencyCode;
}
