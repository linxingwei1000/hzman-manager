package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.AsinItemDo;
import com.cn.hzm.core.repository.mapper.AsinItemMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author linxingwei
 * @date 12.3.25 11:52 下午
 */
@Service
public class AsinItemDao {

    @Autowired
    private AsinItemMapper asinItemMapper;

    public List<AsinItemDo> selectAll(String[] fields) {
        QueryWrapper<AsinItemDo> query = new QueryWrapper<>();
        query.select(fields);
        return asinItemMapper.selectList(query);
    }

    public AsinItemDo selectById(Integer id) {
        QueryWrapper<AsinItemDo> query = new QueryWrapper<>();
        query.eq("id", id);
        return asinItemMapper.selectOne(query);
    }

    public List<AsinItemDo> selectByIds(List<Integer> ids) {
        QueryWrapper<AsinItemDo> query = new QueryWrapper<>();
        query.in("id", ids);
        return asinItemMapper.selectList(query);
    }

    public List<AsinItemDo> getByAsins(Set<String> asins, String[] fields) {
        QueryWrapper<AsinItemDo> query = new QueryWrapper<>();
        query.in("asin", asins);
        query.eq("active", 1);
        query.select(fields);
        return asinItemMapper.selectList(query);
    }

    public AsinItemDo getByAsin(String asin) {
        QueryWrapper<AsinItemDo> query = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(asin)) {
            query.eq("asin", asin);
        }
        query.eq("active", 1);
        return asinItemMapper.selectOne(query);
    }

    /**
     * 创建商品
     *
     * @param asinItemDo
     */
    public void createItem(AsinItemDo asinItemDo) {
        asinItemDo.setUtime(new Date());
        asinItemDo.setCtime(new Date());
        asinItemMapper.insert(asinItemDo);
    }

    /**
     * 更新商品
     *
     * @param asinItemDo
     */
    public void updateItem(AsinItemDo asinItemDo) {
        asinItemDo.setUtime(new Date());
        asinItemMapper.updateById(asinItemDo);
    }
}
