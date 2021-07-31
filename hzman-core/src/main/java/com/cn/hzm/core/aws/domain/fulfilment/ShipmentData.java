package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 31.7.21 11:13 上午
 */
@Data
@XStreamAlias("ShipmentData")
public class ShipmentData {

    @XStreamImplicit(itemFieldName="member")
    private List<ShipmentMember> list;
}
