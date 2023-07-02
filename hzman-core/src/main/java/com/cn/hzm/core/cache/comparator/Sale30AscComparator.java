package com.cn.hzm.core.cache.comparator;

import com.cn.hzm.api.dto.ItemDto;
import com.cn.hzm.api.dto.SaleInfoDto;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/1 3:06 下午
 */
public class Sale30AscComparator implements Comparator<ItemDto> {

    @Override
    public int compare(ItemDto o1, ItemDto o2) {

        SaleInfoDto s1 = o1.getDuration30Day();
        SaleInfoDto s2 = o2.getDuration30Day();
        return SortHelper.compareEach(s1.getSaleNum(), s2.getSaleNum(), o2.getSku(), o1.getSku());
    }
}
