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
@TableName("hzm_shipment_info_record")
public class FbaInboundDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String fcid;

    @TableField(value="lp_type")
    private String lpType;

    @TableField(value="ship_address")
    private String shipAddress;

    @TableField(value="shipment_id")
    private String shipmentId;

    @TableField(value="shipment_name")
    private String shipmentName;

    @TableField(value="box_contents_source")
    private String boxContentsSource;

    @TableField(value="shipment_status")
    private String shipmentStatus;

    private Date ctime;

    private Date utime;
}
