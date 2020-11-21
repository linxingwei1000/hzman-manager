package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:55 下午
 */
@Data
@XStreamAlias("ListOrderItemsByNextTokenResponse")
public class ListOrderItemsByNextTokenResponse {

    @XStreamAlias(value="ListOrderItemsByNextTokenResult")
    ListOrderItemsByNextTokenResult listOrderItemsByNextTokenResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
