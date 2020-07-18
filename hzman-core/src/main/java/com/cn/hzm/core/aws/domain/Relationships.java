package com.cn.hzm.core.aws.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:05 下午
 */
@Data
@XStreamAlias("Relationships")
public class Relationships {

    @XStreamAlias(value="VariationParent")
    private VariationParent variationParent;
}
