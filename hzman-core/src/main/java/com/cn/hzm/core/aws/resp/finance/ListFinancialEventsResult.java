package com.cn.hzm.core.aws.resp.finance;

import com.cn.hzm.core.aws.domain.finance.FinancialEvents;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author linxingwei
 * @date 6.11.22 4:17 下午
 */
@Data
@XStreamAlias("ListFinancialEventsResult")
public class ListFinancialEventsResult {

    @XStreamAlias(value="FinancialEvents")
    FinancialEvents financialEvents;
}
