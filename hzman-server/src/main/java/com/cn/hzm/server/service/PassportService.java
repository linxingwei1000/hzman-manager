package com.cn.hzm.server.service;

import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmToken;

import java.util.List;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public interface PassportService {

    Integer insertPassport(HzmPassport hzmPassport);

    Integer updatePassport(HzmPassport hzmPassport);

    HzmPassport findPassportByUsername(String username);

    HzmPassport findPassportById(Long id);

    List<HzmPassport> findPassportByCondition(String username, Integer companyId);

    HzmToken parseToken(String encryptToken);

    String generateToken(Long passportId);
}
