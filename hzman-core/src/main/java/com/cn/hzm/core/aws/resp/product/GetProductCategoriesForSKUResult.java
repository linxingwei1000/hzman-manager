package com.cn.hzm.core.aws.resp.product;

import com.cn.hzm.core.aws.domain.product.CategoryParent;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;


/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:19 下午
 */
@Data
@XStreamAlias("GetProductCategoriesForSKUResult")
public class GetProductCategoriesForSKUResult {

    @XStreamImplicit(itemFieldName="Self")
    private List<CategoryParent> categoryParents;

}
