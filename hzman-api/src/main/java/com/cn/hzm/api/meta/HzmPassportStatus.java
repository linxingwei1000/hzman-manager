package com.cn.hzm.api.meta;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public enum HzmPassportStatus {

    ENABLE(1, "启用"),

    DISABLE(2, "禁用"),

    INACTIVE(3, "未激活"),

    ;

    private Integer value;

    private String desc;

    HzmPassportStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    private static final Map<Integer, HzmPassportStatus> LOOK_UP;
    static {
        LOOK_UP = Stream.of(values()).collect(Collectors.toMap(s -> s.value, s -> s, (s1, s2) -> s1));
    }

    public static HzmPassportStatus findByValue(Integer value) {
        return LOOK_UP.get(value);
    }

    public static boolean isActive(Integer status) {
        return ENABLE.getValue().equals(status);
    }
}
