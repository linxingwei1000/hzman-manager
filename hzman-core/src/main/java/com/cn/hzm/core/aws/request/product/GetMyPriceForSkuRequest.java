package com.cn.hzm.core.aws.request.product;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:15 下午
 */
@Data
public class GetMyPriceForSkuRequest extends ProductRequest {

    @JSONField(serialize = false)
    private List<String> skus;

    @Override
    protected void privateJson(JSONObject jo) {
        String key = "SellerSKUList.SellerSKU.";
        int num = 1;
        for (String sku : skus) {
            jo.put(key + num, sku);
            num++;
        }
    }
}
