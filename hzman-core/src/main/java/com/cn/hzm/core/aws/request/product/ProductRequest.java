package com.cn.hzm.core.aws.request.product;

import com.alibaba.fastjson.annotation.JSONField;
import com.cn.hzm.core.aws.request.BaseRequest;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:34 上午
 */
@Data
public abstract class ProductRequest extends BaseRequest {

    @JSONField(name="MarketplaceId")
    protected String MARKETPLACE_ID = "ATVPDKIKX0DER";

    private String apiSection = "Products";

    @JSONField(name="Version")
    private String version = "2011-10-01";

    @JSONField(name="Action")
    private String action;
}
