package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 3:44 下午
 */
@Data
@TableName("hzm_factory_item")
public class FactoryItemDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "factory_id")
    private Integer factoryId;

    private String sku;

    @TableField(value = "factory_price")
    private Double factoryPrice;

    private Date ctime;

    private Date utime;
}
