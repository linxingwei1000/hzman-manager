package com.cn.hzm.core.cache.comparator;

import com.cn.hzm.api.dto.InventoryDto;
import com.cn.hzm.api.dto.ItemDto;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 6:12 下午
 */
public class SaleInventoryAscComparator implements Comparator<ItemDto> {

    @Override
    public int compare(ItemDto o1, ItemDto o2) {

        InventoryDto i1 = o1.getInventoryDTO();
        InventoryDto i2 = o2.getInventoryDTO();
        return SortHelper.compareEach(i1.getAmazonQuantity(), i2.getAmazonQuantity(), o2.getSku(), o1.getSku());
    }
}
