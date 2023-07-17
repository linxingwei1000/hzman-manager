package com.cn.hzm.api.enums;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 5:21 下午
 */
public enum ReplenishmentEnum {

    /**
     * 智能补货状态
     */
    REPLENISHMENT_SHIP(1, "发货"),
    REPLENISHMENT_ORDER(2, "订货"),
    ;

    private Integer code;

    private String desc;

    ReplenishmentEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getDesc(){
        return this.desc;
    }

    public static ReplenishmentEnum getEnumByCode(Integer code){
        for(ReplenishmentEnum os: values()){
            if(os.getCode().equals(code)){
                return os;
            }
        }
        return null;
    }
}
