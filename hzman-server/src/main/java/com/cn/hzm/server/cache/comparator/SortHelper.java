package com.cn.hzm.server.cache.comparator;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/18 10:24 上午
 */
public class SortHelper {

    public static int compareEach(Integer saleNum1, Integer saleNum2, String sku1, String sku2) {
        if (saleNum1 == null) {
            saleNum1 = 0;
        }
        if (saleNum2 == null) {
            saleNum2 = 0;
        }

        int result = saleNum1 - saleNum2;
        if (result == 0) {
            result = sku1.compareTo(sku2);
        }
        return result;
    }
}
