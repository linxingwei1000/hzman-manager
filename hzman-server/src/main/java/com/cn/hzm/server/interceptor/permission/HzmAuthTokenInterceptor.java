package com.cn.hzm.server.interceptor.permission;

import com.cn.hzm.core.constant.ResponseCode;
import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.exception.HzmBasicRuntimeException;
import com.cn.hzm.core.exception.HzmUnauthorizedException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmToken;
import com.cn.hzm.server.meta.HzmPassportStatus;
import com.cn.hzm.server.service.PassportService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Created by yuyang04 on 2021/1/16.
 */
@Component
public class HzmAuthTokenInterceptor implements HandlerInterceptor {

    @Value("${auth.token.effectiveMills:604800000}")
    private Long authTokenEffectiveMills;

    @Resource
    private PassportService passportService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (!handlerMethod.hasMethodAnnotation(HzmAuthToken.class)
                && !AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), HzmAuthToken.class)) {
            return true;
        }

        String encryptToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        HzmContext.get().setAccountToken(encryptToken);
        HzmToken token = Optional.ofNullable(encryptToken)
                .map(t -> passportService.parseToken(t))
                .orElseThrow(() -> new HzmBasicRuntimeException(ResponseCode.TOKEN_INVALID));

        if (token.getAccountId() == null || token.getCreateTime() == null) {
            throw new HzmUnauthorizedException();
        }

        if (TimeUtil.nowMillis() >= token.getCreateTime() + authTokenEffectiveMills) {
            throw new HzmBasicRuntimeException(ResponseCode.TOKEN_EXPIRED);
        }

        HzmPassport passport = passportService.findPassportById(token.getAccountId());
        if (!StringUtils.equals(passport.getToken(), encryptToken)) {
            throw new HzmBasicRuntimeException(ResponseCode.TOKEN_EXPIRED);
        }

        HzmContext.get().setAccountId(passport.getId());
        if (!HzmPassportStatus.isActive(passport.getStatus())) {
            throw new HzmBasicRuntimeException(ResponseCode.FORBIDDEN);
        }

        return true;
    }
}
