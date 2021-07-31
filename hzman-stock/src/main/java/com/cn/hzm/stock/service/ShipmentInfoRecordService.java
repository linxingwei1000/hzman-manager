package com.cn.hzm.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.ShipmentInfoRecordDO;
import com.cn.hzm.stock.dao.ShipmentInfoRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:10 下午
 */
@Service
public class ShipmentInfoRecordService {

    @Autowired
    private ShipmentInfoRecordMapper shipmentInfoRecordMapper;

    /**
     * 创建amazon入库记录
     * @param shipmentInfoRecordDO
     */
    public void createRecord(ShipmentInfoRecordDO shipmentInfoRecordDO){
        shipmentInfoRecordDO.setUtime(new Date());
        shipmentInfoRecordDO.setCtime(new Date());
        shipmentInfoRecordMapper.insert(shipmentInfoRecordDO);
    }

    /**
     * 更新库存
     * @param shipmentInfoRecordDO
     */
    public void updateRecord(ShipmentInfoRecordDO shipmentInfoRecordDO){
        shipmentInfoRecordDO.setUtime(new Date());
        shipmentInfoRecordMapper.updateById(shipmentInfoRecordDO);
    }



    public ShipmentInfoRecordDO getByShipmentId(String shipmentId){
        QueryWrapper<ShipmentInfoRecordDO> query = new QueryWrapper<>();
        query.eq("shipment_id", shipmentId);
        query.select("id", "shipment_id");
        return shipmentInfoRecordMapper.selectOne(query);
    }

    public List<ShipmentInfoRecordDO> getAllRecordByShipmentIds(List<String> shipmentIds){
        QueryWrapper<ShipmentInfoRecordDO> query = new QueryWrapper<>();
        query.in("shipment_id", shipmentIds);
        query.select("shipment_id", "shipment_status");
        return shipmentInfoRecordMapper.selectList(query);
    }

    public List<ShipmentInfoRecordDO> getAllRecordByShipmentStatus(List<String> status){
        QueryWrapper<ShipmentInfoRecordDO> query = new QueryWrapper<>();
        query.in("shipment_status", status);
        query.select("shipment_id");
        return shipmentInfoRecordMapper.selectList(query);
    }
}
