package com.cn.hzm.core.common;

import java.io.Serializable;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public class BaseDomain implements Serializable {
    private static final long serialVersionUID = 5233652550204859280L;

    private Long createTime;

    private Long updateTime;

    private Long lastOptUserId;

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getLastOptUserId() {
        return lastOptUserId;
    }

    public void setLastOptUserId(Long lastOptUserId) {
        this.lastOptUserId = lastOptUserId;
    }
}
