package com.cn.hzm.core.aws.resp.product;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 2:15 下午
 */
@Data
@XStreamAlias("GetProductCategoriesForSKUResponse")
public class GetProductCategoriesForSKUResponse {

    @XStreamAlias(value = "GetProductCategoriesForSKUResult")
    GetProductCategoriesForSKUResult getProductCategoriesForSKUResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
