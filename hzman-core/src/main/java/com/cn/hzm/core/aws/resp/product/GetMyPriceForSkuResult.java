package com.cn.hzm.core.aws.resp.product;

import com.cn.hzm.core.aws.domain.product.Product;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:19 下午
 */
@Data
@XStreamAlias("GetMyPriceForSKUResult")
public class GetMyPriceForSkuResult {

    @XStreamAlias(value="Product")
    private Product product;

}
