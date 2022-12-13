package com.cn.hzm.core.aws.request.finance;

import com.alibaba.fastjson.annotation.JSONField;
import com.cn.hzm.core.aws.request.BaseRequest;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:37 上午
 */
@Data
public abstract class FinanceRequest extends BaseRequest {

    private String apiSection = "Finances";

    @JSONField(name="Version")
    private String version = "2015-05-01";

    @JSONField(name="Action")
    private String action;


}
