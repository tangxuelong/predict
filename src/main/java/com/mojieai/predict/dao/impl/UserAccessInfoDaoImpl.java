package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserAccessInfoDao;
import com.mojieai.predict.entity.po.UserAccessInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserAccessInfoDaoImpl extends BaseDao implements UserAccessInfoDao {
    @Override
    public UserAccessInfo getUserAccessInfo(Integer accessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("accessId", accessId);
        return sqlSessionTemplate.selectOne("UserAccessInfo.getUserAccessInfo", params);

    }

    @Override
    public void insert(UserAccessInfo userAccessInfo) {
        sqlSessionTemplate.insert("UserAccessInfo.insert", userAccessInfo);
    }

    @Override
    public void update(UserAccessInfo userAccessInfo) {
        sqlSessionTemplate.update("UserAccessInfo.update", userAccessInfo);
    }
}
