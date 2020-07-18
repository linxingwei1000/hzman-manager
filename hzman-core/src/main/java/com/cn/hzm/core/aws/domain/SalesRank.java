package com.cn.hzm.core.aws.domain;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:41 下午
 */
@Data
@XStreamAlias("SalesRank")
public class SalesRank {

    @XStreamAlias(value="ProductCategoryId")
    private String productCategoryId;

    @XStreamAlias(value="Rank")
    private String rank;
}
