package com.cn.hzm.core.aws.domain.product;

import com.cn.hzm.core.aws.domain.order.MoneyParam;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:25 下午
 */
@Data
@XStreamAlias(value="Offer")
public class Offer {

    @XStreamAlias(value="BuyingPrice")
    BuyingPrice buyingPrice;

    @XStreamAlias(value="RegularPrice")
    MoneyParam regularPrice;

    @XStreamAlias(value="FulfillmentChannel")
    String fulfillmentChannel;

    @XStreamAlias(value="ItemCondition")
    String itemCondition;

    @XStreamAlias(value="ItemSubCondition")
    String itemSubCondition;

    @XStreamAlias(value="SellerId")
    String SellerId;

    @XStreamAlias(value="SellerSKU")
    String sellerSKU;
}
