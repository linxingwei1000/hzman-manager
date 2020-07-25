package com.cn.hzm.core.exception;

/**
 * Created by yuyang04 on 2020/7/11.
 */
public class HzmBasicRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -3459497515674209141L;
    private final int code;

    public HzmBasicRuntimeException(int code) {
        this.code = code;
    }

    public HzmBasicRuntimeException(String message, int code) {
        super(message);
        this.code = code;
    }

    public HzmBasicRuntimeException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public HzmBasicRuntimeException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
