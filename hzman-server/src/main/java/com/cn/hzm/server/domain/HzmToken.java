package com.cn.hzm.server.domain;

import com.cn.hzm.core.util.RandomUtil;
import com.cn.hzm.core.util.TimeUtil;

import java.io.Serializable;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public class HzmToken implements Serializable {
    private static final long serialVersionUID = 4909884978241558720L;

    private Long accountId;

    private Long createTime;

    private String tokenId;

    public HzmToken() {
    }

    public HzmToken(Long accountId) {
        this.accountId = accountId;
        this.createTime = TimeUtil.nowMillis();
        this.tokenId = RandomUtil.uuidWithoutSymbol();
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
