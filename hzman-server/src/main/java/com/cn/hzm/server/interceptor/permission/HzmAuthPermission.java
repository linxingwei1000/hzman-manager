package com.cn.hzm.server.interceptor.permission;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public @interface HzmAuthPermission {

    String[] needRole() default {};

    String[] needPermission() default {};
}
