package com.cn.hzm.api.dto;

import lombok.Data;

/**
 * @author linxingwei
 * @date 27.4.25 6:10 下午
 */
@Data
public class SaleInfoDurationDto {

    /**
     * 横轴
     */
    private String date;

    /**
     * 竖轴
     */
    private Integer num;

    /**
     * 节点信息
     */
    private SaleInfoDescDto detailInfo;

}
