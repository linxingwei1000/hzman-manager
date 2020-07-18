package com.cn.hzm.core.aws.resp.product;

import com.cn.hzm.core.aws.domain.Products;
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
