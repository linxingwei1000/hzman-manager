package com.cn.hzm.core.aws.request.fulfilment;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:45 下午
 */
@Data
public class ShipmentItemsRequest extends FulfilmentRequest {

    @JSONField(name = "ShipmentId")
    private String shipmentId;

    @JSONField(name = "LastUpdatedAfter")
    private String lastUpdatedAfter;

    @JSONField(name = "LastUpdatedBefore")
    private String lastUpdatedBefore;

    @Override
    protected void privateJson(JSONObject jo) {

    }
}
