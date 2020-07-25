package com.cn.hzm.server.service;

import com.cn.hzm.server.domain.HzmPassport;

/**
 * Created by yuyang04 on 2020/7/18.
 */
public interface PassportService {

    HzmPassport findPassportByUsername(String username);

    HzmPassport findPassportById(Long id);
}
