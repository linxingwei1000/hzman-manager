package com.cn.hzm.server.service;

import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.domain.HzmToken;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public interface PassportService {

    HzmPassport findPassportByUsername(String username);

    HzmPassport findPassportById(Long id);

    HzmToken parseToken(String encryptToken);

    String generateToken(Long passportId);
}
