package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:07 上午
 */
@Data
@XStreamAlias("ProductInfo")
public class ProductInfo {

    @XStreamAlias(value="NumberOfItems")
    private String numberOfItems;
}
