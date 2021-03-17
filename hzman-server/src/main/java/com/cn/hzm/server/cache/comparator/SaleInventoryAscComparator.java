package com.cn.hzm.server.cache.comparator;

import com.cn.hzm.server.dto.InventoryDTO;
import com.cn.hzm.server.dto.ItemDTO;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 6:12 下午
 */
public class SaleInventoryAscComparator implements Comparator<ItemDTO> {

    @Override
    public int compare(ItemDTO o1, ItemDTO o2) {

        InventoryDTO i1 = o1.getInventoryDTO();
        InventoryDTO i2 = o2.getInventoryDTO();
        return SortHelper.compareEach(i1.getAmazonQuantity(), i2.getAmazonQuantity(), o2.getSku(), o1.getSku());
    }
}
