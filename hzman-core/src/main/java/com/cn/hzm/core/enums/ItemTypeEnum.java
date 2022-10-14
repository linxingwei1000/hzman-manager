package com.cn.hzm.core.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 2.10.22 1:06 下午
 */
@Getter
@AllArgsConstructor
public enum ItemTypeEnum {

    BRACELET("BRACELET","手镯"),

    COSTUME_OUTFIT("COSTUME_OUTFIT","皮具"),

    NECKLACE("NECKLACE","项链"),

    RING("RING","戒指"),

    EARRING("EARRING","耳环"),
            ;

    private String code;

    private String desc;

    public static List<Map<String, Object>> jsonEnum(){
        List<Map<String, Object>> enumList = Lists.newArrayList();
        for (ItemTypeEnum typeEnum : values()) {
            Map<String, Object> enumMap = Maps.newHashMap();
            enumMap.put("code", typeEnum.getCode());
            enumMap.put("desc", typeEnum.getDesc());
            enumList.add(enumMap);
        }
        return enumList;
    }
}
