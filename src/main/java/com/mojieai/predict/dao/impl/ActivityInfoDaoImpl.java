package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityInfoDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityInfoDaoImpl extends BaseDao implements ActivityInfoDao {

    @Override
    public List<ActivityInfo> getEnableActivityInfo() {
        return sqlSessionTemplate.selectList("ActivityInfo.getEnabledActivity");
    }

    @Override
    public ActivityInfo getActivityInfo(Integer activityId) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        return sqlSessionTemplate.selectOne("ActivityInfo.getActivityInfo", params);
    }

    @Override
    public void update(ActivityInfo activityInfo) {
        sqlSessionTemplate.update("ActivityInfo.update", activityInfo);
    }

    @Override
    public void insert(ActivityInfo activityInfo) {
        sqlSessionTemplate.insert("ActivityInfo.insert", activityInfo);
    }
}
