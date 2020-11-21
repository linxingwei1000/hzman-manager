package com.cn.hzm.core.aws.request.order;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:49 下午
 */
@Data
public class GetOrderRequest extends OrderRequest {

    @JSONField(serialize = false)
    private List<String> amazonOrderId;

    @Override
    protected void privateJson(JSONObject jo) {
        String key = "AmazonOrderId.Id.";
        int num = 1;
        for (String id : amazonOrderId) {
            jo.put(key + num, id);
            num++;
        }
    }
}
