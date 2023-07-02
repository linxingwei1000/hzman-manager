package com.cn.hzm.core.cache;

import com.cn.hzm.api.dto.ThreadLocalUserDto;
import lombok.Data;

/**
 * @author linxingwei
 * @date 29.6.23 4:56 下午
 */
@Data
public class ThreadLocalCache {

    private static ThreadLocal<ThreadLocalUserDto> tl = new ThreadLocal<>();

    public static void setUser(ThreadLocalUserDto user){
        tl.set(user);
    }

    public static ThreadLocalUserDto getUser(){
        return tl.get();
    }

    public static void clean(){
        tl.remove();
    }
}
