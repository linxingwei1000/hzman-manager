package com.cn.hzm.core.aws.resp.inventory;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 5:01 下午
 */
@Data
@XStreamAlias("ListInventorySupplyResponse")
public class ListInventorySupplyResponse {

    @XStreamAlias(value = "ListInventorySupplyResult")
    ListInventorySupplyResult listInventorySupplyResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
