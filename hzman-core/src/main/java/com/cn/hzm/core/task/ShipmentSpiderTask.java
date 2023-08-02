package com.cn.hzm.core.task;

import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.repository.dao.AwsSpiderTaskDao;
import com.cn.hzm.core.repository.dao.FbaInboundDao;
import com.cn.hzm.core.repository.dao.FbaInboundItemDao;
import com.cn.hzm.core.repository.entity.AwsSpiderTaskDo;
import com.cn.hzm.core.repository.entity.FbaInboundDo;
import com.cn.hzm.core.repository.entity.FbaInboundItemDo;
import com.cn.hzm.core.spa.SpaManager;
import com.cn.hzm.core.spa.fbainbound.model.*;
import com.cn.hzm.core.util.ConvertUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.threeten.bp.OffsetDateTime;
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
public class ShipmentSpiderTask implements ITask {

    private SpaManager spaManager;

    private Integer spiderTaskId;

    private AwsSpiderTaskDao awsSpiderTaskDao;

    private FbaInboundDao fbaInboundDao;

    private FbaInboundItemDao fbaInboundItemDao;

    private ItemService itemService;

    private Semaphore shipmentSemaphore;

    private Semaphore shipmentItemSemaphore;

    private List<String> needSpiderShipmentStatus;

    private static final Integer DURATION_SECOND = 30 * 60 * 1000;


    private volatile Boolean spiderSwitch;

    private volatile boolean closeSwitch;

    public ShipmentSpiderTask(SpaManager spaManager, Integer spiderTaskId,
                              AwsSpiderTaskDao awsSpiderTaskDao, FbaInboundDao fbaInboundDao, FbaInboundItemDao fbaInboundItemDao,
                              ItemService itemService, boolean spiderSwitch) {
        this.spaManager = spaManager;
        this.spiderTaskId = spiderTaskId;
        this.awsSpiderTaskDao = awsSpiderTaskDao;
        this.fbaInboundDao = fbaInboundDao;
        this.fbaInboundItemDao = fbaInboundItemDao;
        this.itemService = itemService;
        this.spiderSwitch = spiderSwitch;
    }

    @Override
    public void setSpiderSwitch(boolean spiderSwitch) {
        this.spiderSwitch = spiderSwitch;
    }

    @Override
    public void start() {
        shipmentSemaphore = new Semaphore(30);
        shipmentItemSemaphore = new Semaphore(30);

        needSpiderShipmentStatus = Lists.newArrayList();
        needSpiderShipmentStatus.add(ShipmentStatus.WORKING.getValue());
        needSpiderShipmentStatus.add(ShipmentStatus.SHIPPED.getValue());
        needSpiderShipmentStatus.add(ShipmentStatus.IN_TRANSIT.getValue());
        needSpiderShipmentStatus.add(ShipmentStatus.DELIVERED.getValue());
        needSpiderShipmentStatus.add(ShipmentStatus.CHECKED_IN.getValue());
        needSpiderShipmentStatus.add(ShipmentStatus.RECEIVING.getValue());

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
        log.info("添加库存订单任务：{}", spiderTaskId);
        ExecutorService shipmentTask = Executors.newSingleThreadExecutor();
        shipmentTask.execute(this::shipmentSpider);

        //更新订单任务
        log.info("添加库存订单更新任务：{}", spiderTaskId);
        ExecutorService updateShipmentTask = Executors.newSingleThreadExecutor();
        updateShipmentTask.execute(this::shipmentUpdateSpider);
    }


    @Override
    public void close() {
        closeSwitch = true;
    }

    /**
     * shipMentId爬取能力
     */
    @Override
    public String spideData(List<String> shipmentIds) {
        long startTime = System.currentTimeMillis();
        List<FbaInboundDo> fbaInboundDos = fbaInboundDao.getAllRecordByShipmentIds(shipmentIds);
        if (!CollectionUtils.isEmpty(fbaInboundDos)) {
            return "货物单【" + shipmentIds + "】已入库";
        }

        GetShipmentsResponse r = spaManager.getShipmentsByShipmentIds(shipmentIds);
        if (r == null) {
            throw new HzmException(ExceptionCode.SHIPMENT_ID_FAIL_RETRY);
        }
        parseShipmentInfo(r.getPayload().getShipmentData());
        log.info("货物入库单任务结束，shipmentId：{} 耗时：{}", shipmentIds, System.currentTimeMillis() - startTime);
        return "amazon货物入库单爬取任务执行成功";
    }

    private void shipmentSpider() {
        while (true) {
            try {
                if (!spiderSwitch) {
                    log.info("货物单爬取任务已被停止，等待激活重试");
                    Thread.sleep(60 * 1000);
                    continue;
                }

                if (closeSwitch) {
                    log.info("货物单爬取任务已被关闭");
                    break;
                }

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
                if (!spiderSwitch) {
                    log.info("货物单爬取任务已被停止，等待激活重试");
                    Thread.sleep(60 * 1000);
                    continue;
                }

                if (closeSwitch) {
                    log.info("货物单爬取任务已被关闭");
                    break;
                }


                List<FbaInboundDo> shipments = fbaInboundDao.getAllRecordByShipmentStatus(needSpiderShipmentStatus);
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

    private void doUpdateShipment(List<FbaInboundDo> shipments) throws InterruptedException {
        log.info("货物入库单更新任务开始");

        int dbOrderNum = shipments.size();
        int limit = 10;
        int dealTimes = dbOrderNum / limit;
        long startTime = System.currentTimeMillis();

        for (int curNum = 0; curNum <= dealTimes; curNum++) {
            int start = curNum * limit;
            int end = Math.min(dbOrderNum, (curNum + 1) * limit);
            //如果相等，直接跳出循环
            if (start == end) {
                break;
            }

            log.info("处理更新货物单 start【{}】 end【{}】", start, end);
            List<FbaInboundDo> subShipments = shipments.subList(start, end);

            List<String> shipmentIds = subShipments.stream().map(FbaInboundDo::getShipmentId).collect(Collectors.toList());
            //获取资源
            shipmentSemaphore.acquire();
            GetShipmentsResponse r = spaManager.getShipmentsByShipmentIds(shipmentIds);
            if (r == null) {
                log.info("货物入库单更新任务任务异常结束，耗时：{}", System.currentTimeMillis() - startTime);
                return;
            }

            parseShipmentInfo(r.getPayload().getShipmentData());
            String nextToken = r.getPayload().getNextToken();
            while (!StringUtils.isEmpty(nextToken)) {
                //获取资源
                shipmentSemaphore.acquire();
                GetShipmentsResponse tokenResponse = spaManager.getShipmentsByNextToken(nextToken);
                nextToken = tokenResponse.getPayload().getNextToken();
                parseShipmentInfo(tokenResponse.getPayload().getShipmentData());
            }
        }

        log.info("货物入库单更新任务结束，耗时：{}", System.currentTimeMillis() - startTime);
    }

    private void doShipmentSpider() throws ParseException, InterruptedException {
        log.info("货物入库单爬取开始");
        long startTime = System.currentTimeMillis();
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);

        String strBeginDate = awsSpiderTaskDo.getSpiderDepend();
        Date beginDate1 = TimeUtil.transformUTCToDate(strBeginDate);
        OffsetDateTime beginDate = OffsetDateTime.parse(strBeginDate);
        OffsetDateTime endDate = beginDate.plusMinutes(30);
        String strEndDate = endDate.toInstant().toString();

        //如果是同一天的请求，endDate为空
        if (System.currentTimeMillis() - beginDate1.getTime() < DURATION_SECOND) {
            //如果拉取当天数据，每6分钟执行一次
            log.info("爬取货物入库单任务未满足时间条件，退出本次任务，休息30分钟");
            Thread.sleep(30 * 60 * 1000);
            return;
        }

        //获取资源
        shipmentSemaphore.acquire();
        GetShipmentsResponse r = spaManager.getShipmentsByDateRange(needSpiderShipmentStatus, beginDate, endDate);
        if (r == null) {
            log.info("货物入库单任务异常结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, endDate);
            return;
        }

        parseShipmentInfo(r.getPayload().getShipmentData());
        String nextToken = r.getPayload().getNextToken();
        while (!StringUtils.isEmpty(nextToken)) {
            //获取资源
            shipmentSemaphore.acquire();
            GetShipmentsResponse tokenResponse = spaManager.getShipmentsByNextToken(nextToken);
            nextToken = tokenResponse.getPayload().getNextToken();
            parseShipmentInfo(tokenResponse.getPayload().getShipmentData());
        }

        awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);
        awsSpiderTaskDo.setSpiderDepend(strEndDate);
        awsSpiderTaskDao.update(awsSpiderTaskDo);
        log.info("货物入库单任务结束，耗时：{}，爬取时间范围：{}--{}", System.currentTimeMillis() - startTime, strBeginDate, strEndDate);
    }

    private void parseShipmentInfo(InboundShipmentList inboundShipments) {
        //说明这个时间段内未发生货物入库
        if (CollectionUtils.isEmpty(inboundShipments)) {
            return;
        }

        inboundShipments.forEach(inboundShipmentInfo -> {
            FbaInboundDo fbaInboundDo = fbaInboundDao.getByShipmentId(inboundShipmentInfo.getShipmentId());
            if (fbaInboundDo != null) {
                ConvertUtil.convertToShipmentInfoDO(fbaInboundDo, inboundShipmentInfo);
                fbaInboundDao.updateRecord(fbaInboundDo);
            } else {
                fbaInboundDao.createRecord(ConvertUtil.convertToShipmentInfoDO(new FbaInboundDo(), inboundShipmentInfo));
            }
            //爬去货物单商品列表
            spiderShipmentItem(inboundShipmentInfo.getShipmentId());
        });
    }

    private void spiderShipmentItem(String shipmentId) {
        try {
            shipmentItemSemaphore.acquire();
            //新接口获取该shipmentId下所有商品
            GetShipmentItemsResponse r = spaManager.getShipmentItemsByShipmentId(shipmentId);
            parseShipmentItemInfo(r.getPayload().getItemData());
        } catch (Exception e) {
            log.error("货运单【{}】爬取失败：", shipmentId, e);
        }
    }

    private void parseShipmentItemInfo(InboundShipmentItemList itemMembers) {
        //说明这个时间段内未发生货物入库
        if (CollectionUtils.isEmpty(itemMembers)) {
            return;
        }

        Map<String, List<InboundShipmentItem>> members = itemMembers.stream().collect(Collectors.groupingBy(InboundShipmentItem::getShipmentId));

        //根据shipment分批处理减库存操作
        members.forEach((shipmentId, memberValues) -> {
            List<FbaInboundItemDo> records = fbaInboundItemDao.getAllRecordByShipmentId(shipmentId);
            Map<String, Integer> dataMap = Maps.newHashMap();
            records.forEach(itemRecord -> dataMap.put(itemRecord.getShipmentId() + itemRecord.getSellerSKU(), itemRecord.getId()));

            memberValues.forEach(member -> {
                FbaInboundItemDo fbaInboundItemDo = ConvertUtil.convertToShipmentItemDO(new FbaInboundItemDo(), member);
                if (dataMap.containsKey(member.getShipmentId() + member.getSellerSKU())) {
                    fbaInboundItemDo.setId(dataMap.get(member.getShipmentId() + member.getSellerSKU()));
                    fbaInboundItemDao.updateRecord(fbaInboundItemDo);
                } else {
                    fbaInboundItemDao.createRecord(fbaInboundItemDo);
                    //当前库存减去amazon入库
                    try {
                        itemService.dealSkuInventory(member.getSellerSKU(), spaManager.getAwsUserId(), spaManager.getMarketId(),
                                "mod", -member.getQuantityShipped());
                        itemService.processSync(member.getSellerSKU(), spaManager.getAwsUserId(), spaManager.getMarketId());
                    } catch (Exception e) {
                        log.error("FBA订单刷新库存失败，sku：{} 可能为新产品  ", member.getSellerSKU(), e);
                    }
                }
            });
        });
    }


}
