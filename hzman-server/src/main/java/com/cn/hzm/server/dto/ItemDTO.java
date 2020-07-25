package com.cn.hzm.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/17 9:50 下午
 */
@ApiModel(description = "商品DTO")
@Data
public class ItemDTO{

    @ApiModelProperty(value = "ID", example = "100")
    private Integer id;

    @ApiModelProperty(value = "ASIN", example = "B07BGY7HWK")
    private String asin;

    @ApiModelProperty(value = "FNSKU", example = "N190301")
    private String fnsku;

    @ApiModelProperty(value = "SKU", example = "N190301")
    private String sku;

    @ApiModelProperty(value = "库存信息", example = "N190301")
    private InventoryDTO inventoryDTO;

    @ApiModelProperty("创建时间")
    private Date ctime;

    @ApiModelProperty("更新时间")
    private Date utime;
}
