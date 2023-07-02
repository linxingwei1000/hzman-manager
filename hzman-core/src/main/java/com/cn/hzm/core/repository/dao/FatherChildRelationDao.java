package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FatherChildRelationDo;
import com.cn.hzm.core.repository.mapper.FatherChildRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author linxingwei
 * @date 19.1.22 4:02 下午
 */
@Service
public class FatherChildRelationDao {

    @Autowired
    private FatherChildRelationMapper fatherChildRelationMapper;

    public List<FatherChildRelationDo> getAllRelation(Integer userMarketId, String fatherAsin) {
        QueryWrapper<FatherChildRelationDo> query = new QueryWrapper<>();
        if (userMarketId != null) {
            query.eq("user_market_id", userMarketId);
        }

        if (!StringUtils.isEmpty(fatherAsin)) {
            query.eq("father_asin", fatherAsin);
        }
        return fatherChildRelationMapper.selectList(query);
    }

    public FatherChildRelationDo getRelationByFatherAndChildAsin(Integer userMarketId, String fatherAsin, String childAsin) {
        QueryWrapper<FatherChildRelationDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("father_asin", fatherAsin);
        query.eq("child_asin", childAsin);
        return fatherChildRelationMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param relationDO
     */
    public void createRelation(FatherChildRelationDo relationDO) {
        relationDO.setUtime(new Date());
        relationDO.setCtime(new Date());
        fatherChildRelationMapper.insert(relationDO);
    }
}
