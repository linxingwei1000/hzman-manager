package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:15 下午
 */
@Data
@XStreamAlias("Order")
public class Order {

    @XStreamAlias(value="LatestShipDate")
    private String latestShipDate;

    @XStreamAlias(value="OrderType")
    private String orderType;

    @XStreamAlias(value="PurchaseDate")
    private String purchaseDate;

    @XStreamAlias(value="AmazonOrderId")
    private String amazonOrderId;

    /**
     * 新order里面没有此字段
     */
    @XStreamAlias(value="BuyerEmail")
    private String buyerEmail;

    @XStreamAlias(value="BuyerName")
    private String buyerName;

    @XStreamAlias(value="LastUpdateDate")
    private String lastUpdateDate;

    @XStreamAlias(value="IsReplacementOrder")
    private String isReplacementOrder;

    @XStreamAlias(value="NumberOfItemsShipped")
    private String numberOfItemsShipped;

    @XStreamAlias(value="ShipServiceLevel")
    private String shipServiceLevel;

    @XStreamAlias(value="OrderStatus")
    private String orderStatus;

    @XStreamAlias(value="SalesChannel")
    private String salesChannel;

    @XStreamAlias(value="OrderChannel")
    private String orderChannel;

    /**
     * 新order里面没有此字段
     */
    @XStreamAlias(value="ShippedByAmazonTFM")
    private String shippedByAmazonTFM;

    @XStreamAlias(value="TFMShipmentStatus")
    private String tfmShipmentStatus;

    @XStreamAlias(value="IsBusinessOrder")
    private String isBusinessOrder;

    @XStreamAlias(value="NumberOfItemsUnshipped")
    private String numberOfItemsUnshipped;

    @XStreamAlias(value="PaymentMethodDetails")
    private PaymentMethodDetails paymentMethodDetails;

    @XStreamAlias(value="LatestDeliveryDate")
    private String latestDeliveryDate;

    @XStreamAlias(value="IsGlobalExpressEnabled")
    private String isGlobalExpressEnabled;

    @XStreamAlias(value="IsSoldByAB")
    private String isSoldByAB;

    @XStreamAlias(value="EarliestDeliveryDate")
    private String earliestDeliveryDate;

    @XStreamAlias(value="IsPremiumOrder")
    private String isPremiumOrder;

    @XStreamAlias(value="OrderTotal")
    private OrderTotal orderTotal;

    @XStreamAlias(value="EarliestShipDate")
    private String earliestShipDate;

    @XStreamAlias(value="MarketplaceId")
    private String marketplaceId;

    @XStreamAlias(value="DefaultShipFromLocationAddress")
    private DefaultShipFromLocationAddress defaultShipFromLocationAddress;

    @XStreamAlias(value="FulfillmentChannel")
    private String fulfillmentChannel;

    @XStreamAlias(value="PaymentMethod")
    private String paymentMethod;

    @XStreamAlias(value="ShippingAddress")
    private ShippingAddress shippingAddress;

    @XStreamAlias(value="IsISPU")
    private String isISPU;

    @XStreamAlias(value="IsPrime")
    private String isPrime;

    @XStreamAlias(value="SellerOrderId")
    private String sellerOrderId;

    @XStreamAlias(value="ShipmentServiceLevelCategory")
    private String shipmentServiceLevelCategory;

    @XStreamAlias(value="CbaDisplayableShippingLabel")
    private String cbaDisplayableShippingLabel;


    @XStreamAlias(value="ReplacedOrderId")
    private String replacedOrderId;
}
