package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:39 下午
 */
@Data
@XStreamAlias("SKUIdentifier")
public class SKUIdentifier {

    @XStreamAlias(value="MarketplaceId")
    private String marketplaceId;

    @XStreamAlias(value="SellerId")
    String SellerId;

    @XStreamAlias(value="SellerSKU")
    String sellerSKU;
}
