package com.cn.hzm.core.aws.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/18 3:02 下午
 */
@Data
@XStreamAlias(value="ns2:SmallImage")
public class SmallImage {
    @XStreamAlias(value="ns2:URL")
    private String url;

    @XStreamAlias(value="ns2:Height")
    private String height;

    @XStreamAlias(value="ns2:Width")
    private String width;
}
