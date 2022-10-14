package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 25.9.22 4:55 下午
 */
@Data
@XStreamAlias("Self")
public class CategorySelf {

    @XStreamAlias(value="Parent")
    CategoryParent categoryParent;

    @XStreamAlias(value="ProductCategoryId")
    String productCategoryId;

    @XStreamAlias(value="ProductCategoryName")
    String productCategoryName;

}
