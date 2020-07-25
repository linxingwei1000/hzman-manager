package com.cn.hzm.server.config.security;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public class HzmGrantedAuthority implements GrantedAuthority {

    private String permissionType;

    private Set<String> permissionCodes;

    public HzmGrantedAuthority() {
    }

    public HzmGrantedAuthority(String permissionType, Set<String> permissionCodes) {
        this.setPermissionType(permissionType);
        this.setPermissionCodes(permissionCodes);
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Set<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(Set<String> permissionCodes) {
        this.permissionCodes = permissionCodes == null ? Sets.newHashSet() : permissionCodes;
    }

    public boolean removePermission(String permission) {
        return permissionCodes.remove(permission);
    }

    public boolean addPermission(String permission) {
        return StringUtils.isBlank(permission) ? false : permissionCodes.add(permission);
    }

    public boolean addPermissionCodes(Set<String> permissionCodes) {
        return permissionCodes == null ? false : this.permissionCodes.addAll(permissionCodes);
    }

    public boolean removePermission(Set<String> permissionCodes) {
        return permissionCodes == null ? false : this.permissionCodes.removeAll(permissionCodes);
    }

    public boolean hasPermission(String permission) {
        return hasPermission(permission, null);
    }

    public boolean hasPermission(String permission, String permissionType) {
        return permissionType == null
                ? permissionCodes.contains(permission)
                : permissionType.equals(this.permissionType) && permissionCodes.contains(permission);
    }

    @Override
    public String getAuthority() {
        return permissionType;
    }
}
