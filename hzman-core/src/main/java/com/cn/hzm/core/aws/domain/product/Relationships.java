package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:05 下午
 */
@Data
@XStreamAlias("Relationships")
public class Relationships {

    @XStreamAlias(value="VariationParent")
    private VariationParent variationParent;

    @XStreamImplicit(itemFieldName="ns2:VariationChild")
    private List<VariationChild> variationChildrens;
}
