package com.cn.hzm.core.common;

import com.cn.hzm.core.constant.ResponseCode;

import java.io.Serializable;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public class HzmResponse implements Serializable {
    private static final long serialVersionUID = 8389190280040336967L;

    private int code;

    private String msg;

    private Object result;

    public static HzmResponse success(Object result){
        return new HzmResponse(ResponseCode.OK, result);
    }

    public HzmResponse() {
    }

    public HzmResponse(ResponseCode code, Object result) {
        this(code.getCode(), code.getMessage(), result);
    }

    public HzmResponse(ResponseCode code) {
        this(code.getCode(), code.getMessage());
    }

    public HzmResponse(int code, String msg, Object result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public HzmResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public static HzmResponse generateOkResponse(Object result) {
        return new HzmResponse(ResponseCode.OK, result);
    }
}
