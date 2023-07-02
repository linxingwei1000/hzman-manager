package com.cn.hzm.core.repository.entity;

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
public class FactoryOrderDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "factory_id")
    private Integer factoryId;

    @TableField(value = "delivery_date")
    private String deliveryDate;

    @TableField(value = "waybill_num")
    private String waybillNum;

    @TableField(value = "payment_voucher")
    private String paymentVoucher;

    @TableField(value = "total_num")
    private Integer totalNum;

    @TableField(value = "total_price")
    private Double totalPrice;

    @TableField(value = "order_desc")
    private String orderDesc;

    @TableField(value = "receive_address")
    private String receiveAddress;

    @TableField(value = "order_status")
    private Integer orderStatus;

    private Date ctime;

    private Date utime;

}
