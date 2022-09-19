package com.cn.hzm.core.entity.thread;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * @author linxingwei
 * @date 13.9.22 6:19 下午
 */
@Data
@AllArgsConstructor
public class UpdateRecordFuture {

    private Set<String> threadFixSaleInfoDay;

    private Integer count;
}
