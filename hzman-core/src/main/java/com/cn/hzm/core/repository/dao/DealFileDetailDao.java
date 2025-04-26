package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.DealFileDetailDo;
import com.cn.hzm.core.repository.entity.DealFileDo;
import com.cn.hzm.core.repository.mapper.DealFileDetailMapper;
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
public class DealFileDetailDao {

    @Autowired
    private DealFileDetailMapper dealFileDetailMapper;

    public List<DealFileDetailDo> selectByDealFileId(Integer dealFileId) {
        QueryWrapper<DealFileDetailDo> query = new QueryWrapper<>();
        query.eq("file_id", dealFileId);
        return dealFileDetailMapper.selectList(query);
    }

    /**
     * 创建商品
     *
     * @param dealFileDetailDo
     */
    public void create(DealFileDetailDo dealFileDetailDo) {
        dealFileDetailDo.setUtime(new Date());
        dealFileDetailDo.setCtime(new Date());
        dealFileDetailMapper.insert(dealFileDetailDo);
    }

    /**
     * 更新商品
     *
     * @param dealFileDetailDo
     */
    public void update(DealFileDetailDo dealFileDetailDo) {
        dealFileDetailDo.setUtime(new Date());
        dealFileDetailMapper.updateById(dealFileDetailDo);
    }
}
