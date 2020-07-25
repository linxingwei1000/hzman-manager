package com.cn.hzm.server.service.impl;

import com.cn.hzm.server.dao.HzmPassportDao;
import com.cn.hzm.server.domain.HzmPassport;
import com.cn.hzm.server.service.PassportService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by yuyang04 on 2020/7/18.
 */
@Service
public class PassportServiceImpl implements PassportService {

    @Resource
    HzmPassportDao passportDao;

    @Override
    public HzmPassport findPassportByUsername(String username) {
        return passportDao.findByUsername(username);
    }

    @Override
    public HzmPassport findPassportById(Long id) {
        return passportDao.findById(id);
    }
}
