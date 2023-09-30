package com.cn.hzm.core.manager;

import com.cn.hzm.core.cache.SaleInfoCache;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.processor.DailyStatProcessor;
import com.cn.hzm.core.repository.dao.*;
import com.cn.hzm.core.repository.entity.AwsSpiderTaskDo;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.spa.SpaManager;
import com.cn.hzm.core.task.ITask;
import com.cn.hzm.core.task.OrderSpiderTask;
import com.cn.hzm.core.task.ShipmentSpiderTask;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 31.5.23 5:46 下午
 */
@Slf4j
@Component
public class TaskManager {

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private AwsSpiderTaskDao awsSpiderTaskDao;

    @Autowired
    private AmazonOrderDao amazonOrderDao;

    @Autowired
    private AmazonOrderItemDao amazonOrderItemDao;

    @Autowired
    private AmazonOrderFinanceDao amazonOrderFinanceDao;

    @Autowired
    private FbaInboundDao fbaInboundDao;

    @Autowired
    private FbaInboundItemDao fbaInboundItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SaleInfoCache saleInfoCache;

    @Autowired
    private AwsUserManager awsUserManager;

    @Autowired
    private DailyStatProcessor dailyStatProcessor;

    @Value("${cache.switch:false}")
    private Boolean spiderSwitch;

    /**
     * 亚马逊seller spi map<awsUserId,<MarketId, spaManager>>
     */
    private final Map<Integer, ITask> spiderTaskMap = Maps.newHashMap();

    @PostConstruct
    public void _init(){
        if (!spiderSwitch) {
            log.info("开发环境关闭爬取任务");
            return;
        }

        //创建爬取任务
        List<AwsSpiderTaskDo> awsUserMarketDos = awsSpiderTaskDao.getActiveSpiderTask();
        awsUserMarketDos.forEach(this::processAdd);
    }

    /**
     * 关闭爬取任务
     * @param spiderTaskId
     */
    public void pauseSpiderTask(Integer spiderTaskId){
        ITask task = spiderTaskMap.get(spiderTaskId);
        task.setSpiderSwitch(false);
    }

    /**
     * 开启爬取任务
     * @param spiderTaskId
     */
    public void openSpiderTask(Integer spiderTaskId){
        ITask task = spiderTaskMap.get(spiderTaskId);
        if(task == null){
            log.info("添加任务：{}", spiderTaskId);
            processAdd(awsSpiderTaskDao.select(spiderTaskId));
            return;
        }
        task.setSpiderSwitch(true);
    }

    /**
     * 删除爬取任务
     * @param spiderTaskId
     */
    public void deleteSpiderTask(Integer spiderTaskId){
        ITask task = spiderTaskMap.get(spiderTaskId);
        task.close();

        //再删除
        spiderTaskMap.remove(spiderTaskId);
    }

    /**
     * 执行指定数据爬取任务
     * @param userMarketId,spiderType
     * @return
     */
    public String execTaskByRelationIds(Integer userMarketId, Integer spiderType, List<String> relationIds){
        AwsSpiderTaskDo taskDo = awsSpiderTaskDao.getByUserMarketIdAndSpiderType(userMarketId, spiderType);
        return spiderTaskMap.get(taskDo.getId()).spideData(relationIds);
    }


    /**
     * 创建任务
     * @param spiderTaskDo
     */
    private void processAdd(AwsSpiderTaskDo spiderTaskDo){
        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(spiderTaskDo.getUserMarketId());
        SpaManager spaManager = awsUserManager.getManager(awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId());

        ITask task;
        if(spiderTaskDo.getSpiderType().equals(SpiderType.CREATE_ORDER.getCode())){
            task = new OrderSpiderTask(spaManager, spiderTaskDo.getId(), awsSpiderTaskDao, amazonOrderDao,
                    amazonOrderItemDao, amazonOrderFinanceDao, itemService, dailyStatProcessor, saleInfoCache, true);
            spiderTaskMap.put(spiderTaskDo.getId(), task);
            log.info("添加任务：{}", spiderTaskDo.getId());

        }else{
            task = new ShipmentSpiderTask(spaManager, spiderTaskDo.getId(), awsSpiderTaskDao, fbaInboundDao,
                    fbaInboundItemDao, itemService,true);
            spiderTaskMap.put(spiderTaskDo.getId(), task);
            log.info("添加任务：{}", spiderTaskDo.getId());
        }

        //开启爬取任务
        task.start();
    }
}
