package com.cn.hzm.core.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public class HzmCollectionUtil {

    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean containsAny(Collection coll1, Collection coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        if (coll1.size() < coll2.size()) {
            for (Object o : coll1) {
                if (coll2.contains(o)) {
                    return true;
                }
            }
        } else {
            for (Object o : coll2) {
                if (coll1.contains(o)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAny(Collection coll1, String[] array) {
        if (isEmpty(coll1) || ArrayUtils.isEmpty(array)) {
            return false;
        }
        if (coll1.size() >= array.length || coll1 instanceof Set) {
            for (String it : array) {
                if (coll1.contains(it)) {
                    return true;
                }

            }
        } else {
            for (Object o : coll1) {
                if (ArrayUtils.contains(array, o)) {
                    return true;
                }
            }
        }
        return false;
    }
}
