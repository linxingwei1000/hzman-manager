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
@TableName("hzm_item_remark")
public class ItemRemarkDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "item_id")
    private Integer itemId;

    private String remark;

    private Date ctime;

    private Date utime;

}
