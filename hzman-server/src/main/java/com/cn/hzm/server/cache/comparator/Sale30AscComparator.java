package com.cn.hzm.server.cache.comparator;

import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.dto.SaleInfoDTO;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/1 3:06 下午
 */
public class Sale30AscComparator implements Comparator<ItemDTO> {

    @Override
    public int compare(ItemDTO o1, ItemDTO o2) {

        SaleInfoDTO s1 = o1.getDuration30Day();
        SaleInfoDTO s2 = o2.getDuration30Day();
        return SortHelper.compareEach(s1.getSaleNum(), s2.getSaleNum(), o2.getSku(), o1.getSku());
    }
}
