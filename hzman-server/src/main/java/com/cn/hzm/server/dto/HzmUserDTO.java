package com.cn.hzm.server.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@Data
public class HzmUserDTO{

    private Long id;

    private String username;

    private String password;

    private Integer companyId;

    private String companyName;

    private Boolean isMyself = false;

    List<RoleInfoDTO> roleInfos;
}
