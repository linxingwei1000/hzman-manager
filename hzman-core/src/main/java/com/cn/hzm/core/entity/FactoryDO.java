package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:45 下午
 */
@Data
@TableName("hzm_factory")
public class FactoryDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "factory_name")
    private String factoryName;

    private String address;

    @TableField(value = "contact_person")
    private String contactPerson;


    @TableField(value = "contact_info")
    private String contactInfo;

    private Date ctime;

    private Date utime;

}
