package com.cn.hzm.server.task;

import com.cn.hzm.core.aws.AwsClient;
import com.cn.hzm.core.aws.domain.fulfilment.Member;
import com.cn.hzm.core.aws.domain.fulfilment.ShipmentMember;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentItemsByNextTokenResponse;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentItemsResponse;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentsByNextTokenResponse;
import com.cn.hzm.core.aws.resp.fulfilment.ListInboundShipmentsResponse;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.entity.ShipmentInfoRecordDO;
import com.cn.hzm.core.entity.ShipmentItemRecordDO;
import com.cn.hzm.core.enums.AmazonShipmentStatusEnum;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.server.service.ItemDealService;
import com.cn.hzm.server.service.OperateDependService;
import com.cn.hzm.server.util.ConvertUtil;
import com.cn.hzm.stock.service.ShipmentInfoRecordService;
import com.cn.hzm.stock.service.ShipmentItemRecordService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    private ShipmentInfoRecordService shipmentInfoRecordService;

    @Autowired
    private ShipmentItemRecordService shipmentItemRecordService;

    @Autowired
    private ItemDealService itemDealService;

    @Autowired
    private AwsClient awsClient;

    private Semaphore shipmentSemaphore;

    private Semaphore shipmentItemSemaphore;

    private List<String> needSpiderShipmentStatus;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;


    @Value("${spider.switch:false}")
    private Boolean spiderSwitch;

    /**
     * 收到amazon入库单爬取任务
     *
     * @param shipmentId
     */
    public String shipmentSpiderTask(String shipmentId) {
        long startTime = System.currentTimeMillis();
        ShipmentInfoRecordDO shipmentInfoRecordDO = shipmentInfoRecordService.getByShipmentId(shipmentId);
        if(shipmentInfoRecordDO!=null){
            return "货物单【"+ shipmentId +"】已入库";
        }

        ListInboundShipmentsResponse r = awsClient.getShipmentInfo(null, Lists.newArrayList(shipmentId), null, null);
        if (r == null) {
            throw new HzmException(ExceptionCode.SHIPMENT_ID_FAIL_RETRY);
        }
        parseShipmentInfo(r.getListInboundShipmentsResult().getShipmentData().getList());
        String nextToken = r.getListInboundShipmentsResult().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            ListInboundShipmentsByNextTokenResponse tokenResponse = awsClient.getShipmentInfoNextToken(nextToken);
            nextToken = tokenResponse.getListInboundShipmentsByNextTokenResult().getNextToken();

            parseShipmentInfo(tokenResponse.getListInboundShipmentsByNextTokenResult().getShipmentData().getList());
        }
        log.info("货物入库单任务结束，shipmentId：{} 耗时：{}", shipmentId, System.currentTimeMillis() - startTime);
        return "amazon货物入库单爬取任务执行成功";
    }

    /**
     * 线程任务：无限爬取远端入库订单信息
     * 关闭任务
     */
    @PostConstruct
    public void initTask() {
        if (!spiderSwitch) {
            log.info("测试环境关闭爬虫任务");
            return;
        }

        shipmentSemaphore = new Semaphore(30);
        shipmentItemSemaphore = new Semaphore(30);

        needSpiderShipmentStatus = Lists.newArrayList();
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_WORKING.getCode());
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_SHIPPED.getCode());
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_IN_TRANSIT.getCode());
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_DELIVERED.getCode());
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_CHECKED_IN.getCode());
        needSpiderShipmentStatus.add(AmazonShipmentStatusEnum.STATUS_RECEIVING.getCode());

        //订单信息爬取资源定时充能
        ScheduledThreadPoolExecutor infoScheduledTask = new ScheduledThreadPoolExecutor(1);
        infoScheduledTask.scheduleAtFixedRate(() -> {
            if (shipmentSemaphore.availablePermits() < 30) {
                shipmentSemaphore.release(1);
            }
        }, 60, 2, TimeUnit.SECONDS);

        //订单商品爬取资源定时充能
        ScheduledThreadPoolExecutor scheduledTask = new ScheduledThreadPoolExecutor(1);
        scheduledTask.scheduleAtFixedRate(() -> {
            if (shipmentItemSemaphore.availablePermits() < 30) {
                shipmentItemSemaphore.release(1);
            }
        }, 60, 2, TimeUnit.SECONDS);

        //爬取订单任务
        ExecutorService shipmentTask = Executors.newSingleThreadExecutor();
        shipmentTask.execute(this::shipmentSpider);

        //更新订单任务
        ExecutorService updateShipmentTask = Executors.newSingleThreadExecutor();
        updateShipmentTask.execute(this::shipmentUpdateSpider);
    }

    private void shipmentSpider() {
        while (true) {
            try {
                doShipmentSpider();
            } catch (HzmException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("爬虫任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void shipmentUpdateSpider() {
        while (true) {
            try {

                List<ShipmentInfoRecordDO> shipments = shipmentInfoRecordService.getAllRecordByShipmentStatus(needSpiderShipmentStatus);
                log.info("货物入库单待更新总数量：{}", shipments.size());
                if (!CollectionUtils.isEmpty(shipments)) {
                    doUpdateShipment(shipments);
                }
                Thread.sleep(30 * 60 * 1000);
            } catch (HzmException e) {
                if (e.getExceptionCode().equals(ExceptionCode.REQUEST_LIMIT)) {
                    log.error("订单状态更新任务触发限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doUpdateShipment(List<ShipmentInfoRecordDO> shipments) throws InterruptedException {
        log.info("货物入库单更新任务开始");

        int dbOrderNum = shipments.size();
        int limit = 10;
        int dealTimes = dbOrderNum / limit;
        long startTime = System.currentTimeMillis();

        for (int curNum = 0; curNum <= dealTimes; curNum++) {
            int start = curNum * limit;
            int end = Math.min(dbOrderNum, (curNum + 1) * limit);

            log.info("处理更新货物单 start【{}】 end【{}】", start, end);
            List<ShipmentInfoRecordDO> subShipments = shipments.subList(start, end);

            List<String> shipmentIds = subShipments.stream().map(ShipmentInfoRecordDO::getShipmentId).collect(Collectors.toList());
            //获取资源
            shipmentSemaphore.acquire();
            ListInboundShipmentsResponse r = awsClient.getShipmentInfo(null, shipmentIds, null, null);
            if (r == null) {
                log.info("货物入库单更新任务任务异常结束，耗时：{}", System.currentTimeMillis() - startTime);
                return;
            }

            parseShipmentInfo(r.getListInboundShipmentsResult().getShipmentData().getList());
            String nextToken = r.getListInboundShipmentsResult().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                //获取资源
                shipmentSemaphore.acquire();
                ListInboundShipmentsByNextTokenResponse tokenResponse = awsClient.getShipmentInfoNextToken(nextToken);
                nextToken = tokenResponse.getListInboundShipmentsByNextTokenResult().getNextToken();
                parseShipmentInfo(tokenResponse.getListInboundShipmentsByNextTokenResult().getShipmentData().getList());
            }
        }

        log.info("货物入库单更新任务结束，耗时：{}", System.currentTimeMillis() - startTime);

    }

    private void doShipmentSpider() throws ParseException, InterruptedException {
        log.info("货物入库单爬取开始");
        long startTime = System.currentTimeMillis();
        String strBeginDate = operateDependService.getValueByKey(ContextConst.OPERATE_SHIPMENT_INFO);

        Date beginDate = TimeUtil.transformUTCToDate(strBeginDate);
        Date endDate = TimeUtil.dateFixByDay(beginDate, 0, 0, 40);
        String strEndDate = TimeUtil.dateToUTC(endDate);

        //如果是同一天的请求，endDate为空
        if (System.currentTimeMillis() - beginDate.getTime() < DURATION_SECOND) {
            //如果拉取当天数据，每6分钟执行一次
            log.info("爬取货物入库单任务未满足时间条件，退出本次任务，休息30分钟");
            Thread.sleep(30 * 60 * 1000);
            return;
        }

        //获取资源
        shipmentSemaphore.acquire();
        ListInboundShipmentsResponse r = awsClient.getShipmentInfo(needSpiderShipmentStatus, null, strBeginDate, strEndDate);
        if (r == null) {
            log.info("货物入库单任务异常结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate);
            return;
        }

        parseShipmentInfo(r.getListInboundShipmentsResult().getShipmentData().getList());
        String nextToken = r.getListInboundShipmentsResult().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            shipmentSemaphore.acquire();
            ListInboundShipmentsByNextTokenResponse tokenResponse = awsClient.getShipmentInfoNextToken(nextToken);
            nextToken = tokenResponse.getListInboundShipmentsByNextTokenResult().getNextToken();
            parseShipmentInfo(tokenResponse.getListInboundShipmentsByNextTokenResult().getShipmentData().getList());
        }

        operateDependService.updateValueByKey(ContextConst.OPERATE_SHIPMENT_INFO, strEndDate);
        log.info("货物入库单任务结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate);
    }

    private void parseShipmentInfo(List<ShipmentMember> shipmentMembers) {
        //说明这个时间段内未发生货物入库
        if (CollectionUtils.isEmpty(shipmentMembers)) {
            return;
        }

        shipmentMembers.forEach(shipmentMember -> {
            ShipmentInfoRecordDO shipmentInfo = shipmentInfoRecordService.getByShipmentId(shipmentMember.getShipmentId());
            if (shipmentInfo != null) {
                ConvertUtil.convertToShipmentInfoDO(shipmentInfo, shipmentMember);
                shipmentInfoRecordService.updateRecord(shipmentInfo);
            }else{
                shipmentInfoRecordService.createRecord(ConvertUtil.convertToShipmentInfoDO(new ShipmentInfoRecordDO(), shipmentMember));
            }
            //爬去货物单商品列表
            spiderShipmentItem(shipmentMember.getShipmentId());
        });
    }

    private void spiderShipmentItem(String shipmentId) {
        try {
            shipmentItemSemaphore.acquire();
            ListInboundShipmentItemsResponse r = awsClient.getShipmentItems(shipmentId, null, null);
            parseShipmentItemInfo(r.getListInboundShipmentItemsResult().getItemData().getList());
            String nextToken = r.getListInboundShipmentItemsResult().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                //获取资源
                shipmentItemSemaphore.acquire();
                ListInboundShipmentItemsByNextTokenResponse tokenResponse = awsClient.getShipmentItemsByNextToken(nextToken);
                nextToken = tokenResponse.getListInboundShipmentItemsByNextTokenResult().getNextToken();

                parseShipmentItemInfo(tokenResponse.getListInboundShipmentItemsByNextTokenResult().getItemData().getList());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void parseShipmentItemInfo(List<Member> itemMembers) {
        //说明这个时间段内未发生货物入库
        if (CollectionUtils.isEmpty(itemMembers)) {
            return;
        }

        Map<String, List<Member>> members = itemMembers.stream().collect(Collectors.groupingBy(Member::getShipmentId));

        //根据shipment分批处理减库存操作
        members.forEach((shipmentId, memberValues) -> {
            List<ShipmentItemRecordDO> records = shipmentItemRecordService.getAllRecordByShipmentId(shipmentId);
            Map<String, Integer> dataMap = Maps.newHashMap();
            records.forEach(itemRecord -> dataMap.put(itemRecord.getShipmentId() + itemRecord.getSellerSKU(), itemRecord.getId()));

            memberValues.forEach(member -> {
                ShipmentItemRecordDO shipmentItemDO = ConvertUtil.convertToShipmentItemDO(new ShipmentItemRecordDO(), member);
                if (dataMap.containsKey(member.getShipmentId() + member.getSellerSKU())) {
                    shipmentItemDO.setId(dataMap.get(member.getShipmentId() + member.getSellerSKU()));
                    shipmentItemRecordService.updateRecord(shipmentItemDO);
                }else{
                    shipmentItemRecordService.createRecord(shipmentItemDO);
                    //当前库存减去amazon入库
                    itemDealService.dealSkuInventory(member.getSellerSKU(), "mod", -member.getQuantityShipped());
                }
            });
        });
    }
}
