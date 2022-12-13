package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:53 下午
 */
@Data
public class FeeAmount {

    @XStreamAlias(value="CurrencyAmount")
    private Double currencyAmount;

    @XStreamAlias(value="CurrencyCode")
    private String currencyCode;
}
