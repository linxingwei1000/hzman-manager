package com.cn.hzm.core.aws.request.order;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.cn.hzm.core.aws.request.BaseRequest;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:37 上午
 */
@Data
public abstract class OrderRequest extends BaseRequest {

    private String apiSection = "Orders";

    @JSONField(name="Version")
    private String version = "2013-09-01";

    @JSONField(name="Action")
    private String action;


}
