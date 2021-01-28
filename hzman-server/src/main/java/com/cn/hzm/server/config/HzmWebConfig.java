package com.cn.hzm.server.config;

import com.cn.hzm.core.context.ContextHandler;
import com.cn.hzm.core.context.HzmRequestFilter;
import com.cn.hzm.server.interceptor.HzmCommonInterceptor;
import com.cn.hzm.server.interceptor.permission.HzmAuthPermissionInterceptor;
import com.cn.hzm.server.interceptor.permission.HzmAuthTokenInterceptor;
import com.google.common.collect.Maps;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Created by yuyang04 on 2021/1/16.
 */
@Configuration
public class HzmWebConfig implements WebMvcConfigurer {

    @Resource
    private HzmCommonInterceptor hzmCommonInterceptor;

    @Resource
    private HzmAuthTokenInterceptor hzmAuthTokenInterceptor;

    @Resource
    private HzmAuthPermissionInterceptor hzmAuthPermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hzmCommonInterceptor).addPathPatterns("/**").excludePathPatterns("/health/**");
        registry.addInterceptor(hzmAuthTokenInterceptor).addPathPatterns("/user/**", "/factory/**", "/item/**");
        registry.addInterceptor(hzmAuthPermissionInterceptor).addPathPatterns("/user/**", "/factory/**", "/item/**");
    }

    @Order(0)
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    public FilterRegistrationBean<HzmRequestFilter> hzmRequestFilterFilterRegistrationBean() {
        FilterRegistrationBean<HzmRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HzmRequestFilter(contextHandler()));
        registrationBean.setInitParameters(Maps.newHashMap());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("hzmRequestFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public ContextHandler contextHandler() {
        return new ContextHandler();
    }
}
