package com.cn.hzm.core.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/7 11:16 下午
 */
@Data
@XStreamAlias("Identifiers")
public class Identifiers {

    @XStreamAlias(value="MarketplaceASIN")
    private MarketplaceASIN marketplaceASIN;
}
