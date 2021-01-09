package com.cn.hzm.core.context;

import com.cn.hzm.core.constant.ClientTypeEnum;
import com.cn.hzm.core.util.TimeUtil;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class HzmContext {

    private static final ThreadLocal<HzmContext> THREAD_LOCAL = new InheritableThreadLocal<>();

    private ClientTypeEnum clientType;

    private Long accountId;

    private String accountToken;

    private long startTime = TimeUtil.nowMillis();

    private String body;

    private String param;

    private String traceId;

    private String peer;

    public static HzmContext get() {
        return THREAD_LOCAL.get();
    }

    public static HzmContext init(String traceId) {
        HzmContext context = new HzmContext();
        context.setTraceId(traceId);
        THREAD_LOCAL.set(context);
        return context;
    }

    public void unload() {
        THREAD_LOCAL.remove();
    }

    public ClientTypeEnum getClientType() {
        return clientType;
    }

    public void setClientType(ClientTypeEnum clientType) {
        this.clientType = clientType;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountToken() {
        return accountToken;
    }

    public void setAccountToken(String accountToken) {
        this.accountToken = accountToken;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }
}
