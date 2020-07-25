package com.cn.hzm.server.domain;

import com.cn.hzm.server.meta.PassportStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public class HzmUserDetails implements UserDetails {

    private final String username;

    private String password;

    private final Integer status;

    private final Set<GrantedAuthority> authorities;

    public HzmUserDetails(String username, String password, Integer status, Set<GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public HzmUserDetails(HzmPassport passport, Set<GrantedAuthority> authorities) {
        this.username = passport.getUsername();
        this.password = passport.getPassword();
        this.status = passport.getStatus();
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return PassportStatus.ENABLE.getValue().equals(status);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !PassportStatus.DISABLE.getValue().equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return PassportStatus.ENABLE.getValue().equals(status);
    }

    @Override
    public boolean isEnabled() {
        return PassportStatus.ENABLE.getValue().equals(status);
    }

    @Override
    public String toString() {
        return "HzmUserDetails{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", authorities=" + authorities +
                '}';
    }
}
