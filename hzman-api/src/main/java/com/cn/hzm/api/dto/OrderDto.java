package com.cn.hzm.api.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 2:19 下午
 */
@Data
@ApiModel(description = "订单DTO")
public class OrderDto extends RespBaseDto{

    @ApiModelProperty(value = "本地数据自增id")
    private Integer id;

    @ApiModelProperty(value = "amazon订单号")
    private String amazonOrderId;

    @ApiModelProperty(value="买家订单号")
    private String sellerOrderId;

    @ApiModelProperty(value="创建订单日期")
    private Date purchaseDate;

    @ApiModelProperty(value="订单最后更新日期")
    private Date lastUpdateDate;

    @ApiModelProperty(value="订单状态")
    private String orderStatus;

    @ApiModelProperty(value="订单配送方式")
    private String fulfillmentChannel;

    @ApiModelProperty(value="订单的配送地址")
    private JSONObject shippingAddressJson;

    @ApiModelProperty(value="订单费用")
    private Double orderAmount;

    @ApiModelProperty(value="订单货币代码")
    private String orderCurrencyCode;

    @ApiModelProperty(value="已配送商品数量")
    private Integer numberOfItemsShipped;

    @ApiModelProperty(value="未配送商品数量")
    private Integer numberOfItemsUnshipped;

    @ApiModelProperty(value="买家匿名电子邮件地址")
    private String buyerEmail;

    @ApiModelProperty(value="买家姓名")
    private String buyerName;

    @ApiModelProperty(value="订单类型")
    private String orderType;

//    @ApiModelProperty(value="订单中第一件商品销售渠道")
//    private String salesChannel;
//
//    @ApiModelProperty(value="订单中第一件商品订单渠道")
//    private String orderChannel;
//
//    @ApiModelProperty(value="货件服务水平")
//    private String shipServiceLevel;

//    @ApiModelProperty(value="付款方式相关信息")
//    private String paymentMethodDetails;
//
//    @ApiModelProperty(value="订单主要付款方式")
//    private String paymentMethod;

//    @ApiModelProperty(value="订单的配送服务级别分类")
//    private String shipmentServiceLevelCategory;
//
//    @ApiModelProperty(value="是否TFM配送")
//    private Integer shippedByAmazonTFM;
//
//    @ApiModelProperty(value="TFM订单的状态")
//    private String tfmShipmentStatus;
//
//    @ApiModelProperty(value="卖家自定义的配送方式")
//    private String cbaDisplayableShippingLabel;

//    @ApiModelProperty(value="您承诺的订单发货时间范围的第一天")
//    private Date earliestShipDate;
//
//    @ApiModelProperty(value="您承诺的订单发货时间范围的最后一天")
//    private Date latestShipDate;
//
//    @ApiModelProperty(value="您承诺的订单送达时间范围的第一天")
//    private Date earliestDeliveryDate;
//
//    @ApiModelProperty(value="您承诺的订单送达时间范围的最后一天")
//    private Date latestDeliveryDate;
//
//    @ApiModelProperty(value="is_replacement_order")
//    private Integer isReplacementOrder;
//
//    @ApiModelProperty(value="is_business_order")
//    private Integer isBusinessOrder;
//
//    @ApiModelProperty(value="is_global_express_enabled")
//    private Integer isGlobalExpressEnabled;
//
//    @ApiModelProperty(value="is_sold_byAB")
//    private Integer isSoldByAB;
//
//    @ApiModelProperty(value="is_premium_order")
//    private Integer isPremiumOrder;
//
//    @ApiModelProperty(value="is_ispu")
//    private Integer isISPU;
//
//    @ApiModelProperty(value="is_prime")
//    private Integer isPrime;
//
//    @ApiModelProperty(value="other_config")
//    private String otherConfig;

}
