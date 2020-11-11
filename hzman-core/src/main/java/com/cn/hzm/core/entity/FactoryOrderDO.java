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
@TableName("hzm_factory_order")
public class FactoryOrderDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "factory_id")
    private Integer factoryId;

    @TableField(value = "item_id")
    private Integer itemId;

    @TableField(value = "order_num")
    private Integer orderNum;

    private String remark;

    @TableField(value = "item_price")
    private Double itemPrice;

    @TableField(value = "delivery_date")
    private String deliveryDate;

    @TableField(value = "waybill_num")
    private String waybillNum;

    @TableField(value = "receive_num")
    private Integer receiveNum;

    @TableField(value = "payment_voucher")
    private String paymentVoucher;


    @TableField(value = "order_status")
    private Integer orderStatus;

    private Date ctime;

    private Date utime;

}