package com.cn.hzm.core.context;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class LogContext {

    private static final ThreadLocal<LogContext> THREAD_LOCAL = new InheritableThreadLocal<>();

    private Map<String, Object> properties = Maps.newLinkedHashMap();

    public static LogContext context() {
        LogContext context = THREAD_LOCAL.get();
        if (context == null) {
            context = new LogContext();
            THREAD_LOCAL.set(context);
        }
        return context;
    }

    public static void putToContext(String key, Object value) {
        context().put(key, value);
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void clear() {
        properties.clear();
    }
}
