package com.cn.hzm.server.domain;

import com.cn.hzm.core.common.BaseDomain;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public class HzmUserRole extends BaseDomain {
    private static final long serialVersionUID = 1785469500313032905L;

    private Long id;

    private Long passportId;

    private String roleId;

    private Integer valid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPassportId() {
        return passportId;
    }

    public void setPassportId(Long passportId) {
        this.passportId = passportId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }
}
