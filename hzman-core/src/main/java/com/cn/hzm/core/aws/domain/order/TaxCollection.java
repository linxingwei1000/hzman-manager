package com.cn.hzm.core.aws.domain.order;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/18 4:31 下午
 */
@Data
@XStreamAlias("TaxCollection")
public class TaxCollection {

    @XStreamAlias(value="ResponsibleParty")
    private String responsibleParty;

    @XStreamAlias(value="Model")
    private String model;
}
