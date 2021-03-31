package com.cn.hzm.server.cache.comparator;

import com.cn.hzm.server.dto.ItemDTO;
import com.cn.hzm.server.dto.SaleInfoDTO;

import java.util.Comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 6:12 下午
 */
public class Sale30DescComparator implements Comparator<ItemDTO> {

    @Override
    public int compare(ItemDTO o1, ItemDTO o2) {

        SaleInfoDTO s1 = o1.getDuration30Day();
        SaleInfoDTO s2 = o2.getDuration30Day();
        return SortHelper.compareEach(s2.getSaleNum(), s1.getSaleNum(), o1.getSku(), o2.getSku());
    }
}
