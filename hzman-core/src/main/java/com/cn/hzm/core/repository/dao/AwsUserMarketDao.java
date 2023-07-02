package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.repository.mapper.AwsUserMarketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author linxingwei
 * @date 13.4.23 4:48 下午
 */
@Service
public class AwsUserMarketDao {

    @Autowired
    private AwsUserMarketMapper awsUserMarketMapper;

    public Integer insert(AwsUserMarketDo awsUserMarketDo){
        awsUserMarketDo.setUtime(new Date());
        awsUserMarketDo.setCtime(new Date());
        return awsUserMarketMapper.insert(awsUserMarketDo);
    }

    public Integer delete(Integer id){
        return awsUserMarketMapper.deleteById(id);
    }

    public List<AwsUserMarketDo> all(){
        QueryWrapper<AwsUserMarketDo> query = new QueryWrapper<>();
        return awsUserMarketMapper.selectList(query);
    }

    public AwsUserMarketDo getById(Integer id){
        return awsUserMarketMapper.selectById(id);
    }

    public List<AwsUserMarketDo> getByUserId(Integer userId){
        QueryWrapper<AwsUserMarketDo> query = new QueryWrapper<>();
        query.eq("aws_user_id", userId);
        return awsUserMarketMapper.selectList(query);
    }

    public List<AwsUserMarketDo> getByMarketId(String marketId){
        QueryWrapper<AwsUserMarketDo> query = new QueryWrapper<>();
        query.eq("market_id", marketId);
        return awsUserMarketMapper.selectList(query);
    }

    public AwsUserMarketDo getByUserIdAndMarketId(Integer userId, String marketId){
        QueryWrapper<AwsUserMarketDo> query = new QueryWrapper<>();
        query.eq("aws_user_id", userId);
        query.eq("market_id", marketId);
        return awsUserMarketMapper.selectOne(query);
    }
}
