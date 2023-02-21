package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.TouristUserDao;
import com.mojieai.predict.entity.po.TouristUser;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TouristUserDaoImpl extends BaseDao implements TouristUserDao {

    @Override
    public TouristUser getUserIdByDeviceId(String deviceId) {
        Map params = new HashMap<>();
        params.put("deviceId", deviceId);
        return sqlSessionTemplate.selectOne("TouristUser.getUserIdByDeviceId", params);
    }

    @Override
    public TouristUser getUserIdByToken(String userToken) {
        Map params = new HashMap<>();
        params.put("userToken", userToken);
        return sqlSessionTemplate.selectOne("TouristUser.getUserIdByToken", params);
    }

    @Override
    public TouristUser getUserByUserId(Long userId) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("TouristUser.getUserByUserId", params);
    }

    @Override
    public Integer insert(TouristUser touristUser) {
        return sqlSessionTemplate.insert("TouristUser.insert", touristUser);
    }
}
