package com.cn.hzm.server.dto;

import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/27 2:39 下午
 */
@Data
public class UserConditionDTO extends PageDTO {

    String username;

    Integer companyId;
}
