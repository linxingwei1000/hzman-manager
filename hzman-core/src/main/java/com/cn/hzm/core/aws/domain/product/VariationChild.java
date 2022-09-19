package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:52 下午
 */
@Data
@XStreamAlias("ns2:VariationChild")
public class VariationChild {

    @XStreamAlias(value="Identifiers")
    private Identifiers identifiers;

    @XStreamAlias(value="ns2:Color")
    private String color;

    @XStreamAlias(value="ns2:RingSize")
    private String ringSize;

    @XStreamAlias(value="ns2:MetalType")
    private String metalType;

    @XStreamAlias(value="ns2:MaterialType")
    private String materialType;

    @XStreamAlias(value="ns2:ItemDimensions")
    private String itemDimensions;

    @XStreamAlias(value="ns2:Size")
    private String size;

}
