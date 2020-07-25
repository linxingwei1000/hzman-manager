package com.cn.hzm.core.exception;

import com.cn.hzm.core.constant.ResponseCode;

/**
 * Created by yuyang04 on 2020/7/16.
 */
public class HzmUnauthorizedException extends HzmBasicRuntimeException {

    public HzmUnauthorizedException() {
        super(ResponseCode.UNAUTHORIZED.getMessage(), ResponseCode.UNAUTHORIZED.getCode());
    }

    public HzmUnauthorizedException(String message) {
        super(message, ResponseCode.UNAUTHORIZED.getCode());
    }

    public HzmUnauthorizedException(String message, Throwable cause) {
        super(message, cause, ResponseCode.UNAUTHORIZED.getCode());
    }

    public HzmUnauthorizedException(Throwable cause) {
        super(cause, ResponseCode.UNAUTHORIZED.getCode());
    }
}
