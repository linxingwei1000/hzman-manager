package com.cn.hzm.factory.enums;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 5:21 下午
 */
public enum OrderStatusEnum {

    /**
     * 订单状态
     */
    ORDER_START(0, "订单开始"),
    ORDER_CONFIRM_PLACE(1, "公司确认下单"),
    ORDER_FACTORY_CONFIRM(2, "厂家确认订单"),
    ORDER_CONFIRM(3, "公司确认订单"),
    ORDER_FACTORY_DELIVERY(4, "厂家交货"),
    ORDER_DELIVERY(5, "公司确认收货"),
    ORDER_PAY(6, "财务支付订单"),
    ;

    private Integer code;

    private String desc;

    OrderStatusEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getDesc(){
        return this.desc;
    }

    public static OrderStatusEnum getEnumByCode(Integer code){
        for(OrderStatusEnum os: values()){
            if(os.getCode().equals(code)){
                return os;
            }
        }
        return null;
    }
}
