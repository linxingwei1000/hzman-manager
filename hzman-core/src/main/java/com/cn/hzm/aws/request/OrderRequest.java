package com.cn.hzm.aws.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:37 上午
 */
@Data
public class OrderRequest extends BaseRequest {

    private String apiSection = "Orders";

    @JSONField(name="Version")
    private String version = "2013-09-01";

    @JSONField(name="Action")
    private String action;

    @JSONField(name="CreatedAfter")
    private String createdAfter;

    @JSONField(name="CreatedBefore")
    private String createdBefore;

    @JSONField(name="MarketplaceId.Id.1")
    private String marketplaceIdOne;
}
