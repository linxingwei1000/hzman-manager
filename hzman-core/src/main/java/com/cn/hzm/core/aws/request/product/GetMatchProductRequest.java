package com.cn.hzm.core.aws.request.product;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:14 下午
 */
@Data
public class GetMatchProductRequest extends ProductRequest {

    @JSONField(name = "IdType")
    private String idType;

    @JSONField(serialize = false)
    private List<String> ids;

    @Override
    protected void privateJson(JSONObject jo) {
        String key = "IdList.Id.";
        int num = 1;
        for (String id : ids) {
            jo.put(key + num, id);
            num++;
        }
    }
}
