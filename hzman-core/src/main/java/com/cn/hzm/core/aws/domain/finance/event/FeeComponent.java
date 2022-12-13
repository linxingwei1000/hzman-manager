package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:52 下午
 */
@Data
public class FeeComponent {

    @XStreamAlias(value="FeeAmount")
    private FeeAmount feeAmount;

    @XStreamAlias(value="FeeType")
    private String feeType;
}
