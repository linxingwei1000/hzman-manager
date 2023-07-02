package com.cn.hzm.api.dto;

import lombok.Data;

/**
 * @author linxingwei
 * @date 29.6.23 5:00 下午
 */
@Data
public class ThreadLocalUserDto {

    private Integer awsUserId;

    private String marketId;

    private Integer userMarketId;
}
