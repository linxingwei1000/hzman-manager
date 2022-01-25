package com.cn.hzm.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.FatherChildRelationDO;
import com.cn.hzm.item.dao.FatherChildRelationMapper;
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
public class FatherChildRelationService {

    @Autowired
    private FatherChildRelationMapper fatherChildRelationMapper;

    public List<FatherChildRelationDO> getAllRelation(String fatherAsin){
        QueryWrapper<FatherChildRelationDO> query = new QueryWrapper<>();
        if(!StringUtils.isEmpty(fatherAsin)){
            query.eq("father_asin", fatherAsin);
        }
        return fatherChildRelationMapper.selectList(query);
    }


    public FatherChildRelationDO getRelationByFatherAndChild(String fatherSku, String childSku){
        QueryWrapper<FatherChildRelationDO> query = new QueryWrapper<>();
        query.eq("father_sku", fatherSku);
        query.eq("child_sku", childSku);
        return fatherChildRelationMapper.selectOne(query);
    }

    public FatherChildRelationDO getRelationByFatherAndChildAsin(String fatherAsin, String childAsin){
        QueryWrapper<FatherChildRelationDO> query = new QueryWrapper<>();
        query.eq("father_asin", fatherAsin);
        query.eq("child_asin", childAsin);
        return fatherChildRelationMapper.selectOne(query);
    }

    /**
     * 创建商品
     * @param relationDO
     */
    public void createRelation(FatherChildRelationDO relationDO){
        relationDO.setUtime(new Date());
        relationDO.setCtime(new Date());
        fatherChildRelationMapper.insert(relationDO);
    }
}
