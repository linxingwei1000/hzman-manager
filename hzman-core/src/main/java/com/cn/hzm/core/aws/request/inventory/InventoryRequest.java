package com.cn.hzm.core.aws.request.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.cn.hzm.core.aws.request.BaseRequest;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:37 上午
 */
@Data
public abstract class InventoryRequest extends BaseRequest {

    private String apiSection = "Fulfilment";

    @JSONField(name="Version")
    private String version = "2010-10-01";

    @JSONField(name="MarketplaceId")
    protected String MARKETPLACE_ID = "ATVPDKIKX0DER";

    @JSONField(name="Action")
    private String action;


}
