package com.cn.hzm.core.aws.request.inventory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 4:54 下午
 */
@Data
public class ListInventoryRequest extends InventoryRequest{

    List<String> skus;

    @JSONField(name="ResponseGroup")
    String responseGroup;


    @Override
    protected void privateJson(JSONObject jo) {
        String key = "SellerSkus.member.";
        int num = 1;
        for (String sku : skus) {
            jo.put(key + num, sku);
            num++;
        }
    }
}
