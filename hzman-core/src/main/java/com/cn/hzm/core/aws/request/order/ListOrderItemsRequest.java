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
public class ListOrderItemsRequest extends OrderRequest{

    @JSONField(name="AmazonOrderId")
    private String amazonOrderId;

    @Override
    protected void privateJson(JSONObject jo) {
    }
}
