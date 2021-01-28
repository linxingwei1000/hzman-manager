package com.cn.hzm.server.domain;

import com.cn.hzm.core.common.BaseDomain;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public class HzmPassport extends BaseDomain {
    private static final long serialVersionUID = -1780332453797359711L;

    private Long id;

    private String username;

    private String password;

    private Integer companyId;

    private String token;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
