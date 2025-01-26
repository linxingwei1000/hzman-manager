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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 3:44 下午
 */
@Data
@TableName("hzm_item")
public class ItemDo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_market_id")
    private Integer userMarketId;

    private String asin;

    private String sku;

    private String title;

    private String icon;

    @TableField(value = "item_price")
    private Double itemPrice;

    @TableField(value = "item_cost")
    private Double itemCost;

    @TableField(value= "listing_time")
    private String listingTime;

    @TableField(value = "marketplace_id")
    private String marketplaceId;

    @TableField(value = "package_dimension")
    private String packageDimension;

    @TableField(value = "sale_rank")
    private String saleRank;

    @TableField(value = "item_type")
    private String itemType;

    @TableField(value = "attribute_set")
    private String attributeSet;

    private String relationship;

    @TableField(value = "item_remark")
    private String itemRemark;

    @TableField(value = "is_parent")
    private Integer isParent;

    @TableField(value = "activity_info")
    private String activityInfo;

    private Integer active;

    private Date ctime;

    private Date utime;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemActivityObj implements Serializable {
        private static final long serialVersionUID = -4024831915728857514L;
        /**
         * 是否透明计划
         */
        private Boolean isTransparencyPlan = false;
    }

    public ItemActivityObj getActivityObj() {
        if(StringUtils.isNotEmpty(getActivityInfo())){
            return JSONObject.parseObject(getActivityInfo(), ItemActivityObj.class);
        }
        return new ItemActivityObj();
    }
}
