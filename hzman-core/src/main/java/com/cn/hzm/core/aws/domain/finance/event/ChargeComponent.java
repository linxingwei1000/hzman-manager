package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:56 下午
 */
@Data
public class ChargeComponent {

    @XStreamAlias(value="ChargeAmount")
    private FeeAmount chargeAmount;

    @XStreamAlias(value="ChargeType")
    private String chargeType;
}
