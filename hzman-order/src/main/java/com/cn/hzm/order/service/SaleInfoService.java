package com.cn.hzm.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.SaleInfoDO;
import com.cn.hzm.order.dao.SaleInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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
    public Boolean updateSaleInfo(SaleInfoDO saleInfoDO) {
        saleInfoDO.setUtime(new Date());
        return saleInfoMapper.updateById(saleInfoDO) != 0;
    }

    public SaleInfoDO getSaleInfoDOByDate(String statDate, String sku){
        QueryWrapper<SaleInfoDO> query = new QueryWrapper<>();
        query.eq("stat_date", statDate);
        query.eq("sku", sku);
        return saleInfoMapper.selectOne(query);
    }
}