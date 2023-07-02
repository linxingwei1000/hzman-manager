package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.SaleInfoDo;
import com.cn.hzm.core.repository.mapper.SaleInfoMapper;
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
public class SaleInfoDao {

    @Autowired
    private SaleInfoMapper saleInfoMapper;

    /**
     * 创建销量信息
     *
     * @param saleInfoDO
     */
    public Boolean createSaleInfo(SaleInfoDo saleInfoDO) {
        saleInfoDO.setUtime(new Date());
        saleInfoDO.setCtime(new Date());
        return saleInfoMapper.insert(saleInfoDO) != 0;
    }

    /**
     * 更新销量信息
     *
     * @param saleInfoDO
     */
    public Integer updateSaleInfo(SaleInfoDo saleInfoDO) {
        saleInfoDO.setUtime(new Date());
        return saleInfoMapper.updateById(saleInfoDO);
    }

    public SaleInfoDo getSaleInfoDOByDate(String statDate, Integer userMarketId, String sku) {
        QueryWrapper<SaleInfoDo> query = new QueryWrapper<>();
        query.eq("stat_date", statDate);
        query.eq("user_market_id", userMarketId);
        query.eq("sku", sku);
        return saleInfoMapper.selectOne(query);
    }

    public List<SaleInfoDo> getSaleInfoDOByDate(Integer userMarketId, String statDate) {
        QueryWrapper<SaleInfoDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("stat_date", statDate);
        return saleInfoMapper.selectList(query);
    }

    public List<SaleInfoDo> getSaleInfoByDurationDate(String sku, Integer userMarketId, String statBeginDate, String statEndDate) {
        QueryWrapper<SaleInfoDo> query = new QueryWrapper<>();
        query.between("stat_date", statBeginDate, statEndDate);
        if (userMarketId != null) {
            query.eq("user_market_id", userMarketId);
        }
        if (!StringUtils.isEmpty(sku)) {
            query.eq("sku", sku);
        }
        return saleInfoMapper.selectList(query);
    }
}
