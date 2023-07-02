package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FactoryDo;
import com.cn.hzm.core.repository.mapper.FactoryMapper;
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
public class FactoryDao {

    @Autowired
    private FactoryMapper factoryMapper;

    public List<FactoryDo> getListByCondition(Map<String, String> condition){
        QueryWrapper<FactoryDo> query = new QueryWrapper<>();
        if(condition.size()!=0){
        }
        query.orderByAsc("ctime");
        //query.last(SqlCommonUtil.limitOffsetSql(offset, limit));
        return factoryMapper.selectList(query);
    }

    public FactoryDo getByFid(Integer fId){
        return factoryMapper.selectById(fId);
    }

    public List<FactoryDo> getByIds(List<Integer> fIds){
        return factoryMapper.selectBatchIds(fIds);
    }

    public FactoryDo getByName(String name){
        QueryWrapper<FactoryDo> query = new QueryWrapper<>();
        query.eq("factory_name", name);
        return factoryMapper.selectOne(query);
    }

    /**
     * 创建厂家
     * @param factoryDO
     */
    public void createFactory(FactoryDo factoryDO){
        factoryDO.setUtime(new Date());
        factoryDO.setCtime(new Date());
        factoryMapper.insert(factoryDO);
    }

    /**
     * 更新厂家
     * @param factoryDO
     */
    public void updateFactory(FactoryDo factoryDO){
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
