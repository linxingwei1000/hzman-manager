package com.cn.hzm.core.aws.request.finance;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:11 下午
 */
@Data
public class ListFinancialEventsRequest extends FinanceRequest{

    @JSONField(name = "AmazonOrderId")
    private String amazonOrderId;

    @Override
    protected void privateJson(JSONObject jo) {

    }
}
