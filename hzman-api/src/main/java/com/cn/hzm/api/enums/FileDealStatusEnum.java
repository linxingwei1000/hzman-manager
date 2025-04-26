package com.cn.hzm.api.enums;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 5:21 下午
 */
public enum FileDealStatusEnum {

    /**
     * 订单状态
     */
    CREATE(0, "新建"),
    PROCESSING(1, "处理中"),
    PROCESS_SKIP(2, "跳过"),
    PROCESS_FINISH(3, "完成"),
    PROCESS_FAIL(4, "失败"),
    ;

    private Integer code;

    private String desc;

    FileDealStatusEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getDesc(){
        return this.desc;
    }

    public static FileDealStatusEnum getEnumByCode(Integer code){
        for(FileDealStatusEnum os: values()){
            if(os.getCode().equals(code)){
                return os;
            }
        }
        return null;
    }
}
