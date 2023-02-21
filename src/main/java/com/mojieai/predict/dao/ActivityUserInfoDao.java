package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityUserInfo;

import java.util.List;

public interface ActivityUserInfoDao {
    ActivityUserInfo getUserTotalTimes(Integer activityId, Long userId);

    List<ActivityUserInfo> getUsers(Integer activityId);

    void update(ActivityUserInfo activityUserInfo);

    void insert(ActivityUserInfo activityUserInfo);
}
