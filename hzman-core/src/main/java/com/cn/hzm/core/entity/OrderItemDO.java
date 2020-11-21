package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:05 上午
 */
@Data
@TableName("hzm_amazon_order_item")
public class OrderItemDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "amazon_order_id")
    private String amazonOrderId;

    private String asin;

    @TableField(value="sku")
    private String sku;

    @TableField(value="order_item_id")
    private String orderItemId;

    private String title;

    @TableField(value="quantity_ordered")
    private Integer quantityOrdered;

    @TableField(value="quantity_shipped")
    private Integer quantityShipped;

    /**
     * 订单商品的售价及税费
     */
    @TableField(value="item_price_amount")
    private Double itemPriceAmount;
    @TableField(value="item_price_currency_code")
    private String itemPriceCurrencyCode;
    @TableField(value="item_tax_amount")
    private Double itemTaxAmount;
    @TableField(value="item_tax_currency_code")
    private String itemTaxCurrencyCode;

    /**
     * 运费及税费
     */
    @TableField(value="shipping_price_amount")
    private Double shippingPriceAmount;
    @TableField(value="shipping_price_currency_code")
    private String shippingPriceCurrencyCode;
    @TableField(value="shipping_tax_amount")
    private Double shippingTaxAmount;
    @TableField(value="shipping_tax_currency_code")
    private String shippingTaxCurrencyCode;

    /**
     * 商品的礼品包装金额及税费
     */
    @TableField(value="gift_wrap_price_amount")
    private Double giftWrapPriceAmount;
    @TableField(value="gift_wrap_price_currency_code")
    private String giftWrapPriceCurrencyCode;
    @TableField(value="gift_wrap_tax_amount")
    private Double giftWrapTaxAmount;
    @TableField(value="gift_wrap_tax_currency_code")
    private String giftWrapTaxCurrencyCode;

    /**
     * 运费的折扣及税费
     */
    @TableField(value="shipping_discount_amount")
    private Double shippingDiscountAmount;
    @TableField(value="shipping_discount_currency_code")
    private String shippingDiscountCurrencyCode;
    @TableField(value="shipping_discount_tax_amount")
    private Double shippingDiscountTaxAmount;
    @TableField(value="shipping_discount_tax_currency_code")
    private String shippingDiscountTaxCurrencyCode;

    /**
     * 报价中的全部促销折扣总计及税费
     */
    @TableField(value="promotion_discount_amount")
    private Double promotionDiscountAmount;
    @TableField(value="promotion_discount_currency_code")
    private String promotionDiscountCurrencyCode;
    @TableField(value="promotion_discount_tax_amount")
    private Double promotionDiscountTaxAmount;
    @TableField(value="promotion_discount_tax_currency_code")
    private String promotionDiscountTaxCurrencyCode;


    @TableField(value="promotion_ids")
    private String promotionIds;

    /**
     * COD 服务费用
     */
    @TableField(value="cod_fee_amount")
    private Double codFeeAmount;
    @TableField(value="cod_fee_currency_code")
    private String codFeeCurrencyCode;

    /**
     * COD 服务费用折扣
     */
    @TableField(value="cod_fee_discount_amount")
    private Double codFeeDiscountAmount;
    @TableField(value="cod_fee_discount_currency_code")
    private String codFeeDiscountCurrencyCode;

    @TableField(value="gift_message_text")
    private String giftMessageText;

    @TableField(value="gift_wrap_level")
    private String giftWrapLevel;

    @TableField(value="invoice_data")
    private String invoiceData;

    @TableField(value="condition_note")
    private String conditionNote;

    @TableField(value="condition_id")
    private String conditionId;

    @TableField(value="condition_subtypeId")
    private String conditionSubtypeId;

    @TableField(value="scheduled_delivery_start_date")
    private Date scheduledDeliveryStartDate;

    @TableField(value="scheduled_delivery_end_date")
    private Date scheduledDeliveryEndDate;

    @TableField(value="tax_collection")
    private String taxCollection;

    @TableField(value="product_info")
    private String productInfo;

    @TableField(value="is_gift")
    private Integer isGift;

    @TableField(value="is_transparency")
    private Integer isTransparency;

    @TableField(value="other_config")
    private String otherConfig;

    private Date ctime;

    private Date utime;
}
