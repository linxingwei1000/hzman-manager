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
@TableName("hzm_item")
public class ItemDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String asin;

    private String sku;

    private String title;

    private String icon;

    @TableField(value = "marketplace_id")
    private String marketplaceId;

    @TableField(value = "attribute_set")
    private String attributeSet;

    private String relationship;

    private Date ctime;

    private Date utime;
}
