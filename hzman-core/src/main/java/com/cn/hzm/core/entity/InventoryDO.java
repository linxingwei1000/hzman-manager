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
@TableName("hzm_inventory")
public class InventoryDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String sku;

    private String asin;

    private String fnsku;

    @TableField(value = "total_quantity")
    private Integer totalQuantity;

    @TableField(value = "amazon_quantity")
    private Integer amazonQuantity;

    @TableField(value = "amazon_stock_quantity")
    private Integer amazonStockQuantity;

    @TableField(value = "amazon_transfer_quantity")
    private Integer amazonTransferQuantity;

    @TableField(value = "aws_inbound_quantity")
    private Integer amazonInboundQuantity;

    @TableField(value = "local_quantity")
    private Integer localQuantity;

    @TableField(value = "item_condition")
    private String itemCondition;

    @TableField(value = "earliest_availability")
    private String earliestAvailability;

    @TableField(value = "supply_detail")
    private String supplyDetail;

    private Date ctime;

    private Date utime;

    public void calculateTotalQuantity(){
        this.totalQuantity = this.amazonQuantity + this.localQuantity;
    }

}
