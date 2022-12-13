package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 6.11.22 5:00 下午
 */
@Data
public class PromotionList {

    @XStreamImplicit(itemFieldName="Promotion")
    private List<Promotion> promotions;
}
