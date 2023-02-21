package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityUserLogDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityUserLog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityUserLogDaoImpl extends BaseDao implements ActivityUserLogDao {
    @Override
    public List<ActivityUserLog> getUserLog(Integer activityId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("ActivityUserLog.getUserLog", params);
    }

    @Override
    public List<ActivityUserLog> getDateUserLog(Integer activityId, Long userId, String dateId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);
        params.put("dateId", dateId);
        return sqlSessionTemplate.selectList("ActivityUserLog.getDateUserLog", params);
    }

    @Override
    public void insert(ActivityUserLog activityUserLog) {
        sqlSessionTemplate.insert("ActivityUserLog.insert", activityUserLog);
    }
}
