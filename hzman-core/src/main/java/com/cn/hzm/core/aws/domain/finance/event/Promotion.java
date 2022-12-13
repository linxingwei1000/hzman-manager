package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 5:00 下午
 */
@Data
public class Promotion {

    @XStreamAlias(value="PromotionType")
    private String promotionType;

    @XStreamAlias(value="PromotionAmount")
    private FeeAmount promotionAmount;

    @XStreamAlias(value="PromotionId")
    private String promotionId;
}
