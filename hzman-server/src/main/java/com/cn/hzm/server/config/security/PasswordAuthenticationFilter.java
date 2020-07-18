package com.cn.hzm.server.config.security;

import com.cn.hzm.core.exception.HzmUnauthorizedException;
import com.cn.hzm.core.util.GsonUtil;
import com.cn.hzm.server.wrapper.HzmHttpServletRequestWrapper;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public class PasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
    private boolean postOnly = true;

    public PasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login", HttpMethod.POST.name()));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AuthenticationException, IOException, ServletException {
        if (postOnly && HttpMethod.POST.matches(httpServletRequest.getMethod())) {
            throw new HzmUnauthorizedException();
        }

        PasswordAuthenticationToken authRequest = resolveParameters(httpServletRequest);

        setDetails(httpServletRequest, authRequest);

        return authRequest;
    }

    protected PasswordAuthenticationToken resolveParameters(HttpServletRequest request) {
        String username;
        String password;

        String contentType = request.getContentType();

        if (StringUtils.containsIgnoreCase(contentType, ContentType.APPLICATION_JSON.toString())) {
            HzmHttpServletRequestWrapper requestWrapper = (HzmHttpServletRequestWrapper) request;
            String body = requestWrapper.getBodyString();
            JsonObject req = GsonUtil.parseJsonObject(body);
            username = GsonUtil.getString(req, usernameParameter);
            password = GsonUtil.getString(req, passwordParameter);
        } else {
            username = request.getParameter(usernameParameter);
            password = request.getParameter(passwordParameter);
        }

        return new PasswordAuthenticationToken(username == null ? username : username.trim(), password == null ? password : password.trim());
    }

    protected void setDetails(HttpServletRequest request, PasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }

        super.doFilter(new HzmHttpServletRequestWrapper(request), res, chain);
    }
}
