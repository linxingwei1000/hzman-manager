package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/21 5:08 下午
 */
@Data
@XStreamAlias("Error")
public class ProductError {

    @XStreamAlias(value="Type")
    private String type;

    @XStreamAlias(value="Code")
    private String code;

    @XStreamAlias(value="Message")
    private String message;
}
