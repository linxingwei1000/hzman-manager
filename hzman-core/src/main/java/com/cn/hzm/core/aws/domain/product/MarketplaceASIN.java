package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:17 下午
 */
@Data
@XStreamAlias("MarketplaceASIN")
public class MarketplaceASIN {

    @XStreamAlias(value="MarketplaceId")
    private String marketplaceId;
    @XStreamAlias(value="ASIN")
    private String asin;
}
