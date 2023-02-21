package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityUserInfoDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityUserInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityUserInfoDaoImpl extends BaseDao implements ActivityUserInfoDao {
    @Override
    public ActivityUserInfo getUserTotalTimes(Integer activityId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("ActivityUserInfo.getUserTotalTimes", params);
    }

    @Override
    public List<ActivityUserInfo> getUsers(Integer activityId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        return sqlSessionTemplate.selectList("ActivityUserInfo.getUsers", params);
    }

    @Override
    public void update(ActivityUserInfo activityUserInfo) {
        sqlSessionTemplate.update("ActivityUserInfo.update", activityUserInfo);
    }

    @Override
    public void insert(ActivityUserInfo activityUserInfo) {
        sqlSessionTemplate.insert("ActivityUserInfo.insert", activityUserInfo);
    }
}
