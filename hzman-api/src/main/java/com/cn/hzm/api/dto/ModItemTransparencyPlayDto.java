package com.cn.hzm.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author linxingwei
 * @date 14.5.23 6:07 下午
 */
@Data
public class ModItemTransparencyPlayDto implements Serializable {

    /**
     * 操作类型：1.打标,2删标
     */
    private Integer operate;

    private List<String> skus;

    private List<Integer> itemIds;

}
