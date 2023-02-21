package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserInfoDao;
import com.mojieai.predict.entity.po.UserInfo;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserInfoDaoImpl extends BaseDao implements UserInfoDao {
    @Override
    public UserInfo getUserInfo(Long userId) {
        return getUserInfo(userId, false);
    }

    @Override
    public UserInfo getUserInfo(Long userId, boolean lock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isLock", lock);
        return sqlSessionTemplate.selectOne("UserInfo.getUserInfo", params);
    }

    @Override
    public List<UserInfo> getUserInfoByNickNameFromOtter(String nickName) {
        Map<String, Object> param = new HashMap<>();
        param.put("nickName", nickName);
        return otterSqlSessionTemplate.selectList("UserInfo.getUserInfoByNickNameFromOtter", param);
    }

    @Override
    public void insert(UserInfo userInfo) {
        sqlSessionTemplate.insert("UserInfo.insert", userInfo);
    }

    @Override
    public int update(UserInfo userInfo) {
        return sqlSessionTemplate.update("UserInfo.update", userInfo);
    }

    @Override
    public int updateDeviceId(String deviceId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("userId", userId);
        return sqlSessionTemplate.update("UserInfo.updateDeviceId", params);
    }

    @Override
    public Integer updateRemark(Long userId, String setRemark, String originRemark) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("setRemark", setRemark);
        params.put("originRemark", originRemark);
        return sqlSessionTemplate.update("UserInfo.updateRemark", params);
    }

    @Override
    public List<UserInfo> geAllUserInfos() {
        return otterSqlSessionTemplate.selectList("UserInfo.geAllUserInfos");
    }

    @Override
    public List<UserInfo> geUserInfosByDate(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectList("UserInfo.geUserInfosByDate", params);
    }

    @Override
    public Integer getCountAllUserInfos() {
        return otterSqlSessionTemplate.selectOne("UserInfo.getCountAllUserInfos");
    }

    @Override
    public Integer getTodayNewUserCount(Timestamp beginDate,Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectOne("UserInfo.getTodayNewUserCount", params);
    }

}