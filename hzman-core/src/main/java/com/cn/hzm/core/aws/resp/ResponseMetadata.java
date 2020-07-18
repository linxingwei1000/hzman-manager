package com.cn.hzm.core.aws.resp;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:54 下午
 */
@Data
@XStreamAlias("ResponseMetadata")
public class ResponseMetadata {

    @XStreamAlias(value="RequestId")
    String RequestId;

}
