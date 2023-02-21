package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityInfo;

import java.util.List;

public interface ActivityInfoDao {
    List<ActivityInfo> getEnableActivityInfo();

    ActivityInfo getActivityInfo(Integer activityId);

    void update(ActivityInfo activityInfo);

    void insert(ActivityInfo activityInfo);
}
