package com.cn.hzm.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:09 下午
 */
@Data
@XStreamAlias("ListMatchingProductsResponse")
public class ListMatchingProductsResponse {

    @XStreamAlias(value="ListMatchingProductsResult")
    ListMatchingProductsResult listMatchingProductsResult;
}
