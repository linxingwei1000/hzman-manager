package com.cn.hzm.api.dto;

import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/27 2:39 下午
 */
@Data
public class UserConditionDto extends PageDto {

    String username;

    Integer companyId;
}
