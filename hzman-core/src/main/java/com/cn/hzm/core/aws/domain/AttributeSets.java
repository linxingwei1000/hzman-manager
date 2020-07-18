package com.cn.hzm.core.aws.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/8 12:10 上午
 */
@Data
@XStreamAlias("AttributeSets")
public class AttributeSets {

    @XStreamAlias(value="ns2:ItemAttributes")
    private ItemAttributes itemAttributes;
}
