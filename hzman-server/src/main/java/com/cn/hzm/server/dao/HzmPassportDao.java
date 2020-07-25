package com.cn.hzm.server.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.cn.hzm.server.domain.HzmPassport;

@Mapper
public interface HzmPassportDao {
    int insert(@Param("pojo") HzmPassport pojo);

    int insertSelective(@Param("pojo") HzmPassport pojo);

    int insertList(@Param("pojos") List<HzmPassport> pojo);

    int update(@Param("pojo") HzmPassport pojo);

    HzmPassport findByUsername(@Param("username")String username);

    HzmPassport findById(@Param("id")Long id);

}
