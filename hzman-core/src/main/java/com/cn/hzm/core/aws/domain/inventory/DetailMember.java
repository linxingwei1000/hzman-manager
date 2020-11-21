package com.cn.hzm.core.aws.domain.inventory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/20 2:41 下午
 */
@Data
@XStreamAlias(value="member")
public class DetailMember {


    @XStreamAlias(value="LatestAvailableToPick")
    private EarliestAvailability latestAvailableToPick;

    @XStreamAlias(value="EarliestAvailableToPick")
    private EarliestAvailability earliestAvailability;

    @XStreamAlias(value="Quantity")
    private Integer quantity;

    @XStreamAlias(value="SupplyType")
    private String supplyType;
}
