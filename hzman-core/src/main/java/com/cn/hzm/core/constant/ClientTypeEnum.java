package com.cn.hzm.core.constant;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public enum ClientTypeEnum {

    UNKNOWN(0, "unknown"),
    WEB(1, "web"),

    ;

    private Integer type;

    private String desc;

    ClientTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
