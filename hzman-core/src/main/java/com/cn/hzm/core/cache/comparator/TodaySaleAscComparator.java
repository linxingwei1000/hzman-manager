package com.cn.hzm.core.cache.comparator;

import com.cn.hzm.api.dto.ItemDto;
import com.cn.hzm.api.dto.SaleInfoDto;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 6:12 下午
 */
public class TodaySaleAscComparator implements Comparator<ItemDto> {

    @Override
    public int compare(ItemDto o1, ItemDto o2) {

        SaleInfoDto s1 = o1.getToday();
        SaleInfoDto s2 = o2.getToday();
        return SortHelper.compareEach(s1.getSaleNum(), s2.getSaleNum(), o2.getSku(), o1.getSku());
    }
}
