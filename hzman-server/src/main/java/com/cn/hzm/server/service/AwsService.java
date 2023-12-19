package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.AwsSpiderTaskDto;
import com.cn.hzm.api.dto.AwsUserDto;
import com.cn.hzm.api.dto.AwsUserMarketDto;
import com.cn.hzm.api.dto.AwsUserSearchDto;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.manager.TaskManager;
import com.cn.hzm.core.repository.dao.AwsSpiderTaskDao;
import com.cn.hzm.core.repository.dao.AwsUserDao;
import com.cn.hzm.core.repository.dao.AwsUserMarketDao;
import com.cn.hzm.core.repository.entity.AwsSpiderTaskDo;
import com.cn.hzm.core.repository.entity.AwsUserDo;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.manager.AwsUserManager;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linxingwei
 * @date 13.4.23 6:34 下午
 */
@Component
public class AwsService {

    @Autowired
    private AwsUserDao awsUserDao;

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    @Autowired
    private AwsSpiderTaskDao awsSpiderTaskDao;

    @Autowired
    private AwsUserManager awsUserManager;

    @Autowired
    private TaskManager taskManager;

    public List<AwsUserDto> getAwsUserList(AwsUserSearchDto searchDto) {
        List<AwsUserDo> userDos = awsUserDao.selectByCondition(searchDto.getRemark());
        if (CollectionUtils.isEmpty(userDos)) {
            return Lists.newArrayList();
        }

        if (StringUtils.isNotEmpty(searchDto.getMarketId())) {
            List<AwsUserMarketDo> list = awsUserMarketDao.getByMarketId(searchDto.getMarketId());
            if (!CollectionUtils.isEmpty(list)) {
                List<Integer> userIds = list.stream().map(AwsUserMarketDo::getAwsUserId).collect(Collectors.toList());
                userDos = userDos.stream().filter(awsUserDo -> userIds.contains(awsUserDo.getId())).collect(Collectors.toList());
            }
        }

        return userDos.stream().map(awsUserDo -> {
            AwsUserDto awsUserDto = new AwsUserDto();
            BeanUtils.copyProperties(awsUserDo, awsUserDto);
            List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.getByUserId(awsUserDo.getId());
            awsUserDto.setMarketDtos(
                    awsUserMarketDos.stream().map(awsUserMarketDo -> {
                        AwsUserMarketDto awsUserMarketDto = new AwsUserMarketDto();
                        BeanUtils.copyProperties(awsUserMarketDo, awsUserMarketDto);

                        AwsMarket awsMarket = AwsMarket.getByMarketId(awsUserMarketDo.getMarketId());
                        awsUserMarketDto.setMarketCountry(awsMarket.getDesc());
                        awsUserMarketDto.setRegion(ContextConst.REGION_MAP.get(awsMarket.getRegion()));

                        List<AwsSpiderTaskDo> spiderTaskDos = awsSpiderTaskDao.getByUserMarketId(awsUserMarketDo.getId());
                        awsUserMarketDto.setAwsSpiderTaskDtos(
                                spiderTaskDos.stream().map(spiderTaskDo -> {
                                    AwsSpiderTaskDto awsSpiderTaskDto = new AwsSpiderTaskDto();
                                    BeanUtils.copyProperties(spiderTaskDo, awsSpiderTaskDto);
                                    awsSpiderTaskDto.setActive(spiderTaskDo.getIsActive());
                                    return awsSpiderTaskDto;
                                }).collect(Collectors.toList()));
                        return awsUserMarketDto;
                    }).collect(Collectors.toList()));
            return awsUserDto;
        }).collect(Collectors.toList());
    }


    public Integer createAwsUser(AwsUserDto awsUserDto) {
        checkParam(awsUserDto);
        AwsUserDo awsUserDo = new AwsUserDo();
        BeanUtils.copyProperties(awsUserDto, awsUserDo);
        return awsUserDao.insert(awsUserDo);
    }

    public Integer modAwsUser(AwsUserDto awsUserDto) {
        checkParam(awsUserDto);
        AwsUserDo awsUserDo = new AwsUserDo();
        BeanUtils.copyProperties(awsUserDto, awsUserDo);
        Integer result = awsUserDao.mod(awsUserDo);

        //同步修改aws api调用
        if (result == 1) {
            List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.getByUserId(awsUserDo.getId());
            if (!CollectionUtils.isEmpty(awsUserMarketDos)) {
                awsUserManager.syncAwsInfo(awsUserMarketDos);
            }
        }
        return result;
    }

    public Integer deleteAwsUser(Integer awsUserId) {
        //关联市场是否已经全部解除
        List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.getByUserId(awsUserId);
        if(!CollectionUtils.isEmpty(awsUserMarketDos)){
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "账号存在关联市场，无法删除");
        }
        return awsUserDao.delete(awsUserId);
    }

    public List<AwsUserMarketDto> marketList(){
        List<AwsUserDo> awsUserDos = awsUserDao.all();
        List<AwsUserMarketDto> list = Lists.newArrayList();
        awsUserDos.forEach(awsUserDo -> {
            List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.getByUserId(awsUserDo.getId());
            awsUserMarketDos.forEach(awsUserMarketDo -> {
                AwsUserMarketDto awsUserMarketDto = new AwsUserMarketDto();
                awsUserMarketDto.setId(awsUserMarketDo.getId());
                AwsMarket awsMarket = AwsMarket.getByMarketId(awsUserMarketDo.getMarketId());
                awsUserMarketDto.setShowText(String.format("%s|%s|%s", awsUserDo.getRemark(),  awsMarket.getDesc(), awsMarket.getId()));
                awsUserMarketDto.setAwsUserId(awsUserMarketDo.getAwsUserId());
                awsUserMarketDto.setMarketId(awsUserMarketDo.getMarketId());
                list.add(awsUserMarketDto);
            });
        });
        return list;
    }

    public JSONObject marketWarnMsg(){
        List<AwsUserDo> awsUserDos = awsUserDao.all();

        StringBuilder sb = new StringBuilder();
        awsUserDos.forEach(awsUserDo -> {
            List<AwsUserMarketDo> awsUserMarketDos = awsUserMarketDao.getByUserId(awsUserDo.getId());
            awsUserMarketDos.forEach(awsUserMarketDo -> {
                Long dayNum = TimeUtil.daysBetweenTwoDate(awsUserMarketDo.getUtime(), new Date());
                int remainDay = 90 - dayNum.intValue();

                if(remainDay < 30){
                    AwsMarket awsMarket = AwsMarket.getByMarketId(awsUserMarketDo.getMarketId());
                    String showText = String.format("【%s|%s|%s token还有%d天过期，请注意替换更新】",
                            awsUserDo.getRemark(),  awsMarket.getDesc(), awsMarket.getId(), remainDay);
                    sb.append(showText);
                }
            });
        });

        JSONObject jo = new JSONObject();
        boolean needWarn = false;
        if(sb.length() > 0){
            needWarn = true;
            jo.put("warnMsg", sb);
        }
        jo.put("needWarn", needWarn);
        return jo;
    }

    public Integer createMarketRelation(AwsUserMarketDto awsUserMarketDto) {
        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(awsUserMarketDto.getAwsUserId(), awsUserMarketDto.getMarketId());
        if (awsUserMarketDo != null) {
            throw new HzmException(ExceptionCode.AWS_MARKET_ERROR, "账号已关联市场");
        }

        if(StringUtils.isEmpty(awsUserMarketDto.getRefreshToken())){
            throw new HzmException(ExceptionCode.AWS_MARKET_ERROR, "refreshToken必填");
        }

        awsUserMarketDo = new AwsUserMarketDo();
        awsUserMarketDo.setAwsUserId(awsUserMarketDto.getAwsUserId());
        awsUserMarketDo.setMarketId(awsUserMarketDto.getMarketId());
        awsUserMarketDo.setRefreshToken(awsUserMarketDto.getRefreshToken());
        Integer result = awsUserMarketDao.insert(awsUserMarketDo);

        //添加账号市场api
        if(result.equals(1)){
            awsUserManager.addManager(awsUserMarketDo.getId());
        }
        return result;
    }

    public Integer modMarketRelation(AwsUserMarketDto awsUserMarketDto) {
        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(awsUserMarketDto.getId());
        if (awsUserMarketDo == null) {
            throw new HzmException(ExceptionCode.AWS_MARKET_ERROR, "市场关联关系不存在");
        }

        if(StringUtils.isEmpty(awsUserMarketDto.getRefreshToken())){
            throw new HzmException(ExceptionCode.AWS_MARKET_ERROR, "refreshToken必填");
        }

        awsUserMarketDo.setRefreshToken(awsUserMarketDto.getRefreshToken());
        return awsUserMarketDao.mod(awsUserMarketDo);
    }

    public Integer deleteMarketRelation(Integer userMarketId) throws Exception {
        List<AwsSpiderTaskDo> spiderTaskDos = awsSpiderTaskDao.getByUserMarketId(userMarketId);
        if(!CollectionUtils.isEmpty(spiderTaskDos)){
            throw new Exception("账号存在爬取任务，无法删除关联市场");
        }

        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getById(userMarketId);
        Integer result = awsUserMarketDao.delete(userMarketId);

        //删除账号市场api
        if(result.equals(1)){
            awsUserManager.deleteManager(awsUserMarketDo.getAwsUserId(), awsUserMarketDo.getMarketId());
        }
        return result;
    }

    public Integer createSpiderTask(AwsSpiderTaskDto awsSpiderTaskDto) {
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.getByUserMarketIdAndSpiderType(awsSpiderTaskDto.getUserMarketId(), awsSpiderTaskDto.getSpiderType());
        if (awsSpiderTaskDo != null) {
            throw new HzmException(ExceptionCode.AWS_SPIDER_TASK_ERROR, "爬取任务已存在");
        }

        awsSpiderTaskDo = new AwsSpiderTaskDo();
        awsSpiderTaskDo.setUserMarketId(awsSpiderTaskDto.getUserMarketId());
        awsSpiderTaskDo.setSpiderType(awsSpiderTaskDto.getSpiderType());
        awsSpiderTaskDo.setSpiderDepend(awsSpiderTaskDto.getSpiderDepend());
        awsSpiderTaskDo.setIsActive(0);
        return awsSpiderTaskDao.insert(awsSpiderTaskDo);
    }

    public Integer deleteSpiderTask(Integer spiderTaskId) {
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);
        if (awsSpiderTaskDo.getIsActive() == 1) {
            throw new HzmException(ExceptionCode.AWS_SPIDER_TASK_ERROR, "已激活的爬取任务无法删除");
        }
        Integer result = awsSpiderTaskDao.delete(spiderTaskId);
        if(result.equals(1)){
            taskManager.deleteSpiderTask(spiderTaskId);
        }
        return  result;
    }

    public Integer modSpiderTaskDepend(Integer spiderTaskId, String depend) {
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);
        if (awsSpiderTaskDo == null) {
            throw new HzmException(ExceptionCode.AWS_SPIDER_TASK_ERROR, "爬取任务不存在");
        }

        if (awsSpiderTaskDo.getIsActive() == 1) {
            throw new HzmException(ExceptionCode.AWS_SPIDER_TASK_ERROR, "已激活的爬取任务无法修改");
        }
        awsSpiderTaskDo.setSpiderDepend(depend);
        return awsSpiderTaskDao.update(awsSpiderTaskDo);
    }

    public Integer modSpiderTaskStatus(Integer spiderTaskId, Integer active) {
        AwsSpiderTaskDo awsSpiderTaskDo = awsSpiderTaskDao.select(spiderTaskId);
        if (awsSpiderTaskDo == null) {
            throw new HzmException(ExceptionCode.AWS_SPIDER_TASK_ERROR, "爬取任务不存在");
        }

        awsSpiderTaskDo.setIsActive(active);
        Integer result = awsSpiderTaskDao.update(awsSpiderTaskDo);
        if(result.equals(1)){
            //同步爬取任务
            if (active == 1) {
                taskManager.openSpiderTask(spiderTaskId);
            } else {
                taskManager.pauseSpiderTask(spiderTaskId);
            }
        }
        return result;
    }


    private void checkParam(AwsUserDto awsUserDto) {

        if (StringUtils.isEmpty(awsUserDto.getSellerId())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "sellerId为空");
        }

        if (StringUtils.isEmpty(awsUserDto.getAccessKeyId())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "accessKeyId为空");
        }

        if (StringUtils.isEmpty(awsUserDto.getSecretKey())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "secretKey为空");
        }

        if (StringUtils.isEmpty(awsUserDto.getRoleArn())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "roleArn为空");
        }

        if (StringUtils.isEmpty(awsUserDto.getClientId())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "clientId为空");
        }

        if (StringUtils.isEmpty(awsUserDto.getClientSecret())) {
            throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "clientSecret为空");
        }

        AwsUserDo awsUserDo = awsUserDao.getAwsUserByRemark(awsUserDto.getRemark());
        if (awsUserDo != null) {
            if (awsUserDto.getId() != null && !awsUserDo.getId().equals(awsUserDto.getId())) {
                throw new HzmException(ExceptionCode.AWS_USER_INFO_ERROR, "remark账号已存在");
            }
        }

        //todo 校验账号的正确性


    }
}
