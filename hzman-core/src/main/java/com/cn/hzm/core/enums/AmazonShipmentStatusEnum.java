package com.cn.hzm.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author linxingwei
 * @date 31.7.21 11:34 上午
 */
@Getter
@AllArgsConstructor
public enum AmazonShipmentStatusEnum {

    STATUS_WORKING("WORKING", "卖家已创建货件，但尚未发货"),
    STATUS_SHIPPED("SHIPPED", "承运人已取件"),
    STATUS_IN_TRANSIT("IN_TRANSIT", "承运人通知亚马逊配送中心"),
    STATUS_DELIVERED("DELIVERED", "承运人将货件送至亚马逊配送中心"),
    STATUS_CHECKED_IN("CHECKED_IN", "货件在亚马逊配送中心收货区登记"),
    STATUS_RECEIVING("RECEIVING", "货件已到达亚马逊配送中心，部分商品尚未标记为已收到"),
    STATUS_CLOSED("WORKING", "货件已到达亚马逊配送中心，且所有商品已标记为已收到"),
    STATUS_CANCELLED("CANCELLED", "卖家在将货件发送至亚马逊配送中心后取消了货件"),
    STATUS_DELETED("DELETED", "卖家在将货件发送至亚马逊配送中心前取消了货件"),
    STATUS_ERROR("ERROR", "货件出错，但其并非亚马逊处理"),
    ;

    private String code;

    private String desc;


}
