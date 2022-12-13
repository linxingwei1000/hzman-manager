package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:15 下午
 */
@Data
@TableName("hzm_amazon_order")
public class OrderDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "amazon_order_id")
    private String amazonOrderId;

    @TableField(value="seller_order_id")
    private String sellerOrderId;

    @TableField(value="purchase_date")
    private Date purchaseDate;

    @TableField(value="last_update_date")
    private Date lastUpdateDate;

    @TableField(value="order_status")
    private String orderStatus;

    @TableField(value="fulfillment_channel")
    private String fulfillmentChannel;

    @TableField(value="sales_channel")
    private String salesChannel;

    @TableField(value="order_channel")
    private String orderChannel;

    @TableField(value="ship_service_level")
    private String shipServiceLevel;

    @TableField(value="shipping_address")
    private String shippingAddress;

    @TableField(value="order_amount")
    private Double orderAmount;

    @TableField(value="order_currency_code")
    private String orderCurrencyCode;

    @TableField(value="number_shipped")
    private Integer numberOfItemsShipped;

    @TableField(value="number_un_shipped")
    private Integer numberOfItemsUnshipped;

    @TableField(value="payment_detail")
    private String paymentMethodDetails;

    @TableField(value="payment_method")
    private String paymentMethod;

    @TableField(value="marketplace_id")
    private String marketplaceId;

    @TableField(value="buyer_email")
    private String buyerEmail;

    @TableField(value="buyer_name")
    private String buyerName;

    @TableField(value="shipment_service_level_category")
    private String shipmentServiceLevelCategory;

    @TableField(value="shipped_by_amazonTFM")
    private Integer shippedByAmazonTFM;

    @TableField(value="TFM_shipment_status")
    private String tfmShipmentStatus;

    @TableField(value="cba_displayable_shipping_label")
    private String cbaDisplayableShippingLabel;

    @TableField(value="order_type")
    private String orderType;

    @TableField(value="earliest_ship_date")
    private Date earliestShipDate;

    @TableField(value="latest_ship_date")
    private Date latestShipDate;

    @TableField(value="earliest_delivery_date")
    private Date earliestDeliveryDate;

    @TableField(value="latest_delivery_date")
    private Date latestDeliveryDate;

    @TableField(value="is_replacement_order")
    private Integer isReplacementOrder;

    @TableField(value="is_business_order")
    private Integer isBusinessOrder;

    @TableField(value="is_global_express_enabled")
    private Integer isGlobalExpressEnabled;

    @TableField(value="is_sold_byAB")
    private Integer isSoldByAB;

    @TableField(value="is_premium_order")
    private Integer isPremiumOrder;

    @TableField(value="is_ispu")
    private Integer isISPU;

    @TableField(value="is_prime")
    private Integer isPrime;

    @TableField(value="other_config")
    private String otherConfig;

    @TableField(value="is_finance")
    private Integer isFinance;

    private Date ctime;

    private Date utime;

}
