package com.cn.hzm.server.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public class HzmPermissionEvaluator implements PermissionEvaluator {

    private static Logger logger = LoggerFactory.getLogger(HzmPermissionEvaluator.class);

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomain, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }
}
