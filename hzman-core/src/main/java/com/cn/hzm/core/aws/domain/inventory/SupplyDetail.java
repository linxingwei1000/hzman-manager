package com.cn.hzm.core.aws.domain.inventory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/20 2:40 下午
 */
@Data
@XStreamAlias(value="SupplyDetail")
public class SupplyDetail {

    @XStreamImplicit(itemFieldName="member")
    List<DetailMember> members;
}
