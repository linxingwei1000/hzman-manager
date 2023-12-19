package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FbaInboundDo;
import com.cn.hzm.core.repository.mapper.FbaInboundMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:10 下午
 */
@Service
public class FbaInboundDao {

    @Autowired
    private FbaInboundMapper fbaInboundMapper;

    /**
     * 创建amazon入库记录
     * @param fbaInboundDo
     */
    public void createRecord(FbaInboundDo fbaInboundDo){
        fbaInboundDo.setUtime(new Date());
        fbaInboundDo.setCtime(new Date());
        fbaInboundMapper.insert(fbaInboundDo);
    }

    /**
     * 更新库存
     * @param fbaInboundDo
     */
    public void updateRecord(FbaInboundDo fbaInboundDo){
        fbaInboundDo.setUtime(new Date());
        fbaInboundMapper.updateById(fbaInboundDo);
    }



    public FbaInboundDo getByShipmentId(Integer userMarketId, String shipmentId){
        QueryWrapper<FbaInboundDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.eq("shipment_id", shipmentId);
        query.select("id", "shipment_id", "shipment_status");
        return fbaInboundMapper.selectOne(query);
    }

    public List<FbaInboundDo> getAllRecordByShipmentIds(Integer userMarketId, List<String> shipmentIds){
        QueryWrapper<FbaInboundDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.in("shipment_id", shipmentIds);
        query.select("id", "shipment_id", "shipment_status");
        return fbaInboundMapper.selectList(query);
    }

    public List<FbaInboundDo> getAllRecordByShipmentStatus(Integer userMarketId, List<String> status){
        QueryWrapper<FbaInboundDo> query = new QueryWrapper<>();
        query.eq("user_market_id", userMarketId);
        query.in("shipment_status", status);
        query.select("shipment_id");
        return fbaInboundMapper.selectList(query);
    }
}
