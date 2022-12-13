package com.cn.hzm.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/9 6:50 下午
 */
@Data
@TableName("hzm_sale_info")
public class SaleInfoDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String sku;

    @TableField(value = "stat_date")
    private String statDate;

    @TableField(value = "sale_num")
    private Integer saleNum;

    @TableField(value = "order_num")
    private Integer orderNum;

    @TableField(value = "sale_volume")
    private Double saleVolume;

    @TableField(value = "sale_tax")
    private Double saleTax;

    @TableField(value = "fba_fulfillment_fee")
    private Double fbaFulfillmentFee;

    @TableField(value = "commission")
    private Double commission;

    @TableField(value = "unit_price")
    private Double unitPrice;

    private String config;

    private Date ctime;

    private Date utime;
}
