package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 31.7.21 11:16 上午
 */
@Data
@XStreamAlias("ShipFromAddress")
public class ShipFromAddress {

    @XStreamAlias(value="City")
    private String city;

    @XStreamAlias(value="CountryCode")
    private String countryCode;

    @XStreamAlias(value="PostalCode")
    private String postalCode;

    @XStreamAlias(value="AddressLine1")
    private String addressLine1;

    @XStreamAlias(value="AddressLine2")
    private String addressLine2;

    @XStreamAlias(value="StateOrProvinceCode")
    private String stateOrProvinceCode;

    @XStreamAlias(value="DistrictOrCounty")
    private String districtOrCounty;
}
