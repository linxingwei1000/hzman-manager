package com.cn.hzm.core.aws.resp.order;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:09 下午
 */
@Data
@XStreamAlias("ListOrdersResponse")
public class ListOrdersResponse {

    @XStreamAlias(value="ListOrdersResult")
    ListOrdersResult listOrdersResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
