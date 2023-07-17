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
@TableName("hzm_aws_user")
public class AwsUserDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String remark;

    @TableField(value = "access_key_id")
    private String accessKeyId;

    @TableField(value = "secret_key")
    private String secretKey;

    @TableField(value = "role_arn")
    private String roleArn;

    @TableField(value = "client_id")
    private String clientId;

    @TableField(value = "client_secret")
    private String clientSecret;

    @TableField(value = "refresh_token")
    private String refreshToken;

    private Date ctime;

    private Date utime;

}