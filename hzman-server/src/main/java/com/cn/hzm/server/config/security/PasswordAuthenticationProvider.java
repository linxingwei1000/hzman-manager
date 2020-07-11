package com.cn.hzm.server.config.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by yuyang04 on 2020/7/11.
 */
public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        authentication.getPrincipal();
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return PasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }
}
