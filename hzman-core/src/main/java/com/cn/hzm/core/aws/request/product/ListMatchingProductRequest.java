package com.cn.hzm.core.aws.request.product;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:15 下午
 */
@Data
public class ListMatchingProductRequest extends ProductRequest {

    @JSONField(name="Query")
    private String query;

    @JSONField(name="QueryContextId")
    private Integer queryContextId;

    @Override
    protected void privateJson(JSONObject jo) {
    }
}
