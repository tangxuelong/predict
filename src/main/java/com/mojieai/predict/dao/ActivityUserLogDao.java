package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityUserLog;

import java.util.List;

public interface ActivityUserLogDao {
    List<ActivityUserLog> getUserLog(Integer activityId, Long userId);

    List<ActivityUserLog> getDateUserLog(Integer activityId, Long userId, String dateId);

    void insert(ActivityUserLog activityUserLog);
}
