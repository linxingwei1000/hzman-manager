package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author linxingwei
 * @date 6.11.22 4:38 下午
 */
@Data
public class ShipmentEvent {

    @XStreamAlias(value="ShipmentItemList")
    private ShipmentItemList shipmentItemList;

    @XStreamAlias(value="AmazonOrderId")
    private String amazonOrderId;

    @XStreamAlias(value="PostedDate")
    private String postedDate;

    @XStreamAlias(value="MarketplaceName")
    private String marketplaceName;

    @XStreamAlias(value="SellerOrderId")
    private String sellerOrderId;
}
