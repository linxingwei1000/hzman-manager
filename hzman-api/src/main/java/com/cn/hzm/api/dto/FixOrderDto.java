package com.cn.hzm.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 5.12.22 10:44 上午
 */
@Data
public class FixOrderDto {

    private Integer userMarketId;

    private List<String> orderIds;
}
