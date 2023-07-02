package com.cn.hzm.core.manager;

import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.repository.dao.AwsSpiderTaskDao;
import com.cn.hzm.core.repository.dao.AwsUserDao;
import com.cn.hzm.core.repository.dao.AwsUserMarketDao;
import com.cn.hzm.core.repository.entity.AwsUserDo;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.spa.SpaManager;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 30.5.23 6:23 下午
 */
@Component
public class AwsUserManager {

    @Autowired
    private AwsUserDao awsUserDao;

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private AwsSpiderTaskDao awsSpiderTaskDao;

    /**
     * 亚马逊seller spi map<awsUserId,<MarketId, spaManager>>
     */
    private final Map<Integer, Map<String, SpaManager>> awsUserSpaMap = Maps.newHashMap();


    @PostConstruct
    public void _init(){
        List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.all();
        awsUserMarketDos.forEach(this::processAdd);
    }

    /**
     * 获取spaManager
     * @param awsUserId
     * @param marketId
     * @return
     */
    public SpaManager getManager(Integer awsUserId, String marketId){
        return awsUserSpaMap.get(awsUserId).get(marketId);
    }

    /**
     * 账号关联市场，添加spaManager
     * @param awsUserMarketId
     */
    public void addManager(Integer awsUserMarketId){
        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(awsUserMarketId);
        processAdd(awsUserMarketDo);


    }


    /**
     * 账号取消关联市场，删除spaManager
     * @param awsUserId
     * @param marketId
     */
    public void deleteManager(Integer awsUserId, String marketId){
        if(awsUserSpaMap.containsKey(awsUserId)){
            Map<String, SpaManager> marketSpaManager = awsUserSpaMap.get(awsUserId);
            marketSpaManager.remove(marketId);
        }
    }

    /**
     * 账号修改，同步账号信息
     * @param awsUserMarketDos
     */
    public void syncAwsInfo(List<AwsUserMarketDo> awsUserMarketDos){
        awsUserMarketDos.forEach(this::processAdd);
    }

    private void processAdd(AwsUserMarketDo awsUserMarketDo){
        AwsMarket awsMarket = AwsMarket.getByMarketId(awsUserMarketDo.getMarketId());
        AwsUserDo awsUserDo = awsUserDao.getAwsUserById(awsUserMarketDo.getAwsUserId());

        SpaManager spaManager = new SpaManager(awsUserDo, awsMarket, awsUserMarketDo);
        Map<String, SpaManager> marketSpaManager = awsUserSpaMap.getOrDefault(awsUserDo.getId(), Maps.newHashMap());
        marketSpaManager.put(awsMarket.getId(), spaManager);
        awsUserSpaMap.put(awsUserDo.getId(), marketSpaManager);
    }
}
