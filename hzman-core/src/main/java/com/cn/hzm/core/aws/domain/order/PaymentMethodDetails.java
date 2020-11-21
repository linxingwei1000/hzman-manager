package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:23 下午
 */
@Data
@XStreamAlias("PaymentMethodDetails")
public class PaymentMethodDetails {

    @XStreamAlias(value="PaymentMethodDetail")
    private String paymentMethodDetail;

}
