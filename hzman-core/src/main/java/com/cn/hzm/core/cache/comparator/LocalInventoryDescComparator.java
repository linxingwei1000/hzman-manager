package com.cn.hzm.core.cache.comparator;

import com.cn.hzm.api.dto.InventoryDto;
import com.cn.hzm.api.dto.ItemDto;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/1 3:06 下午
 */
public class LocalInventoryDescComparator implements Comparator<ItemDto> {

    @Override
    public int compare(ItemDto o1, ItemDto o2) {

        InventoryDto i1 = o1.getInventoryDTO();
        InventoryDto i2 = o2.getInventoryDTO();
        return SortHelper.compareEach(i2.getLocalQuantity(), i1.getLocalQuantity(), o2.getSku(), o1.getSku());
    }
}
