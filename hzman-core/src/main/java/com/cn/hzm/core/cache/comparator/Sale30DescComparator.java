package com.cn.hzm.core.cache.comparator;

import com.cn.hzm.api.dto.ItemDto;
import com.cn.hzm.api.dto.SaleInfoDto;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 6:12 下午
 */
public class Sale30DescComparator implements Comparator<ItemDto> {

    @Override
    public int compare(ItemDto o1, ItemDto o2) {

        SaleInfoDto s1 = o1.getDuration30Day();
        SaleInfoDto s2 = o2.getDuration30Day();
        return SortHelper.compareEach(s2.getSaleNum(), s1.getSaleNum(), o1.getSku(), o2.getSku());
    }
}
