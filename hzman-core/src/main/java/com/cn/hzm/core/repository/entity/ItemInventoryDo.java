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
@TableName("hzm_item_inventory")
public class ItemInventoryDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_market_id")
    private Integer userMarketId;

    private String sku;

    private String asin;

    private String fnsku;

    @TableField(value = "item_condition")
    private String itemCondition;

    @TableField(value = "total_quantity")
    private Integer totalQuantity;

    @TableField(value = "amazon_quantity")
    private Integer amazonQuantity;

    @TableField(value = "fulfillable_quantity")
    private Integer fulfillableQuantity;

    @TableField(value = "inbound_working_quantity")
    private Integer inboundWorkingQuantity;

    @TableField(value = "inbound_shipped_quantity")
    private Integer inboundShippedQuantity;

    @TableField(value = "inbound_receiving_quantity")
    private Integer inboundReceivingQuantity;

    @TableField(value = "reserved_quantity")
    private String reservedQuantity;

    @TableField(value = "researching_quantity")
    private String researchingQuantity;

    @TableField(value = "unfulfillable_quantity")
    private String unfulfillableQuantity;

    @TableField(value = "last_updated_time")
    private String lastUpdatedTime;

    @TableField(value = "local_quantity")
    private Integer localQuantity;

    private Date ctime;

    private Date utime;

    public void calculateTotalQuantity(){
        this.totalQuantity = this.amazonQuantity + this.localQuantity;
    }

}
