package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AwsUserDo;
import com.cn.hzm.core.repository.entity.ItemRemarkDo;
import com.cn.hzm.core.repository.mapper.ItemRemarkMapper;
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
public class ItemRemarkDao {

    @Autowired
    private ItemRemarkMapper itemRemarkMapper;

    public ItemRemarkDo selectById(Integer id){
        return itemRemarkMapper.selectById(id);
    }

    public List<ItemRemarkDo> selectByItemId(Integer itemId){
        QueryWrapper<ItemRemarkDo> query = new QueryWrapper<>();
        query.eq("item_id", itemId);
        return itemRemarkMapper.selectList(query);
    }


    public Integer insert(ItemRemarkDo itemRemarkDo){
        itemRemarkDo.setUtime(new Date());
        itemRemarkDo.setCtime(new Date());
        return itemRemarkMapper.insert(itemRemarkDo);
    }

    public Integer update(ItemRemarkDo itemRemarkDo){
        itemRemarkDo.setUtime(new Date());
        return itemRemarkMapper.updateById(itemRemarkDo);
    }

    public Integer delete(Integer id){
        return itemRemarkMapper.deleteById(id);
    }

    public Integer mod(ItemRemarkDo itemRemarkDo){
        itemRemarkDo.setUtime(new Date());
        return itemRemarkMapper.updateById(itemRemarkDo);
    }
}
