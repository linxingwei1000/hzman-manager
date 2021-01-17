package com.cn.hzm.core.aws.request.fulfilment;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:45 下午
 */
@Data
public class ShipmentItemsByNextTokenRequest extends FulfilmentRequest {

    @JSONField(name = "NextToken")
    private String nextToken;

    @Override
    protected void privateJson(JSONObject jo) {

    }
}
