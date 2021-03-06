package com.cn.hzm.core.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:14 下午
 */
@Data
@XStreamAlias("Products")
public class Product {

    @XStreamAlias(value="Identifiers")
    Identifiers identifiers;
}
