package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityAwardLevelDaoImpl extends BaseDao implements ActivityAwardLevelDao {

    @Override
    public List<ActivityAwardLevel> getAwardLevelByActivityId(Integer activityId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        return sqlSessionTemplate.selectList("ActivityAwardLevel.getAwardLevelByActivityId", params);
    }

    @Override
    public ActivityAwardLevel getAwardLevel(Integer activityId, Integer levelId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("levelId", levelId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("ActivityAwardLevel.getAwardLevel", params);
    }

    @Override
    public void update(ActivityAwardLevel activityAwardLevel) {
        sqlSessionTemplate.update("ActivityAwardLevel.update", activityAwardLevel);
    }

    @Override
    public void insert(ActivityAwardLevel activityAwardLevel) {
        sqlSessionTemplate.insert("ActivityAwardLevel.insert", activityAwardLevel);
    }
}
