package com.cn.hzm.aws.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 10:34 上午
 */
@Data
public class BaseRequest {

    @JSONField(name="SellerId")
    protected String SELLER_ID = "AK0HQWR8PUJRG";

    @JSONField(name="MWSAuthToken")
    protected String MWS_AUTH_TOKEN = "amzn.mws.58b92ec2-2cb4-a64b-a697-fcb1b7909a1d";

    @JSONField(name="AWSAccessKeyId")
    protected String AWS_ACCESS_KEY_ID = "AKIAJIDG7RO7VB7ZAJZA";

    @JSONField(name="SignatureMethod")
    protected String SignatureMethod = "HmacSHA256";

    @JSONField(name="SignatureVersion")
    protected String SignatureVersion= "2";

    @JSONField(name="Signature")
    protected String signature;

    @JSONField(name="Timestamp")
    private String timestamp;
}
