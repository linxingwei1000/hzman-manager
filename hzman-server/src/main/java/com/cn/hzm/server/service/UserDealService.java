package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.UserConditionDto;
import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.repository.entity.FactoryDo;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.MD5Util;
import com.cn.hzm.core.repository.dao.FactoryDao;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmUserRole;
import com.cn.hzm.server.dto.*;
import com.cn.hzm.api.meta.HzmPassportStatus;
import com.cn.hzm.api.meta.HzmRoleType;
import com.cn.hzm.api.meta.HzmUserRoleValidType;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/27 2:41 下午
 */
@Component
public class UserDealService {

    @Autowired
    private PassportService passportService;

    @Autowired
    private FactoryDao factoryDao;

    @Autowired
    private HzmUserRoleService hzmUserRoleService;

    private static final String INSTALL_PASSWORD = "123456";

    /**
     * 获取用户列表
     */
    public JSONObject getUserList(UserConditionDto userConditionDTO) {
        String username = null;
        if (!StringUtils.isEmpty(userConditionDTO.getUsername())) {
            username = "%" + userConditionDTO.getUsername() + "%";
        }

        Integer companyId = null;
        if (userConditionDTO.getCompanyId() != null) {
            companyId = userConditionDTO.getCompanyId();
        }

        List<HzmPassport> list = passportService.findPassportByCondition(username, companyId);

        List<HzmUserDTO> userDTOS = userConditionDTO.pageResult(list).stream().map(this::processUserDO).collect(Collectors.toList());


        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data", JSONObject.toJSON(userDTOS));
        return respJo;
    }

    public HzmUserDTO getUserInfo(){
        Long userId = HzmContext.get().getAccountId();
        HzmPassport hzmPassport = passportService.findPassportById(userId);
        return processUserDO(hzmPassport);
    }

    /**
     * 添加用户
     *
     * @param hzmUserDTO
     * @return
     */
    @Transactional
    public Integer createUser(HzmUserDTO hzmUserDTO) {
        checkParam(hzmUserDTO);

        HzmPassport hzmPassport = new HzmPassport();
        hzmPassport.setUsername(hzmUserDTO.getUsername());
        hzmPassport.setPassword(MD5Util.inputPassToDbPass(INSTALL_PASSWORD));
        hzmPassport.setCompanyId(hzmUserDTO.getCompanyId());
        hzmPassport.setStatus(HzmPassportStatus.ENABLE.getValue());
        passportService.insertPassport(hzmPassport);

        List<HzmUserRole> list = Lists.newArrayList();
        for (RoleInfoDTO roleInfoDTO : hzmUserDTO.getRoleInfos()) {
            HzmUserRole hzmUserRole = new HzmUserRole();
            hzmUserRole.setPassportId(hzmPassport.getId());
            hzmUserRole.setRoleId(roleInfoDTO.getRoleId());
            hzmUserRole.setValid(HzmUserRoleValidType.EFFECTIVE.getType());
            hzmUserRole.setCreateTime(System.currentTimeMillis());
            hzmUserRole.setUpdateTime(System.currentTimeMillis());
            list.add(hzmUserRole);
        }
        hzmUserRoleService.insertList(list);
        return 1;
    }

    /**
     * 修改用户信息
     *
     * @param hzmUserDTO
     * @return
     */
    @Transactional
    public Integer updateUser(HzmUserDTO hzmUserDTO) {
        checkParam(hzmUserDTO);

        HzmPassport hzmPassport = new HzmPassport();
        hzmPassport.setId(hzmUserDTO.getId());
        hzmPassport.setUsername(hzmUserDTO.getUsername());
        hzmPassport.setCompanyId(hzmUserDTO.getCompanyId());
        passportService.updatePassport(hzmPassport);


        hzmUserRoleService.deleteRoleByPassportId(hzmUserDTO.getId());
        List<HzmUserRole> list = Lists.newArrayList();
        for (RoleInfoDTO roleInfoDTO : hzmUserDTO.getRoleInfos()) {
            HzmUserRole hzmUserRole = new HzmUserRole();
            hzmUserRole.setPassportId(hzmPassport.getId());
            hzmUserRole.setRoleId(roleInfoDTO.getRoleId());
            hzmUserRole.setValid(HzmUserRoleValidType.EFFECTIVE.getType());
            hzmUserRole.setCreateTime(System.currentTimeMillis());
            hzmUserRole.setUpdateTime(System.currentTimeMillis());
            list.add(hzmUserRole);
        }
        hzmUserRoleService.insertList(list);
        return 1;
    }

    /**
     * 修改用户密码
     *
     * @param userId
     * @param password
     * @return
     */
    public Integer updateUserPassword(Long userId, String password) {
        //只有自己能修改密码
        if (!HzmContext.get().getAccountId().equals(userId)) {
            throw new HzmException(ExceptionCode.USER_PASSWORD_MOD_ERROR);
        }
        HzmPassport hzmPassport = new HzmPassport();
        hzmPassport.setId(userId);
        hzmPassport.setPassword(MD5Util.formPassToDb(password));
        passportService.updatePassport(hzmPassport);
        return 1;
    }

    public Integer installPassword(Long userId) {
        HzmPassport hzmPassport = new HzmPassport();
        hzmPassport.setId(userId);
        hzmPassport.setPassword(MD5Util.inputPassToDbPass(INSTALL_PASSWORD));
        passportService.updatePassport(hzmPassport);
        return 1;
    }

    private HzmUserDTO processUserDO(HzmPassport user){
        HzmUserDTO hzmUserDTO = JSONObject.parseObject(JSONObject.toJSONString(user), HzmUserDTO.class);
        if (user.getCompanyId() == null || user.getCompanyId() == 0) {
            hzmUserDTO.setCompanyName("hzm");
        } else {
            FactoryDo factoryDO = factoryDao.getByFid(user.getCompanyId());
            hzmUserDTO.setCompanyName(factoryDO.getFactoryName());
        }

        //本用户转换名字
        if (HzmContext.get().getAccountId().equals(user.getId())) {
            hzmUserDTO.setIsMyself(true);
        }

        //转换用户角色
        List<HzmUserRole> userRoleList = hzmUserRoleService.findValidRoleByPassportId(user.getId());
        hzmUserDTO.setRoleInfos(userRoleList.stream().map(userRole -> {
            HzmRoleType roleType = HzmRoleType.getHzmRoleTypeByRoleId(userRole.getRoleId());
            RoleInfoDTO roleInfoDTO = new RoleInfoDTO();
            roleInfoDTO.setRoleId(roleType.getRoleId());
            roleInfoDTO.setDesc(roleType.getDesc());
            return roleInfoDTO;
        }).collect(Collectors.toList()));

        return hzmUserDTO;
    }

    private void checkParam(HzmUserDTO hzmUserDTO) {

        HzmPassport old = passportService.findPassportByUsername(hzmUserDTO.getUsername());
        if (old != null && !Objects.equals(old.getId(), hzmUserDTO.getId())) {
            throw new HzmException(ExceptionCode.USER_EXIST);
        }

        if (hzmUserDTO.getCompanyId() != 0) {
            if (factoryDao.getByFid(hzmUserDTO.getCompanyId()) == null) {
                throw new HzmException(ExceptionCode.FACTORY_NO_EXIST);
            }
        }

        for (RoleInfoDTO roleInfoDTO : hzmUserDTO.getRoleInfos()) {
            HzmRoleType roleType = HzmRoleType.getHzmRoleTypeByRoleId(roleInfoDTO.getRoleId());
            if (HzmRoleType.ROLE_DEFAULT.equals(roleType)) {
                throw new HzmException(ExceptionCode.USER_ROLE_MUST);
            }

            if (HzmRoleType.ROLE_FACTORY.equals(roleType) && hzmUserDTO.getCompanyId() == 0) {
                throw new HzmException(ExceptionCode.USER_ROLE_FACTORY_MUST_CHOOSE_FACTORY);
            }
        }
    }
}
