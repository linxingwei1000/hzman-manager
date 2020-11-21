package com.cn.hzm.server.service.impl;

import com.cn.hzm.core.exception.HzmUnauthorizedException;
import com.cn.hzm.server.config.security.HzmGrantedAuthority;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmUserDetails;
import com.cn.hzm.server.meta.HzmPermission;
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

        //todo 暂时修改一下
//        HzmPassport passport = passportService.findPassportByUsername(username);
//        if (passport == null) {
//            new HzmUnauthorizedException();
//        }
        HzmPassport passport = new HzmPassport();
        passport.setUsername("root");
        passport.setPassword("$2a$10$4EdQAXlng87sn1zSirHmwuH69vnW5PpXA.rN417jExXwmdPTPl.FG");
        passport.setStatus(1);
        // TODO: 2020/7/25 load role and permissions
        Set<GrantedAuthority> authorities = Sets.newHashSet();
        authorities.add(new HzmGrantedAuthority(HzmPermission.SUPER_ADMIN, Sets.newHashSet()));
        authorities.add(new HzmGrantedAuthority(HzmPermission.PermissionType.USER_MANAGER, Sets.newHashSet(HzmPermission.UserManager.DEL_USER)));

        return new HzmUserDetails(passport, authorities);
    }
}
