package com.cn.hzm.core.context;

import org.springframework.http.HttpMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class HzmRequestFilter implements Filter {

    private final ContextHandler contextHandler;

    public HzmRequestFilter(ContextHandler contextHandler) {
        this.contextHandler = contextHandler;
    }

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
            filterChain.doFilter(requestWrapper, servletResponse);
        } catch (Exception e) {
            HzmContext.get().exception(e);
            filterChain.doFilter(requestWrapper, servletResponse);
        } finally {
            HzmContext.get().printLog();
            contextHandler.destroyContext();
        }

    }

    @Override
    public void destroy() {

    }
}
