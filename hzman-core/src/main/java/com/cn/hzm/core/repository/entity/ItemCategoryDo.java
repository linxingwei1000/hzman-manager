package com.cn.hzm.core.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linxingwei
 * @date 19.1.22 4:02 下午
 */
@Data
@TableName("hzm_item_category_rank")
public class ItemCategoryDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "item_id")
    private Integer itemId;

    @TableField(value = "relation_info")
    private String relationInfo;

    @TableField(value = "category_title")
    private String categoryTitle;

    @TableField(value = "category_link")
    private String categoryLink;

    @TableField(value = "category_rank")
    private Integer categoryRank;

    private Date ctime;

    private Date utime;
}
