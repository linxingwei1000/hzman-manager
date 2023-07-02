package com.cn.hzm.api.meta;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public enum HzmUserRoleValidType {

    EFFECTIVE(1, "有效"),
    DELETED(2, "已删除")
    ;

    private Integer type;

    private String desc;

    HzmUserRoleValidType(Integer type, String desc) {
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
