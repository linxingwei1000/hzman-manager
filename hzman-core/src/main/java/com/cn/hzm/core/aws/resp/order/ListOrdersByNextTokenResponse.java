package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/16 2:55 下午
 */
@Data
@XStreamAlias("ListOrdersByNextTokenResponse")
public class ListOrdersByNextTokenResponse {

    @XStreamAlias(value="ListOrdersByNextTokenResult")
    ListOrdersByNextTokenResult listOrdersByNextTokenResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
