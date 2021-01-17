package com.cn.hzm.server.service.impl;

import com.cn.hzm.core.constant.ResponseCode;
import com.cn.hzm.core.exception.HzmBasicRuntimeException;
import com.cn.hzm.core.util.CryptoUtil;
import com.cn.hzm.core.util.GsonUtil;
import com.cn.hzm.core.util.TimeUtil;
import com.cn.hzm.server.dao.HzmPassportDao;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmToken;
import com.cn.hzm.server.service.PassportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@Service
public class PassportServiceImpl implements PassportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PassportServiceImpl.class);

    @Value("${auth.token.secret:1234567890abcdef}")
    private String authTokenSecret;

    @Resource
    private HzmPassportDao passportDao;

    @Override
    public HzmPassport findPassportByUsername(String username) {
        return passportDao.findByUsername(username);
    }

    @Override
    public HzmPassport findPassportById(Long id) {
        return passportDao.findById(id);
    }

    @Override
    public HzmToken parseToken(String encryptToken) {
        String tokenJson;
        try {
            tokenJson = CryptoUtil.decryptWithAesEcbBase64(encryptToken, authTokenSecret);
        } catch (Exception e) {
            LOGGER.error("parse token failed! token: {}, msg: {}.", encryptToken, e.getMessage());
            throw new HzmBasicRuntimeException(ResponseCode.TOKEN_INVALID);
        }
        return GsonUtil.fromJson(tokenJson, HzmToken.class);
    }

    @Override
    public String generateToken(Long passportId) {
        HzmToken token = new HzmToken(passportId);
        try {
            return CryptoUtil.encryptWithAesEcbBase64(GsonUtil.toJson(token), authTokenSecret);
        } catch (Exception e) {
            LOGGER.error("generate token failed! passportId: {}.", passportId, e);
            throw new HzmBasicRuntimeException(ResponseCode.SERVER_ERROR);
        }
    }
}
