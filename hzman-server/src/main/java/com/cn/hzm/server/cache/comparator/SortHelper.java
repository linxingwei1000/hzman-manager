package com.cn.hzm.server.cache.comparator;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.server.dto.ItemDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/18 10:24 上午
 */
public class SortHelper {


    /**
     * 根据sortType 返回指定排序list
     *
     * @param sortType
     * @return
     */
    public static List<ItemDTO> sortItem(Integer sortType, List<ItemDTO> needSortList) {

        Comparator<ItemDTO> comparator;
        switch (sortType) {
            case ContextConst.ITEM_SORT_TODAY_DESC:
                comparator = new TodaySaleDescComparator();
                break;
            case ContextConst.ITEM_SORT_TODAY_ASC:
                comparator = new TodaySaleAscComparator();
                break;
            case ContextConst.ITEM_SORT_YESTERDAY_DESC:
                comparator = new YesterdaySaleDescComparator();
                break;
            case ContextConst.ITEM_SORT_YESTERDAY_ASC:
                comparator = new YesterdaySaleAscComparator();
                break;
            case ContextConst.ITEM_SORT_LAST_WEEK_DESC:
                comparator = new LastWeekSaleDescComparator();
                break;
            case ContextConst.ITEM_SORT_LAST_WEEK_ASC:
                comparator = new LastWeekSaleAscComparator();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + sortType);
        }

        TreeSet<ItemDTO> set = Sets.newTreeSet(comparator);
        set.addAll(needSortList);
        return Lists.newArrayList(set);
    }

    public static int compareEach(int saleNum1, int saleNum2, String sku1, String sku2){
        int result = saleNum1 - saleNum2;
        if(result==0){
            result = sku1.compareTo(sku2);
        }
        return result;
    }
}
