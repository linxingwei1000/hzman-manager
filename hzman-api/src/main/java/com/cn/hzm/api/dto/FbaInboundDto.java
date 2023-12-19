package com.cn.hzm.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:06 下午
 */
@Data
public class FbaInboundDto {

    private String shipmentId;

    private String shipmentName;

    private String shipmentStatus;

    private List<FbaInboundItemDto> fbaInboundItemDtos;

}
