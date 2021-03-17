package com.cn.hzm.server.cache.comparator;

import com.cn.hzm.server.dto.InventoryDTO;
import com.cn.hzm.server.dto.ItemDTO;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/1 3:06 下午
 */
public class LocalInventoryAscComparator implements Comparator<ItemDTO> {

    @Override
    public int compare(ItemDTO o1, ItemDTO o2) {

        InventoryDTO i1 = o1.getInventoryDTO();
        InventoryDTO i2 = o2.getInventoryDTO();
        return SortHelper.compareEach(i1.getLocalQuantity(), i2.getLocalQuantity(), o2.getSku(), o1.getSku());
    }
}
