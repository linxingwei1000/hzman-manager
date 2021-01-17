package com.cn.hzm.server.task;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.fulfilment.Member;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentItemsByNextTokenResponse;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentItemsResponse;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ShipmentItemRecordDO;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmanException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.service.OperateDependService;
import com.cn.hzm.stock.service.ShipmentItemRecordService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/14 11:20 上午
 */
@Slf4j
@Component
public class ShipmentSpiderTask {

    @Autowired
    private OperateDependService operateDependService;

    @Autowired
    private ShipmentItemRecordService shipmentItemRecordService;

    @Autowired
    private ItemDealService itemDealService;

    @Autowired
    private AwsClient awsClient;

    private Semaphore shipmentSemaphore;

    private static final Integer DURATION_SECOND = 10 * 60 * 1000;


    /**
     * 线程任务：无限爬取远端入库订单信息
     */
    @PostConstruct
    public void initTask() {
        shipmentSemaphore = new Semaphore(30);

        //订单商品爬取资源定时充能
        ScheduledThreadPoolExecutor scheduledTask = new ScheduledThreadPoolExecutor(1);
        scheduledTask.scheduleAtFixedRate(() -> {
            if (shipmentSemaphore.availablePermits() < 30) {
                shipmentSemaphore.release(1);
            }
        }, 60, 2, TimeUnit.SECONDS);


//        //爬取订单任务
//        ExecutorService shipmentTask = Executors.newSingleThreadExecutor();
//        shipmentTask.execute(this::shipmentSpider);
    }

    private void shipmentSpider() {
        while (true) {
            try {
                doShipmentSpider();
            } catch (HzmanException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("爬虫任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doShipmentSpider() throws ParseException, InterruptedException {
        log.info("货物入库单爬取开始");
        long startTime = System.currentTimeMillis();
        String strBeginDate = operateDependService.getValueByKey(ContextConst.OPERATE_SHIPMENT_INFO);

        Date beginDate = TimeUtil.transformUTCToDate(strBeginDate);
        Date endDate = TimeUtil.dateFixByDay(beginDate, 0, 0, 10);
        String strEndDate = TimeUtil.dateToUTC(endDate);

        //如果是同一天的请求，endDate为空
        if (System.currentTimeMillis() - beginDate.getTime() < DURATION_SECOND) {
            //如果拉取当天数据，每6分钟执行一次
            log.info("爬取货物入库单任务未满足时间条件，退出本次任务，休息6分钟");
            Thread.sleep(6 * 60 * 1000);
            return;
        }

        //获取资源
        shipmentSemaphore.acquire();
        ListInboundShipmentItemsResponse r = awsClient.getShipmentItems(null, strBeginDate, strEndDate);
        if (r == null) {
            log.info("货物入库单任务异常结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate);
            return;
        }

        parseShipmentInfo(r.getListInboundShipmentItemsResult().getItemData().getList());
        String nextToken = r.getListInboundShipmentItemsResult().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            shipmentSemaphore.acquire();
            ListInboundShipmentItemsByNextTokenResponse tokenResponse = awsClient.getShipmentItemsByNextToken(nextToken);
            nextToken = tokenResponse.getListInboundShipmentItemsByNextTokenResult().getNextToken();

            parseShipmentInfo(tokenResponse.getListInboundShipmentItemsByNextTokenResult().getItemData().getList());
        }

        operateDependService.updateValueByKey(ContextConst.OPERATE_SHIPMENT_INFO, strEndDate);
        log.info("货物入库单任务结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate);
    }

    private void parseShipmentInfo(List<Member> itemMembers) {
        //说明这个时间段内未发生货物入库
        if (CollectionUtils.isEmpty(itemMembers)) {
            return;
        }

        List<ShipmentItemRecordDO> records = shipmentItemRecordService.getAllRecord();
        Set<String> dealSet = Sets.newHashSet();
        records.forEach(itemRecord -> dealSet.add(itemRecord.getShipmentId() + itemRecord.getSellerSKU()));

        itemMembers.forEach(member -> {
            if (dealSet.contains(member.getShipmentId() + member.getSellerSKU())) {
                return;
            }

            ShipmentItemRecordDO shipmentItemDO = new ShipmentItemRecordDO();
            shipmentItemDO.setQuantityShipped(member.getQuantityShipped());
            shipmentItemDO.setShipmentId(member.getShipmentId());
            shipmentItemDO.setPrepDetailsList(JSONObject.toJSONString(member.getPrepDetailsList()));
            shipmentItemDO.setFulfillmentNetworkSKU(member.getFulfillmentNetworkSKU());
            shipmentItemDO.setSellerSKU(member.getSellerSKU());
            shipmentItemDO.setQuantityReceived(member.getQuantityReceived());
            shipmentItemDO.setQuantityInCase(member.getQuantityInCase());
            shipmentItemRecordService.createRecord(shipmentItemDO);

            //当前库存减去amazon入库
            itemDealService.dealSkuInventory(member.getSellerSKU(), "mod", -member.getQuantityReceived());
        });

    }
}
