package com.cn.hzm.core.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 13.4.23 6:12 下午
 */
@Getter
@AllArgsConstructor
public enum SpiderType {

    CREATE_ORDER(1,"爬取亚马逊新建订单"),

    SHIPMENT_INFO(2,"爬取货物单"),
    ;

    private Integer code;

    private String desc;

    public static List<Map<String, Object>> jsonEnum(){
        List<Map<String, Object>> enumList = Lists.newArrayList();
        for (SpiderType spiderType : values()) {
            Map<String, Object> enumMap = Maps.newHashMap();
            enumMap.put("id", spiderType.getCode());
            enumMap.put("desc", spiderType.getDesc());
            enumList.add(enumMap);
        }
        return enumList;
    }
}
