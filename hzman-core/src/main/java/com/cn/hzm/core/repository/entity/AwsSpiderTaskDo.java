package com.cn.hzm.core.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linxingwei
 * @date 24.3.23 4:19 下午
 */
@Data
@TableName("hzm_aws_spider_task")
public class AwsSpiderTaskDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_market_id")
    private Integer userMarketId;

    @TableField(value = "spider_type")
    private Integer spiderType;

    @TableField(value = "spider_depend")
    private String spiderDepend;

    @TableField(value = "is_active")
    private Integer isActive;

    private Date ctime;

    private Date utime;

}
