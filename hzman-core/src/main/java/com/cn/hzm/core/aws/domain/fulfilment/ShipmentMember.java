package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 31.7.21 11:15 上午
 */
@Data
@XStreamAlias("member")
public class ShipmentMember {

    @XStreamAlias(value="DestinationFulfillmentCenterId")
    private String destinationFulfillmentCenterId;

    @XStreamAlias(value="LabelPrepType")
    private String labelPrepType;

    @XStreamAlias(value="ShipFromAddress")
    private ShipFromAddress shipFromAddress;

    @XStreamAlias(value="ShipmentId")
    private String shipmentId;

    @XStreamAlias(value="AreCasesRequired")
    private String areCasesRequired;

    @XStreamAlias(value="ShipmentName")
    private String shipmentName;

    @XStreamAlias(value="BoxContentsSource")
    private String boxContentsSource;

    @XStreamAlias(value="ShipmentStatus")
    private String shipmentStatus;
}
