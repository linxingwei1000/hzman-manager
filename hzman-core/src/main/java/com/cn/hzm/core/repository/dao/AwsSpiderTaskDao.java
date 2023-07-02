package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AwsSpiderTaskDo;
import com.cn.hzm.core.repository.mapper.AwsSpiderTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author linxingwei
 * @date 13.4.23 4:48 下午
 */
@Service
public class AwsSpiderTaskDao {

    @Autowired
    private AwsSpiderTaskMapper awsSpiderTaskMapper;

    public Integer insert(AwsSpiderTaskDo awsSpiderTaskDo){
        awsSpiderTaskDo.setUtime(new Date());
        awsSpiderTaskDo.setCtime(new Date());
        return awsSpiderTaskMapper.insert(awsSpiderTaskDo);
    }

    public Integer delete(Integer id){
        return awsSpiderTaskMapper.deleteById(id);
    }

    public Integer update(AwsSpiderTaskDo awsSpiderTaskDo){
        awsSpiderTaskDo.setUtime(new Date());
        return awsSpiderTaskMapper.updateById(awsSpiderTaskDo);
    }

    public AwsSpiderTaskDo select(Integer id){
        return awsSpiderTaskMapper.selectById(id);
    }

    public List<AwsSpiderTaskDo> getActiveSpiderTask(){
        QueryWrapper<AwsSpiderTaskDo> query = new QueryWrapper<>();
        query.eq("is_active", 1);
        return awsSpiderTaskMapper.selectList(query);
    }

    public List<AwsSpiderTaskDo> getByUserMarketId(Integer userMarketId){
        QueryWrapper<AwsSpiderTaskDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        return awsSpiderTaskMapper.selectList(query);
    }

    public AwsSpiderTaskDo getByUserMarketIdAndSpiderType(Integer userMarketId, Integer spiderType){
        QueryWrapper<AwsSpiderTaskDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("spider_type", spiderType);
        return awsSpiderTaskMapper.selectOne(query);
    }
}
