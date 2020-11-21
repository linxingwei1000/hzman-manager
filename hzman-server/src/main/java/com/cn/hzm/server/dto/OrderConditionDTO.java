package com.cn.hzm.server.dto;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 3:42 下午
 */
@ApiModel(description = "订单条件搜索DTO")
@Data
public class OrderConditionDTO extends PageDTO {

    @ApiModelProperty(value = "下单时间，yyyy-mm-dd")
    private String purchaseDate;

    @ApiModelProperty(value = "下单开始时间，yyyy-mm-dd")
    private String purchaseDateBegin;

    @ApiModelProperty(value = "下单结束时间，yyyy-mm-dd")
    private String purchaseDateEnd;

    @ApiModelProperty(value = "订单最后更新时间，yyyy-mm-dd")
    private String lastUpdateDate;

    @ApiModelProperty(value = "订单最后更新时间区间起，yyyy-mm-dd")
    private String lastUpdateDateBegin;

    @ApiModelProperty(value = "订单最后更新时间区间末，yyyy-mm-dd")
    private String lastUpdateDateEnd;

    @ApiModelProperty(value = "订单状态")
    @JSONField(name = "order_status")
    private String orderStatus;

    @ApiModelProperty(value = "订单类型")
    @JSONField(name = "order_type")
    private String orderType;

    @ApiModelProperty(value = "买家")
    private String buyerName;
}
