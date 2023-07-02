package com.cn.hzm.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linxingwei
 * @date 14.5.23 6:07 下午
 */
@Data
public class AddItemRemarkDto implements Serializable {

    private Integer id;

    private Integer itemId;

    private String remark;
}
