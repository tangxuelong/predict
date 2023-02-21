package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityDateUserInfoDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityDateUserInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityDateUserInfoDaoImpl extends BaseDao implements ActivityDateUserInfoDao {
    @Override
    public ActivityDateUserInfo getUserTimesByDate(Integer activityId, Long userId, String dateId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);
        params.put("dateId", dateId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("ActivityDateUserInfo.getUserTimesByDate", params);
    }

    @Override
    public List<ActivityDateUserInfo> getUserByDate(Integer activityId, String dateId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("dateId", dateId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectList("ActivityDateUserInfo.getUserByDate", params);
    }

    @Override
    public List<ActivityDateUserInfo> getAllActivityUserInfo(Integer activityId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("ActivityDateUserInfo.getAllActivityUserInfo", params);
    }

    @Override
    public void update(ActivityDateUserInfo activityDateUserInfo) {
        sqlSessionTemplate.update("ActivityDateUserInfo.update", activityDateUserInfo);
    }

    @Override
    public void insert(ActivityDateUserInfo activityDateUserInfo) {
        sqlSessionTemplate.insert("ActivityDateUserInfo.insert", activityDateUserInfo);
    }
}
