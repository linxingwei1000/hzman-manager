package com.cn.hzm.core.aws.resp.product;

import com.cn.hzm.core.aws.domain.product.ProductError;
import com.cn.hzm.core.aws.domain.product.Products;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 4:48 下午
 */
@Data
@XStreamAlias("GetMatchingProductForIdResult")
public class GetMatchingProductForIdResult {

    @XStreamAlias(value = "Products")
    Products products;

    @XStreamAlias(value = "Error")
    ProductError error;
}
