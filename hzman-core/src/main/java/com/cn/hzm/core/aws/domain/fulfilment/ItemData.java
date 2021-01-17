package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 11:59 上午
 */
@Data
@XStreamAlias("ItemData")
public class ItemData {

    @XStreamImplicit(itemFieldName="member")
    private List<Member> list;
}
