package com.cn.hzm.core.aws.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:06 下午
 */
@Data
@XStreamAlias("SalesRankings")
public class SalesRankings {

    @XStreamImplicit(itemFieldName="SalesRank")
    private List<SalesRank> salesRanks;
}
