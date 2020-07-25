package com.cn.hzm.core.aws.domain.inventory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:36 下午
 */
@Data
@XStreamAlias("member")
public class Member {

    @XStreamAlias(value="Condition")
    private String condition;

    @XStreamAlias(value="SupplyDetail")
    private String supplyDetail;

    @XStreamAlias(value="TotalSupplyQuantity")
    private Integer totalSupplyQuantity;

    @XStreamAlias(value="EarliestAvailability")
    private EarliestAvailability earliestAvailability;

    @XStreamAlias(value="FNSKU")
    private String fnsku;

    @XStreamAlias(value="InStockSupplyQuantity")
    private Integer inStockSupplyQuantity;

    @XStreamAlias(value="ASIN")
    private String asin;

    @XStreamAlias(value="SellerSKU")
    private String sellerSKU;

}
