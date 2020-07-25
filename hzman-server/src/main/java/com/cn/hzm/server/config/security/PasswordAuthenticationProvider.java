package com.cn.hzm.server.config.security;

import com.cn.hzm.core.exception.HzmUnauthorizedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * Created by yuyang04 on 2020/7/11.
 */
public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private UserDetailsService userDetailsService;

    protected void additionalAuthenticationChecks(UserDetails userDetails, PasswordAuthenticationToken authentication) {
        if (authentication.getCredentials() == null) {
            throw new HzmUnauthorizedException();
        }

        String password = (String) authentication.getCredentials();
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new HzmUnauthorizedException();
        }
    }

    protected UserDetails retrieveUser(String username, PasswordAuthenticationToken authentication) {
        UserDetails loadedUser = userDetailsService.loadUserByUsername(username);

        if (loadedUser == null) {
            throw new HzmUnauthorizedException();
        }

        return loadedUser;
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        PasswordAuthenticationToken result =
                new PasswordAuthenticationToken((String) principal, (String) authentication.getCredentials(), user.getAuthorities());
        result.setDetails(user);
        return result;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(PasswordAuthenticationToken.class, authentication, "Only PasswordAuthenticationToken supported!");
        PasswordAuthenticationToken passwordToken = (PasswordAuthenticationToken) authentication;
        String username = passwordToken.getPrincipal() == null ? "anonymous" : passwordToken.getName();
        // TODO: 2020/7/18 load from cache
        UserDetails user = null;
        // load from db
        if (user == null) {
            user = retrieveUser(username, passwordToken);
        }

        additionalAuthenticationChecks(user, passwordToken);

        return createSuccessAuthentication(authentication.getPrincipal(), authentication, user);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return PasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
