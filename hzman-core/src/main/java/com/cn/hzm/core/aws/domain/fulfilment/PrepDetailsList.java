package com.cn.hzm.core.aws.domain.fulfilment;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 12:01 下午
 */
@Data
@XStreamAlias("PrepDetailsList")
public class PrepDetailsList {

    @XStreamImplicit(itemFieldName="PrepDetails")
    private List<PrepDetails> list;

}
