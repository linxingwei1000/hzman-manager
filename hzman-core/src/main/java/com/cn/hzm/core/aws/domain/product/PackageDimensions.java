package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:00 下午
 */
@Data
@XStreamAlias(value="ns2:PackageDimensions")
public class PackageDimensions {

    @XStreamAlias(value="ns2:Height")
    private String height;

    @XStreamAlias(value="ns2:Length")
    private String length;

    @XStreamAlias(value="ns2:Width")
    private String width;

    @XStreamAlias(value="ns2:Weight")
    private String weight;
}
