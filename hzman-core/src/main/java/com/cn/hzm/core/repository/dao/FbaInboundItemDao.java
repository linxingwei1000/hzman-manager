package com.cn.hzm.core.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.repository.entity.FbaInboundItemDo;
import com.cn.hzm.core.repository.mapper.FbaInboundItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:10 下午
 */
@Service
public class FbaInboundItemDao {

    @Autowired
    private FbaInboundItemMapper fbaInboundItemMapper;

    /**
     * 创建amazon入库记录
     * @param fbaInboundItemDo
     */
    public void createRecord(FbaInboundItemDo fbaInboundItemDo){
        fbaInboundItemDo.setUtime(new Date());
        fbaInboundItemDo.setCtime(new Date());
        fbaInboundItemMapper.insert(fbaInboundItemDo);
    }

    /**
     * 创建amazon入库记录
     * @param shipmentItemDO
     */
    public void updateRecord(FbaInboundItemDo shipmentItemDO){
        shipmentItemDO.setUtime(new Date());
        fbaInboundItemMapper.updateById(shipmentItemDO);
    }

    public List<FbaInboundItemDo> getAllRecordBySku(String sku){
        QueryWrapper<FbaInboundItemDo> query = new QueryWrapper<>();
        query.eq("seller_sku", sku);
        query.select("shipment_id", "quantity_shipped", "quantity_received");
        return fbaInboundItemMapper.selectList(query);
    }

    public List<FbaInboundItemDo> getAllRecordByShipmentId(String shipmentId){
        QueryWrapper<FbaInboundItemDo> query = new QueryWrapper<>();
        query.eq("shipment_id", shipmentId);
        query.select("id", "shipment_id", "seller_sku");
        return fbaInboundItemMapper.selectList(query);
    }
}
