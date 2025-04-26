package com.cn.hzm.core.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@TableName("hzm_deal_file_detail")
public class DealFileDetailDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "file_id")
    private Integer fileId;

    private String asin;

    private String sku;

    private String fnsku;

    @TableField(value = "deal_num")
    private Integer dealNum;

    @TableField(value = "deal_status")
    private Integer dealStatus;

    @TableField(value = "deal_result")
    private String dealResult;

    private Date ctime;

    private Date utime;
}
