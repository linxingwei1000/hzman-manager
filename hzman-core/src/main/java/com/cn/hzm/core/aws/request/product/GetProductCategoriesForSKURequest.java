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
public class GetProductCategoriesForSKURequest extends ProductRequest {

//    @JSONField(name="MarketplaceId")
//    private String marketplaceId;

    @JSONField(name="SellerSKU")
    private String sellerSKU;

    @Override
    protected void privateJson(JSONObject jo) {
    }
}
