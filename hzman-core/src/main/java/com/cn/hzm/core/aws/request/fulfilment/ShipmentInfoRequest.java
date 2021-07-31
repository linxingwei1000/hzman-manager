package com.cn.hzm.core.aws.request.fulfilment;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author linxingwei
 * @date 31.7.21 11:01 上午
 */
@Data
public class ShipmentInfoRequest extends FulfilmentRequest{

    private List<String> shipmentStatusList;

    private List<String> shipmentIds;

    @JSONField(name = "LastUpdatedAfter")
    private String lastUpdatedAfter;

    @JSONField(name = "LastUpdatedBefore")
    private String lastUpdatedBefore;

    @Override
    protected void privateJson(JSONObject jo) {
        String key = "ShipmentIdList.member.";
        int num = 1;
        if(!CollectionUtils.isEmpty(shipmentIds)){
            for (String shipmentId : shipmentIds) {
                jo.put(key + num, shipmentId);
                num++;
            }
        }

        key = "ShipmentStatusList.member.";
        num = 1;
        if(!CollectionUtils.isEmpty(shipmentStatusList)){
            for(String shipmentStatus: shipmentStatusList){
                jo.put(key + num, shipmentStatus);
                num++;
            }
        }
    }
}
