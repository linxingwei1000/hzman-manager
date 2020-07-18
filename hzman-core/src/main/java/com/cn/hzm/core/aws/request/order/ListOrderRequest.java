package com.cn.hzm.core.aws.request.order;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:45 下午
 */
@Data
public class ListOrderRequest extends OrderRequest{

    @JSONField(name="CreatedAfter")
    private String createdAfter;

    @JSONField(name="CreatedBefore")
    private String createdBefore;

    @JSONField(serialize = false)
    private List<String> marketplaceIds;

    @Override
    protected void privateJson(JSONObject jo) {
        String key = "MarketplaceId.Id.";
        int num = 1;
        for (String id : marketplaceIds) {
            jo.put(key + num, id);
            num++;
        }
    }
}
