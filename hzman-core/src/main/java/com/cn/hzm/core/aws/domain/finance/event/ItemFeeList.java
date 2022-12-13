package com.cn.hzm.core.aws.domain.finance.event;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * @author linxingwei
 * @date 6.11.22 4:51 下午
 */
@Data
public class ItemFeeList {

    @XStreamImplicit(itemFieldName="FeeComponent")
    private List<FeeComponent> feeComponents;
}
