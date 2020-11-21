package com.cn.hzm.factory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.FactoryDO;
import com.cn.hzm.core.util.SqlCommonUtil;
import com.cn.hzm.factory.dao.FactoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/6 4:42 下午
 */
@Service
public class FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;

    public List<FactoryDO> getListByCondition(Map<String, String> condition){
        QueryWrapper<FactoryDO> query = new QueryWrapper<>();
        if(condition.size()!=0){
        }
        query.orderByAsc("ctime");
        //query.last(SqlCommonUtil.limitOffsetSql(offset, limit));
        return factoryMapper.selectList(query);
    }

    public FactoryDO getByFid(Integer fId){
        return factoryMapper.selectById(fId);
    }

    public FactoryDO getByName(String name){
        QueryWrapper<FactoryDO> query = new QueryWrapper<>();
        query.eq("factory_name", name);
        return factoryMapper.selectOne(query);
    }

    /**
     * 创建厂家
     * @param factoryDO
     */
    public void createFactory(FactoryDO factoryDO){
        factoryDO.setUtime(new Date());
        factoryDO.setCtime(new Date());
        factoryMapper.insert(factoryDO);
    }

    /**
     * 更新厂家
     * @param factoryDO
     */
    public void updateFactory(FactoryDO factoryDO){
        factoryDO.setUtime(new Date());
        factoryMapper.updateById(factoryDO);
    }

    /**
     * 删除厂家
     * @param fId
     */
    public void deleteFactory(Integer fId){
        factoryMapper.deleteById(fId);
    }
}
