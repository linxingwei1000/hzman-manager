package com.cn.hzm.core.aws.request.order;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:49 下午
 */
@Data
public class ListOrderItemsByTokenRequest extends OrderRequest {

    @JSONField(name="NextToken")
    private String nextToken;

    @Override
    protected void privateJson(JSONObject jo) {

    }
}
