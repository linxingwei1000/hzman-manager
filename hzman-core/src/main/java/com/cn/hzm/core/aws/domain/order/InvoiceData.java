package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/19 5:24 下午
 */
@Data
@XStreamAlias("InvoiceData")
public class InvoiceData {

    @XStreamAlias("InvoiceRequirement")
    String invoiceRequirement;

    @XStreamAlias("BuyerSelectedInvoiceCategory")
    String buyerSelectedInvoiceCategory;

    @XStreamAlias("InvoiceTitle")
    String invoiceTitle;

    @XStreamAlias("InvoiceInformation")
    String invoiceInformation;
}
