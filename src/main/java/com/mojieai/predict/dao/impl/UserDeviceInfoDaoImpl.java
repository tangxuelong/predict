package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserDeviceInfoDao;
import com.mojieai.predict.entity.po.UserDeviceInfo;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDeviceInfoDaoImpl extends BaseDao implements UserDeviceInfoDao {
    @Override
    public UserDeviceInfo getUserDeviceInfoByDeviceId(String deviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        return sqlSessionTemplate.selectOne("UserDeviceInfo.getUserDeviceInfoByDeviceId", params);
    }

    @Override
    public Long getDeviceIdByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserDeviceInfo.getDeviceIdByUserId", params);
    }

    @Override
    public void insert(UserDeviceInfo userDeviceInfo) {
        sqlSessionTemplate.insert("UserDeviceInfo.insert", userDeviceInfo);
    }

    @Override
    public int update(UserDeviceInfo userDeviceInfo) {
        return sqlSessionTemplate.insert("UserDeviceInfo.update", userDeviceInfo);
    }

    @Override
    public List<UserDeviceInfo> getAllUserDeviceInfoByShardType(String deviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        return sqlSessionTemplate.selectList("UserDeviceInfo.getAllUserDeviceInfoByShardType", params);
    }

    @Override
    public List<UserDeviceInfo> getNewDeviceCountByDate(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectList("UserDeviceInfo.getNewDeviceCountByDate", params);
    }

    @Override
    public int getDayActiveDeviceCountByDate(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectOne("UserDeviceInfo.getDayActiveDeviceCountByDate", params);
    }

    @Override
    public int getAllDeviceCount() {
        return otterSqlSessionTemplate.selectOne("UserDeviceInfo.getAllDeviceCount");
    }

    @Override
    public int getIosDayActiveDeviceCountByDate(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectOne("UserDeviceInfo.getIosDayActiveDeviceCountByDate",params);
    }
}