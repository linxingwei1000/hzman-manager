package com.cn.hzm.core.aws.domain.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:05 上午
 */
@Data
@XStreamAlias("OrderItem")
public class OrderItem {

    @XStreamAlias(value="ASIN")
    private String asin;

    @XStreamAlias(value="SellerSKU")
    private String sellerSKU;

    @XStreamAlias(value="OrderItemId")
    private String orderItemId;

    @XStreamAlias(value="Title")
    private String title;

    @XStreamAlias(value="QuantityOrdered")
    private String quantityOrdered;

    /**
     * 已配送商品数量
     */
    @XStreamAlias(value="QuantityShipped")
    private String quantityShipped;


    /**
     * 订单商品的售价
     */
    @XStreamAlias(value="ItemPrice")
    private MoneyParam itemPrice;

    /**
     * 商品价格的税费
     */
    @XStreamAlias(value="ItemTax")
    private MoneyParam itemTax;

    /**
     * 运费
     */
    @XStreamAlias(value="ShippingPrice")
    private MoneyParam shippingPrice;


    /**
     * 运费税费
     */
    @XStreamAlias(value="ShippingTax")
    private MoneyParam shippingTax;

    /**
     * 商品的礼品包装金额
     */
    @XStreamAlias(value="GiftWrapPrice")
    private MoneyParam giftWrapPrice;

    /**
     * 礼品包装金额的税费
     */
    @XStreamAlias(value="GiftWrapTax")
    private MoneyParam giftWrapTax;

    /**
     * 运费的折扣
     */
    @XStreamAlias(value="ShippingDiscount")
    private MoneyParam shippingDiscount;

    @XStreamAlias(value="ShippingDiscountTax")
    private MoneyParam shippingDiscountTax;

    /**
     * 报价中的全部促销折扣总计
     */
    @XStreamAlias(value="PromotionDiscount")
    private MoneyParam promotionDiscount;

    @XStreamAlias(value="PromotionDiscountTax")
    private MoneyParam promotionDiscountTax;

    @XStreamAlias(value="PromotionIds")
    private PromotionIds promotionIds;

    @XStreamAlias(value="CODFee")
    private MoneyParam codFee;

    @XStreamAlias(value="CODFeeDiscount")
    private MoneyParam codFeeDiscount;


    @XStreamAlias(value="GiftMessageText")
    private String giftMessageText;

    @XStreamAlias(value="GiftWrapLevel")
    private String giftWrapLevel;

    @XStreamAlias(value="InvoiceData")
    private InvoiceData invoiceData;

    @XStreamAlias(value="ConditionNote")
    private String conditionNote;

    @XStreamAlias(value="ConditionId")
    private String conditionId;

    @XStreamAlias(value="ConditionSubtypeId")
    private String conditionSubtypeId;

    @TableField(value="ScheduledDeliveryStartDate")
    private String scheduledDeliveryStartDate;

    @TableField(value="ScheduledDeliveryEndDate")
    private String scheduledDeliveryEndDate;

    @XStreamAlias(value="TaxCollection")
    private TaxCollection taxCollection;


    @XStreamAlias(value="IsGift")
    private String isGift;


    @XStreamAlias(value="IsTransparency")
    private String isTransparency;

    @XStreamAlias(value="ProductInfo")
    private ProductInfo productInfo;

    @XStreamAlias(value="PriceDesignation")
    private String priceDesignation;

}
