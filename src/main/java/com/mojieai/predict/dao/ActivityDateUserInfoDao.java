package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityDateUserInfo;

import java.util.List;

public interface ActivityDateUserInfoDao {
    ActivityDateUserInfo getUserTimesByDate(Integer activityId, Long userId, String dateId, Boolean isLock);

    List<ActivityDateUserInfo> getUserByDate(Integer activityId, String dateId, Boolean isLock);

    List<ActivityDateUserInfo> getAllActivityUserInfo(Integer activityId, Long userId);

    void update(ActivityDateUserInfo activityDateUserInfo);

    void insert(ActivityDateUserInfo activityDateUserInfo);
}
