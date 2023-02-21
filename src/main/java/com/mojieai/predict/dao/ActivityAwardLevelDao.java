package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityAwardLevel;

import java.util.List;

public interface ActivityAwardLevelDao {
    List<ActivityAwardLevel> getAwardLevelByActivityId(Integer activityId);

    ActivityAwardLevel getAwardLevel(Integer activityId, Integer levelId, Boolean isLock);

    void update(ActivityAwardLevel activityAwardLevel);

    void insert(ActivityAwardLevel activityAwardLevel);
}
