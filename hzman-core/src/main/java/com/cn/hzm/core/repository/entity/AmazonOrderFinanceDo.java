package com.cn.hzm.core.repository.entity;

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
@TableName("hzm_amazon_order_finance")
public class AmazonOrderFinanceDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "amazon_order_id")
    private String amazonOrderId;

    @TableField(value="shipment_event_list")
    private String shipmentEventList;

    @TableField(value="other_event_list")
    private String other_event_list;

    private Date ctime;

    private Date utime;

    //CREATE TABLE hzm.hzm_amanzon_order_finance (
    //  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    //  `amazon_order_id` varchar(255) NOT NULL COMMENT '亚马逊自定义订单编码',
    //  `adhoc_disbursement_event_list` text DEFAULT null COMMENT '特别支付事件',
    //  `adjustment_event_list` text DEFAULT null COMMENT '调整事件',
    //  `affordability_expense_event_list` text DEFAULT null COMMENT '支付费用事件',
    //  `affordability_expense_reversal_event_list` text DEFAULT null COMMENT '支付反转费用事件',
    //  `charge_refund_event_list` text DEFAULT null COMMENT '退款发起事件',
    //  `coupon_payment_event_list` text DEFAULT null COMMENT '息票支付事件',
    //  `debt_recovery_event_list` text DEFAULT null COMMENT '债务回购事件',
    //  `explore_out_of_band_payments_event_list` text DEFAULT null COMMENT '支付带外事件',
    //  `fba_liquidation_event_list` text DEFAULT null COMMENT 'fba清算事件',
    //  `failed_adhoc_disbursement_event_list` text DEFAULT null COMMENT '支付失败事件',
    //  `guarantee_claim_event_list` text DEFAULT null COMMENT '索赔事件',
    //  `imaging_services_fee_event_list` text DEFAULT null COMMENT '影像服务费用事件',
    //  `loan_servicing_event_list` text DEFAULT null COMMENT '贷款服务事件',
    //  `network_commingling_transaction_event_list` text DEFAULT null COMMENT '网络混合事务事件',
    //  `pay_with_amazon_event_list` text DEFAULT null COMMENT '亚马逊支付事件',
    //  `performance_bond_refund_event_list` text DEFAULT null COMMENT '履约保证金退款事件',
    //  `product_ads_payment_event_list` text DEFAULT null COMMENT '产品广告事件',
    //  `refund_event_list` text DEFAULT null COMMENT '调整事件',
    //  `removal_shipment_adjustment_event_list` text DEFAULT null COMMENT '装运调整事件',
    //  `removal_shipment_event_list` text DEFAULT null COMMENT '装运事件',
    //  `rental_transaction_event_list` text DEFAULT null COMMENT '租赁交易事件',
    //  `retrocharge_event_list` text DEFAULT null COMMENT 'retrocharge事件',
    //  `safet_reimbursement_event_list` text DEFAULT null COMMENT 'safet报销事件',
    //  `seller_deal_payment_event_list` text DEFAULT null COMMENT '卖方交易付款事件',
    //  `seller_review_enrollment_payment_event_list` text DEFAULT null COMMENT '审核注册事件',
    //  `service_fee_event_list` text DEFAULT null COMMENT '服务费用事件',
    //  `service_provider_credit_event_list` text DEFAULT null COMMENT '服务商信用事件',
    //  `shipment_event_list` text DEFAULT null COMMENT '货物事件',
    //  `shipment_settle_event_list` text DEFAULT null COMMENT '货物结算事件',
    //  `storage_reservation_billing_event_list` text DEFAULT null COMMENT '存储预约计费事件',
    //  `tds_reimbursement_event_list` text DEFAULT null COMMENT 'tds报销事件',
    //  `tax_withholding_event_list` text DEFAULT null COMMENT '税收扣缴事件',
    //  `chargeback_event_list` text DEFAULT null COMMENT '税收事件',
    //  `trial_shipment_event_list` text DEFAULT null COMMENT '试运事件',
    //  `value_added_service_charge_event_list` text DEFAULT null COMMENT '增值服务费事件',
    //  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    //  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    //  PRIMARY KEY (`id`),
    //  KEY `idx_amazon_order_id` (`amazon_order_id`)) ENGINE=InnoDB COMMENT='订单财务信息表';
}
