package com.cn.hzm.server.interceptor.permission;

import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.util.HzmCollectionUtil;
import com.cn.hzm.server.domain.HzmUserRole;
import com.cn.hzm.server.service.HzmUserRoleService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by yuyang04 on 2021/1/17.
 */
@Component
public class HzmAuthPermissionInterceptor implements HandlerInterceptor {

    @Resource
    private HzmUserRoleService hzmUserRoleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        HzmAuthPermission permission = handlerMethod.getMethodAnnotation(HzmAuthPermission.class);
        if (permission == null) {
            permission = AnnotationUtils.getAnnotation(handlerMethod.getBeanType(), HzmAuthPermission.class);
        }

        if (permission == null || (permission.needPermission().length == 0 && permission.needRole().length == 0)) {
            return true;
        }

        String[] needRole = permission.needRole();
        if (needRole.length > 0) {
            // load roles
            List<HzmUserRole> userRoleList = hzmUserRoleService.findValidRoleByPassportId(HzmContext.get().getAccountId());
            Set<String> roles = HzmCollectionUtil.isEmpty(userRoleList) ? Sets.newHashSet() : userRoleList.stream().map(r -> r.getRoleId()).collect(Collectors.toSet());

            HzmContext.get().setRoles(roles);
            if (!HzmCollectionUtil.containsAny(roles, needRole)) {
                return false;
            }
        }

        // TODO: 2021/1/17 check permissions
        return true;
    }
}
