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
@TableName("hzm_aws_user_market")
public class AwsUserMarketDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "aws_user_id")
    private Integer awsUserId;

    @TableField(value = "market_id")
    private String marketId;

    private Date ctime;

    private Date utime;

}
