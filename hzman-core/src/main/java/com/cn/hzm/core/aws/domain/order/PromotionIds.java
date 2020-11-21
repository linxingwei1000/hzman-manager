package com.cn.hzm.core.aws.domain.order;



import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/17 11:35 上午
 */
@Data
@XStreamAlias("PromotionIds")
public class PromotionIds {

    @XStreamImplicit(itemFieldName="PromotionId")
    List<String> PromotionId;
}
