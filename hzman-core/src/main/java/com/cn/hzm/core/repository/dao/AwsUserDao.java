package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AwsUserDo;
import com.cn.hzm.core.repository.mapper.AwsUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author linxingwei
 * @date 13.4.23 4:48 下午
 */
@Service
public class AwsUserDao {

    @Autowired
    private AwsUserMapper awsUserMapper;

    public List<AwsUserDo> selectByCondition(String remark) {
        QueryWrapper<AwsUserDo> query = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(remark)) {
            query.like("remark", remark);
        }
        return awsUserMapper.selectList(query);
    }


    public Integer insert(AwsUserDo awsUserDo) {
        awsUserDo.setUtime(new Date());
        awsUserDo.setCtime(new Date());
        return awsUserMapper.insert(awsUserDo);
    }

    public Integer delete(Integer id) {
        return awsUserMapper.deleteById(id);
    }

    public Integer mod(AwsUserDo awsUserDo) {
        awsUserDo.setUtime(new Date());
        return awsUserMapper.updateById(awsUserDo);
    }

    public List<AwsUserDo> all() {
        QueryWrapper<AwsUserDo> query = new QueryWrapper<>();
        return awsUserMapper.selectList(query);
    }

    public AwsUserDo getAwsUserById(Integer id) {
        return awsUserMapper.selectById(id);
    }

    public AwsUserDo getAwsUserByRemark(String remark) {
        QueryWrapper<AwsUserDo> query = new QueryWrapper<>();
        query.eq("remark", remark);
        return awsUserMapper.selectOne(query);
    }
}
