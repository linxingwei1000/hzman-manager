package com.cn.hzm.server.meta;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/27 1:58 下午
 */
public enum HzmRoleType {

    /**
     * default
     */
    ROLE_DEFAULT("default", "未指定用户角色"),

    /**
     * 管理员角色
     */
    ROLE_ADMIN("admin", "管理员"),

    /**
     * 普通员工角色
     */
    ROLE_EMPLOYEE("employee", "普通员工"),

    /**
     * 新员工角色
     */
    ROLE_NEW_EMPLOYEE("new_employee", "新员工"),

    /**
     * 厂家用户角色
     */
    ROLE_FACTORY("factoryUser", "厂家用户"),
    ;

    private String roleId;

    private String desc;

    HzmRoleType(String roleId, String desc) {
        this.roleId = roleId;
        this.desc = desc;
    }

    public static HzmRoleType getHzmRoleTypeByRoleId(String roleId){
        for(HzmRoleType roleType: HzmRoleType.values()){
            if(roleType.getRoleId().equals(roleId)){
                return roleType;
            }
        }
        return ROLE_DEFAULT;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getDesc() {
        return desc;
    }
}
