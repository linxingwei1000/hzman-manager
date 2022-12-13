package com.cn.hzm.core.aws.domain.finance;

import com.cn.hzm.core.aws.domain.finance.event.ShipmentEventList;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:21 下午
 */
@Data
@XStreamAlias("FinancialEvents")
public class FinancialEvents {

    @XStreamAlias(value="ProductAdsPaymentEventList")
    private String productAdsPaymentEventList;

    @XStreamAlias(value="RentalTransactionEventList")
    private String rentalTransactionEventList;

    @XStreamAlias(value="ServiceFeeEventList")
    private String serviceFeeEventList;

    @XStreamAlias(value="ShipmentSettleEventList")
    private String shipmentSettleEventList;

    @XStreamAlias(value="ServiceProviderCreditEventList")
    private String serviceProviderCreditEventList;

    @XStreamAlias(value="ImagingServicesFeeEventList")
    private String imagingServicesFeeEventList;

    @XStreamAlias(value="SellerDealPaymentEventList")
    private String sellerDealPaymentEventList;

    @XStreamAlias(value="SellerReviewEnrollmentPaymentEventList")
    private String sellerReviewEnrollmentPaymentEventList;

    @XStreamAlias(value="DebtRecoveryEventList")
    private String debtRecoveryEventList;

    @XStreamAlias(value="ShipmentEventList")
    private ShipmentEventList shipmentEventList;

    @XStreamAlias(value="ExploreOutOfBandPaymentsEventList")
    private String exploreOutOfBandPaymentsEventList;

    @XStreamAlias(value="TaxWithholdingEventList")
    private String taxWithholdingEventList;

    @XStreamAlias(value="GuaranteeClaimEventList")
    private String guaranteeClaimEventList;

    @XStreamAlias(value="TDSReimbursementEventList")
    private String tDSReimbursementEventList;

    @XStreamAlias(value="ChargebackEventList")
    private String chargebackEventList;

    @XStreamAlias(value="NetworkComminglingTransactionEventList")
    private String networkComminglingTransactionEventList;

    @XStreamAlias(value="LoanServicingEventList")
    private String loanServicingEventList;

    @XStreamAlias(value="RefundEventList")
    private String refundEventList;

    @XStreamAlias(value="RemovalShipmentEventList")
    private String removalShipmentEventList;

    @XStreamAlias(value="PerformanceBondRefundEventList")
    private String performanceBondRefundEventList;

    @XStreamAlias(value="FailedAdhocDisbursementEventList")
    private String failedAdhocDisbursementEventList;

    @XStreamAlias(value="AffordabilityExpenseReversalEventList")
    private String affordabilityExpenseReversalEventList;

    @XStreamAlias(value="PayWithAmazonEventList")
    private String payWithAmazonEventList;

    @XStreamAlias(value="AdhocDisbursementEventList")
    private String adhocDisbursementEventList;

    @XStreamAlias(value="CouponPaymentEventList")
    private String couponPaymentEventList;

    @XStreamAlias(value="ChargeRefundEventList")
    private String chargeRefundEventList;

    @XStreamAlias(value="RetrochargeEventList")
    private String retrochargeEventList;

    @XStreamAlias(value="TrialShipmentEventList")
    private String trialShipmentEventList;

    @XStreamAlias(value="ValueAddedServiceChargeEventList")
    private String valueAddedServiceChargeEventList;

    @XStreamAlias(value="SAFETReimbursementEventList")
    private String sAFETReimbursementEventList;

    @XStreamAlias(value="StorageReservationBillingEventList")
    private String storageReservationBillingEventList;

    @XStreamAlias(value="RemovalShipmentAdjustmentEventList")
    private String removalShipmentAdjustmentEventList;

    @XStreamAlias(value="FBALiquidationEventList")
    private String fBALiquidationEventList;

    @XStreamAlias(value="AffordabilityExpenseEventList")
    private String affordabilityExpenseEventList;

    @XStreamAlias(value="AdjustmentEventList")
    private String adjustmentEventList;
}
