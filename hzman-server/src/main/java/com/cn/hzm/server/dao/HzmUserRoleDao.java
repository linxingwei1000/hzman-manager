package com.cn.hzm.server.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.cn.hzm.server.domain.HzmUserRole;

@Mapper
public interface HzmUserRoleDao {
    int insert(@Param("pojo") HzmUserRole pojo);

    int insertSelective(@Param("pojo") HzmUserRole pojo);

    int insertList(@Param("pojos") List<HzmUserRole> pojo);

    int update(@Param("pojo") HzmUserRole pojo);

    List<HzmUserRole> findByPassportIdAndValidType(@Param("passportId") Long passportId, @Param("valid") Integer valid);

    int deleteRoleByPassportId(@Param("passportId") Long passportId);
}
