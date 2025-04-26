package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.DealFileDo;
import com.cn.hzm.core.repository.mapper.DealFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 12.3.25 11:52 下午
 */
@Service
public class DealFileDao {

    @Autowired
    private DealFileMapper dealFileMapper;

    public DealFileDo selectByFileName(String fileName){
        QueryWrapper<DealFileDo> query = new QueryWrapper<>();
        query.eq("file_name", fileName);
        return dealFileMapper.selectOne(query);
    }


    public DealFileDo selectByFileId(Integer fileId){
        return dealFileMapper.selectById(fileId);
    }

    public List<DealFileDo> selectByCondition(Map<String, String> condition) {
        QueryWrapper<DealFileDo> query = new QueryWrapper<>();
        if (condition.size() != 0) {
        }
        query.orderByDesc("id");
        return dealFileMapper.selectList(query);
    }

    /**
     * 创建商品
     *
     * @param dealFileDo
     */
    public void create(DealFileDo dealFileDo) {
        dealFileDo.setUtime(new Date());
        dealFileDo.setCtime(new Date());
        dealFileMapper.insert(dealFileDo);
    }

    /**
     * 更新商品
     *
     * @param dealFileDo
     */
    public void update(DealFileDo dealFileDo) {
        dealFileDo.setUtime(new Date());
        dealFileMapper.updateById(dealFileDo);
    }
}
