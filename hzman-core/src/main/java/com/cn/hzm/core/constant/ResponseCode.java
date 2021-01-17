package com.cn.hzm.core.constant;

/**
 * Created by yuyang04 on 2020/7/16.
 */
public enum ResponseCode {

    OK(200, "ok"),
    CONTINUE(201, "continue"),

    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    TOKEN_INVALID(420, "invalid token"),
    TOKEN_EXPIRED(421, "token expired"),

    SERVER_ERROR(500, "error"),
    ;

    private int code;

    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
