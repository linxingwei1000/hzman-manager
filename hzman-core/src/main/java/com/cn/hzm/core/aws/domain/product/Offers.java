package com.cn.hzm.core.aws.domain.product;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/3/24 3:25 下午
 */
@Data
@XStreamAlias(value="Offers")
public class Offers {

    @XStreamAlias(value="Offer")
    private Offer offer;

}
