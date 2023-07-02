package com.cn.hzm.core.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:06 下午
 */
@Data
@TableName("hzm_shipment_item_record")
public class FbaInboundItemDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value="quantity_shipped")
    private Integer quantityShipped;

    @TableField(value="shipment_id")
    private String shipmentId;

    @TableField(value="prep_details_list")
    private String prepDetailsList;

    @TableField(value="fulfillment_network_sku")
    private String fulfillmentNetworkSKU;

    @TableField(value="seller_sku")
    private String sellerSKU;

    @TableField(value="quantity_received")
    private Integer quantityReceived;

    @TableField(value="quantity_in_case")
    private Integer quantityInCase;

    private Date ctime;

    private Date utime;
}
