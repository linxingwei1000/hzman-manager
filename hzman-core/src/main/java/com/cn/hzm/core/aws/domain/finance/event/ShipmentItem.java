package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:46 下午
 */
@Data
public class ShipmentItem {

    @XStreamAlias(value="ItemTaxWithheldList")
    private String itemTaxWithheldList;

    @XStreamAlias(value="ItemChargeList")
    private ItemChargeList itemChargeList;

    @XStreamAlias(value="ItemFeeList")
    private ItemFeeList itemFeeList;

    @XStreamAlias(value="OrderItemId")
    private String orderItemId;

    @XStreamAlias(value="QuantityShipped")
    private Integer quantityShipped;

    @XStreamAlias(value="SellerSKU")
    private String sellerSKU;

    @XStreamAlias(value="PromotionList")
    private PromotionList promotionList;

}
