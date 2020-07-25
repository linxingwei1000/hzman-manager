package com.cn.hzm.server.service.impl;

import com.cn.hzm.core.exception.HzmUnauthorizedException;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmUserDetails;
import com.cn.hzm.server.service.PassportService;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@Service
public class HzmUserDetailsService implements UserDetailsService {

    @Autowired
    PassportService passportService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HzmPassport passport = passportService.findPassportByUsername(username);
        if (passport == null) {
            new HzmUnauthorizedException();
        }
        // TODO: 2020/7/25 load role and permissions
        Set<GrantedAuthority> authorities = Sets.newHashSet(new SimpleGrantedAuthority("role_admin"));

        return new HzmUserDetails(passport, authorities);
    }
}
