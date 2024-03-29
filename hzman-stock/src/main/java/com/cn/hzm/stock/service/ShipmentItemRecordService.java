package com.cn.hzm.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.hzm.core.entity.ShipmentItemRecordDO;
import com.cn.hzm.stock.dao.ShipmentItemRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 2:10 下午
 */
@Service
public class ShipmentItemRecordService {

    @Autowired
    private ShipmentItemRecordMapper shipmentItemRecordMapper;

    /**
     * 创建amazon入库记录
     * @param shipmentItemDO
     */
    public void createRecord(ShipmentItemRecordDO shipmentItemDO){
        shipmentItemDO.setUtime(new Date());
        shipmentItemDO.setCtime(new Date());
        shipmentItemRecordMapper.insert(shipmentItemDO);
    }

    /**
     * 创建amazon入库记录
     * @param shipmentItemDO
     */
    public void updateRecord(ShipmentItemRecordDO shipmentItemDO){
        shipmentItemDO.setUtime(new Date());
        shipmentItemRecordMapper.updateById(shipmentItemDO);
    }

    public List<ShipmentItemRecordDO> getAllRecordBySku(String sku){
        QueryWrapper<ShipmentItemRecordDO> query = new QueryWrapper<>();
        query.eq("seller_sku", sku);
        query.select("shipment_id", "quantity_shipped", "quantity_received");
        return shipmentItemRecordMapper.selectList(query);
    }

    public List<ShipmentItemRecordDO> getAllRecordByShipmentId(String shipmentId){
        QueryWrapper<ShipmentItemRecordDO> query = new QueryWrapper<>();
        query.eq("shipment_id", shipmentId);
        query.select("id", "shipment_id", "seller_sku");
        return shipmentItemRecordMapper.selectList(query);
    }
}
