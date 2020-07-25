package com.cn.hzm.server.util;

import com.cn.hzm.core.aws.domain.inventory.Member;
import com.cn.hzm.core.aws.resp.inventory.ListInventorySupplyResponse;
import com.cn.hzm.core.entity.InventoryDO;

import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:00 下午
 */
public class ConvertUtil {

    public static Member convertToInventoryDO(ListInventorySupplyResponse inventory, InventoryDO inventoryDO){
        inventoryDO.setLocalQuantity(0);
        Member member = inventory.getListInventorySupplyResult().getInventorySupplyList().getMembers().get(0);
        inventoryDO.setAwsQuantity(member.getTotalSupplyQuantity());
        inventoryDO.setAwsStockQuantity(member.getInStockSupplyQuantity());
        return member;
    }
}
