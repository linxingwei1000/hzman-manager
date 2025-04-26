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
@TableName("hzm_deal_file")
public class DealFileDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "file_type")
    private Integer fileType;

    @TableField(value = "deal_status")
    private Integer dealStatus;

    private Date ctime;

    private Date utime;

}
