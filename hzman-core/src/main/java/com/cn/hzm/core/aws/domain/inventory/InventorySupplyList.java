package com.cn.hzm.core.aws.domain.inventory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 2:36 下午
 */
@Data
@XStreamAlias("InventorySupplyList")
public class InventorySupplyList {

    @XStreamImplicit(itemFieldName="member")
    private List<Member> members;
}
