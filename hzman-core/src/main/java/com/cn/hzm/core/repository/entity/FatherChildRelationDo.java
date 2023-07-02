package com.cn.hzm.core.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linxingwei
 * @date 19.1.22 4:02 下午
 */
@Data
@TableName("hzm_father_child_relation")
public class FatherChildRelationDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_market_id")
    private Integer userMarketId;

    @TableField(value = "father_sku")
    private String fatherSku;

    @TableField(value = "father_asin")
    private String fatherAsin;

    @TableField(value = "child_sku")
    private String childSku;

    @TableField(value = "child_asin")
    private String childAsin;

    private Date ctime;

    private Date utime;
}
