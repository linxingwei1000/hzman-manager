package com.cn.hzm.core.repository.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@TableName("hzm_asin_item")
public class AsinItemDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String asin;

    private String title;

    private String icon;

    @TableField(value = "package_dimension")
    private String packageDimension;

    @TableField(value = "item_type")
    private String itemType;

    @TableField(value = "local_quantity")
    private Integer localQuantity;

    private Integer active;

    private Date ctime;

    private Date utime;

}
