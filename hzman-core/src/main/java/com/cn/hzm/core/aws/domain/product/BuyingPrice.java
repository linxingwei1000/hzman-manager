package com.cn.hzm.core.aws.domain.product;

import com.cn.hzm.core.aws.domain.order.MoneyParam;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:27 下午
 */
@Data
@XStreamAlias(value="BuyingPrice")
public class BuyingPrice {

    @XStreamAlias(value="LandedPrice")
    MoneyParam landedPrice;

    @XStreamAlias(value="ListingPrice")
    MoneyParam listingPrice;

    @XStreamAlias(value="Shipping")
    MoneyParam shipping;
}
