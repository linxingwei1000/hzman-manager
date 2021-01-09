package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/7 10:27 上午
 */
@Data
@XStreamAlias("DefaultShipFromLocationAddress")
public class DefaultShipFromLocationAddress {

    @XStreamAlias(value="City")
    private String city;

    @XStreamAlias(value="PostalCode")
    private String postalCode;

    @XStreamAlias(value="isAddressSharingConfidential")
    private String isAddressSharingConfidential;

    @XStreamAlias(value="StateOrRegion")
    private String stateOrRegion;

    @XStreamAlias(value="Phone")
    private String phone;

    @XStreamAlias(value="CountryCode")
    private String countryCode;

    @XStreamAlias(value="Name")
    private String name;

    @XStreamAlias(value="AddressLine1")
    private String addressLine1;

    @XStreamAlias(value="AddressLine2")
    private String addressLine2;
}
