package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@TableName("hzm_operate_depend")
public class OperateDependDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "operate_key")
    private String operateKey;

    @TableField(value = "operate_value")
    private String operateValue;
}
