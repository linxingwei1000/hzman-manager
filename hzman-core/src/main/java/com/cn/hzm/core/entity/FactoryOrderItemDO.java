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
@TableName("hzm_factory_order_item")
public class FactoryOrderItemDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "factory_order_id")
    private Integer factoryOrderId;

    private String sku;

    @TableField(value = "order_num")
    private Integer orderNum;

    private String remark;

    @TableField(value = "item_price")
    private Double itemPrice;

    private Date ctime;

    private Date utime;

}
