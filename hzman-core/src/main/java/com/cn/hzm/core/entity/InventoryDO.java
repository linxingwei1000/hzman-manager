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

    @TableField(value = "item_id")
    private Integer itemId;

    @TableField(value = "total_quantity")
    private Integer totalQuantity;

    @TableField(value = "aws_quantity")
    private Integer awsQuantity;

    @TableField(value = "aws_stock_quantity")
    private Integer awsStockQuantity;

    @TableField(value = "local_quantity")
    private Integer localQuantity;

    private Date ctime;

    private Date utime;

    public void calculateTotalQuantity(){
        this.totalQuantity = this.awsQuantity + this.localQuantity;
    }

}
