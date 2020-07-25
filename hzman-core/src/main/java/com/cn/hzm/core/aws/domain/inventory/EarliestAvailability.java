package com.cn.hzm.core.aws.domain.inventory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:40 下午
 */
@Data
@XStreamAlias("EarliestAvailability")
public class EarliestAvailability {

    @XStreamAlias(value="TimepointType")
    private String timepointType;
}
