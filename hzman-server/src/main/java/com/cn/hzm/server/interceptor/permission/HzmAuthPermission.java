package com.cn.hzm.server.interceptor.permission;

import com.cn.hzm.server.meta.HzmRoleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuyang04 on 2021/1/17.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HzmAuthPermission {

    HzmRoleType[] needRole() default {};

    String[] needPermission() default {};
}
