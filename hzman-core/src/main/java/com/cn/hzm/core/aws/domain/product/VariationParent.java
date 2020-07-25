package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:52 下午
 */
@Data
@XStreamAlias("VariationParent")
public class VariationParent {

    @XStreamAlias(value="Identifiers")
    private Identifiers identifiers;
}
