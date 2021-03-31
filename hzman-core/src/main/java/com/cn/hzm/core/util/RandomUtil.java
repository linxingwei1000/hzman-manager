package com.cn.hzm.core.util;

import java.util.UUID;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class RandomUtil {

    public static String uuidWithoutSymbol() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static Double saveDefaultDecimal(Double value) {
        return saveDecimal(value, 2);
    }

    public static Double saveDecimal(Double value, int saveDigitNum) {
        String s = String.format("%." + saveDigitNum + "f", value);
        return Double.valueOf(s);
    }
}
