package com.cn.hzm.core.aws.resp.inventory;

import com.cn.hzm.core.aws.domain.inventory.InventorySupplyList;
import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 5:02 下午
 */
@Data
@XStreamAlias("ListInventorySupplyResult")
public class ListInventorySupplyResult {

    @XStreamAlias(value = "MarketplaceId")
    private String marketplaceId;

    @XStreamAlias(value = "ResponseMetadata")
    private ResponseMetadata responseMetadata;

    @XStreamAlias(value = "InventorySupplyList")
    private InventorySupplyList inventorySupplyList;
}
