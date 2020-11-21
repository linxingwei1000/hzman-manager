package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:23 下午
 */
@Data
@XStreamAlias("ShippingAddress")
public class ShippingAddress {

    @XStreamAlias(value="City")
    private String city;

    @XStreamAlias(value="PostalCode")
    private String postalCode;

    @XStreamAlias(value="isAddressSharingConfidential")
    private String isAddressSharingConfidential;

    @XStreamAlias(value="StateOrRegion")
    private String stateOrRegion;

    @XStreamAlias(value="CountryCode")
    private String countryCode;

    @XStreamAlias(value="County")
    private String county;
}
