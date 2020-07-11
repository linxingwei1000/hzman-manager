package com.cn.hzm.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:10 下午
 */
@Data
@XStreamAlias("ListMatchingProductsResult")
public class ListMatchingProductsResult {

    @XStreamAlias(value="Products")
    Products products;
}
