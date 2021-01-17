package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 12:02 下午
 */
@Data
@XStreamAlias("PrepDetails")
public class PrepDetails {

    @XStreamAlias(value="PrepOwner")
    private String prepOwner;

    @XStreamAlias(value="PrepInstruction")
    private String prepInstruction;
}
