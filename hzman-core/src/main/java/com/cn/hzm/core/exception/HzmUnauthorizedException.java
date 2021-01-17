package com.cn.hzm.core.exception;

import com.cn.hzm.core.constant.ResponseCode;

/**
 * Created by yuyang04 on 2020/7/16.
 */
public class HzmUnauthorizedException extends HzmBasicRuntimeException {

    private static final long serialVersionUID = -2565043341775799321L;

    public HzmUnauthorizedException() {
        super(ResponseCode.UNAUTHORIZED);
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
