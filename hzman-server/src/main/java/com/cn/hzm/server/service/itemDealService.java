package com.cn.hzm.server.service;

import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.resp.product.GetMatchingProductForIdResponse;
import com.cn.hzm.server.dto.ItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 5:55 下午
 */
@Service
public class itemDealService {

    @Autowired
    private AwsClient awsClient;

    public void processItemCreate(ItemDTO item){

        GetMatchingProductForIdResponse resp = awsClient.getProductInfoByAsin(item.getAsin());

        //asin取aws数据：商品信息，商品库存，销量


    }
}
