package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 2:44 下午
 */
@Data
@ApiModel(description = "商品订单DTO")
public class OrderItemDTO {

    private Integer id;

    @ApiModelProperty(value = "amazon订单号")
    private String amazonOrderId;

    @ApiModelProperty(value = "asin")
    private String asin;

    @ApiModelProperty(value="sku")
    private String sku;

    @ApiModelProperty(value="amazon商品订单号")
    private String orderItemId;

    private String title;

    @ApiModelProperty(value="订单数量")
    private Integer quantityOrdered;

    @ApiModelProperty(value="已配送数量")
    private Integer quantityShipped;

    /**
     * 订单商品的售价及税费
     */
    @ApiModelProperty(value="订单费用")
    private Double itemPriceAmount;
    @ApiModelProperty(value="订单货币代码")
    private String itemPriceCurrencyCode;
    @ApiModelProperty(value="订单税费")
    private Double itemTaxAmount;
    @ApiModelProperty(value="订单税费货币代码")
    private String itemTaxCurrencyCode;

    /**
     * 运费及税费
     */
    @ApiModelProperty(value="运费")
    private Double shippingPriceAmount;
    @ApiModelProperty(value="运费货币代码")
    private String shippingPriceCurrencyCode;
    @ApiModelProperty(value="运费税费")
    private Double shippingTaxAmount;
    @ApiModelProperty(value="运费税费货币代码")
    private String shippingTaxCurrencyCode;

//    /**
//     * 商品的礼品包装金额及税费
//     */
//    @ApiModelProperty(value="gift_wrap_price_amount")
//    private Double giftWrapPriceAmount;
//    @ApiModelProperty(value="gift_wrap_price_currency_code")
//    private String giftWrapPriceCurrencyCode;
//    @ApiModelProperty(value="gift_wrap_tax_amount")
//    private Double giftWrapTaxAmount;
//    @ApiModelProperty(value="gift_wrap_tax_currency_code")
//    private String giftWrapTaxCurrencyCode;
//
//    /**
//     * 运费的折扣及税费
//     */
//    @ApiModelProperty(value="shipping_discount_amount")
//    private Double shippingDiscountAmount;
//    @ApiModelProperty(value="shipping_discount_currency_code")
//    private String shippingDiscountCurrencyCode;
//    @ApiModelProperty(value="shipping_discount_tax_amount")
//    private Double shippingDiscountTaxAmount;
//    @ApiModelProperty(value="shipping_discount_tax_currency_code")
//    private String shippingDiscountTaxCurrencyCode;
//
//    /**
//     * 报价中的全部促销折扣总计及税费
//     */
//    @ApiModelProperty(value="promotion_discount_amount")
//    private Double promotionDiscountAmount;
//    @ApiModelProperty(value="promotion_discount_currency_code")
//    private String promotionDiscountCurrencyCode;
//    @ApiModelProperty(value="promotion_discount_tax_amount")
//    private Double promotionDiscountTaxAmount;
//    @ApiModelProperty(value="promotion_discount_tax_currency_code")
//    private String promotionDiscountTaxCurrencyCode;
//
//
//    @ApiModelProperty(value="promotion_ids")
//    private String promotionIds;
//
//    /**
//     * COD 服务费用
//     */
//    @ApiModelProperty(value="cod_fee_amount")
//    private Double codFeeAmount;
//    @ApiModelProperty(value="cod_fee_currency_code")
//    private String codFeeCurrencyCode;
//
//    /**
//     * COD 服务费用折扣
//     */
//    @ApiModelProperty(value="cod_fee_discount_amount")
//    private Double codFeeDiscountAmount;
//    @ApiModelProperty(value="cod_fee_discount_currency_code")
//    private String codFeeDiscountCurrencyCode;
//
//    @ApiModelProperty(value="gift_message_text")
//    private String giftMessageText;
//
//    @ApiModelProperty(value="gift_wrap_level")
//    private String giftWrapLevel;
//
//    @ApiModelProperty(value="invoice_data")
//    private String invoiceData;
//
//    @ApiModelProperty(value="condition_note")
//    private String conditionNote;
//
//    @ApiModelProperty(value="condition_id")
//    private String conditionId;
//
//    @ApiModelProperty(value="condition_subtypeId")
//    private String conditionSubtypeId;
//
//    @ApiModelProperty(value="scheduled_delivery_start_date")
//    private Date scheduledDeliveryStartDate;
//
//    @ApiModelProperty(value="scheduled_delivery_end_date")
//    private Date scheduledDeliveryEndDate;
//
//    @ApiModelProperty(value="tax_collection")
//    private String taxCollection;
//
//    @ApiModelProperty(value="product_info")
//    private String productInfo;
//
//    @ApiModelProperty(value="is_gift")
//    private Integer isGift;
//
//    @ApiModelProperty(value="is_transparency")
//    private Integer isTransparency;
//
//    @ApiModelProperty(value="other_config")
//    private String otherConfig;

}
