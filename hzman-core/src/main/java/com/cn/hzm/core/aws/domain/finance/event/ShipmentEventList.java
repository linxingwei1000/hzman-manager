package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 6.11.22 4:37 下午
 */
@Data
@XStreamAlias("ShipmentEventList")
public class ShipmentEventList {

    @XStreamImplicit(itemFieldName="ShipmentEvent")
    private List<ShipmentEvent> list;
}
