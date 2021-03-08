package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.order.dao.SaleInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/9 7:09 下午
 */
@Service
public class SaleInfoService {

    @Autowired
    private SaleInfoMapper saleInfoMapper;

    /**
     * 创建销量信息
     *
     * @param saleInfoDO
     */
    public Boolean createSaleInfo(SaleInfoDO saleInfoDO) {
        saleInfoDO.setUtime(new Date());
        saleInfoDO.setCtime(new Date());
        return saleInfoMapper.insert(saleInfoDO) != 0;
    }

    /**
     * 更新销量信息
     *
     * @param saleInfoDO
     */
    public Integer updateSaleInfo(SaleInfoDO saleInfoDO) {
        saleInfoDO.setUtime(new Date());
        return saleInfoMapper.updateById(saleInfoDO);
    }

    public SaleInfoDO getSaleInfoDOByDate(String statDate, String sku){
        QueryWrapper<SaleInfoDO> query = new QueryWrapper<>();
        query.eq("stat_date", statDate);
        query.eq("sku", sku);
        return saleInfoMapper.selectOne(query);
    }

    public List<SaleInfoDO> getSaleInfoByDurationDate(String sku, String statBeginDate, String statEndDate){
        QueryWrapper<SaleInfoDO> query = new QueryWrapper<>();
        query.between("stat_date", statBeginDate, statEndDate);
        if(!StringUtils.isEmpty(sku)){
            query.eq("sku", sku);
        }
        return saleInfoMapper.selectList(query);
    }
}
