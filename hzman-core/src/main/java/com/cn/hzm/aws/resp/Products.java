package com.cn.hzm.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:11 下午
 */
@Data
@XStreamAlias("Products")
public class Products {

    @XStreamImplicit(itemFieldName="Product")
    private List<Product> list;
}
