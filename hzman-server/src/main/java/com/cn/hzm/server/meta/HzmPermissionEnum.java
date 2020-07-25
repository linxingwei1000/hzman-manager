package com.cn.hzm.server.meta;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public enum HzmPermissionEnum {

    LIST_USER("list_user", "user_manager", "查看用户列表"),
    EDIT_USER("edit_user", "user_manager", "编辑用户"),
    DEL_USER("del_user", "user_manager", "删除用户"),
    ;

    private String code;

    private String type;

    private String desc;

    HzmPermissionEnum(String code, String type, String desc) {
        this.code = code;
        this.type = type;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
