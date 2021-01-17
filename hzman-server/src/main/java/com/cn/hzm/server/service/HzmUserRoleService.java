package com.cn.hzm.server.service;

import com.cn.hzm.server.meta.HzmUserRoleValidType;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.cn.hzm.server.domain.HzmUserRole;
import com.cn.hzm.server.dao.HzmUserRoleDao;

@Service
public class HzmUserRoleService {

    @Resource
    private HzmUserRoleDao hzmUserRoleDao;

    public int insert(HzmUserRole pojo){
        return hzmUserRoleDao.insert(pojo);
    }

    public int insertSelective(HzmUserRole pojo){
        return hzmUserRoleDao.insertSelective(pojo);
    }

    public int insertList(List<HzmUserRole> pojos){
        return hzmUserRoleDao.insertList(pojos);
    }

    public int update(HzmUserRole pojo){
        return hzmUserRoleDao.update(pojo);
    }

    public List<HzmUserRole> findValidRoleByPassportId(Long passportId) {
        return hzmUserRoleDao.findByPassportIdAndValidType(passportId, HzmUserRoleValidType.EFFECTIVE.getType());
    }
}
