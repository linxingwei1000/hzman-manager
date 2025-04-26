package com.cn.hzm.api.enums;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 5:21 下午
 */
public enum FileTypeEnum {

    /**
     * 订单状态
     */
    CREATE(1, "本地库存处理文件"),
    ;

    private Integer code;

    private String desc;

    FileTypeEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getDesc(){
        return this.desc;
    }

    public static FileTypeEnum getEnumByCode(Integer code){
        for(FileTypeEnum os: values()){
            if(os.getCode().equals(code)){
                return os;
            }
        }
        return null;
    }
}
