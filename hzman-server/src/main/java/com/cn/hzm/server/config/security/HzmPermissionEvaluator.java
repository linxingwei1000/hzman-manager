package com.cn.hzm.server.config.security;

import com.cn.hzm.server.service.impl.HzmUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public class HzmPermissionEvaluator implements PermissionEvaluator {

    private static Logger logger = LoggerFactory.getLogger(HzmPermissionEvaluator.class);

    private final ApplicationContext context;

    private HzmUserDetailsService userDetailsService;

    public HzmPermissionEvaluator(ApplicationContext context) {
        this.context = context;
        this.userDetailsService = this.context.getBean(HzmUserDetailsService.class);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomain, Object obj) {
        try {
            String type = (String) targetDomain;
            String permission = (String) obj;
            return Optional.ofNullable(authentication)
                    .filter(a -> a.isAuthenticated())
                    .map(a -> userDetailsService.loadUserByUsername((String) a.getPrincipal()))
                    .map(u -> u.getAuthorities()).map(l -> l.stream().anyMatch(g -> ((HzmGrantedAuthority) g).hasPermission(permission, type)))
                    .orElse(false);
        } catch (Exception e) {
            logger.error("no permissions! userId : {}, permission : {}.", authentication.getPrincipal(), obj, e);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }
}
