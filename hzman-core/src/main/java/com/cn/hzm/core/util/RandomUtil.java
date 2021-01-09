package com.cn.hzm.core.util;

import java.util.UUID;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class RandomUtil {

    public static String uuidWithoutSymbol() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
