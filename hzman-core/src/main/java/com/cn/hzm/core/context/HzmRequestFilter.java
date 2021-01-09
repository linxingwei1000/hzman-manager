package com.cn.hzm.core.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by yuyang04 on 2021/1/9.
 */
@WebFilter(filterName = "hzmRequestFilter", urlPatterns = "/**")
public class HzmRequestFilter implements Filter {

    @Autowired
    private ContextHandler contextHandler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);

        if (HttpMethod.OPTIONS.matches(requestWrapper.getMethod())) {
            filterChain.doFilter(requestWrapper, servletResponse);
            return;
        }

        try {
            contextHandler.initContext(requestWrapper);
        } finally {
            contextHandler.destroyContext();
        }

    }

    @Override
    public void destroy() {

    }
}
