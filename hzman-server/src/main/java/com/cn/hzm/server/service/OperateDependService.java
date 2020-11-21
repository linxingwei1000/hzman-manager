package com.cn.hzm.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.OperateDependDO;
import com.cn.hzm.server.dao.OperateDependMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/18 10:43 上午
 */
@Component
public class OperateDependService {

    @Autowired
    private OperateDependMapper operateDependMapper;

    public String getValueByKey(String key) {
        QueryWrapper<OperateDependDO> query = new QueryWrapper();
        query.eq("operate_key", key);
        OperateDependDO operateDependDO = operateDependMapper.selectOne(query);
        if (operateDependDO == null) {
            return null;
        }
        return operateDependDO.getOperateValue();
    }

    public boolean updateValueByKey(String key, String value) {
        QueryWrapper<OperateDependDO> query = new QueryWrapper();
        query.eq("operate_key", key);

        OperateDependDO operateDependDO = new OperateDependDO();
        operateDependDO.setOperateValue(value);
        int result = operateDependMapper.update(operateDependDO, query);
        return result != 0;
    }
}
