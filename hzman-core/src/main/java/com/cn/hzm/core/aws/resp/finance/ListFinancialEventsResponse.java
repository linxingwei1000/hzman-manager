package com.cn.hzm.core.aws.resp.finance;

import com.cn.hzm.core.aws.resp.ResponseMetadata;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:16 下午
 */
@Data
@XStreamAlias("ListFinancialEventsResponse")
public class ListFinancialEventsResponse {

    @XStreamAlias(value="ListFinancialEventsResult")
    ListFinancialEventsResult listFinancialEventsResult;

    @XStreamAlias(value = "ResponseMetadata")
    ResponseMetadata responseMetadata;
}
