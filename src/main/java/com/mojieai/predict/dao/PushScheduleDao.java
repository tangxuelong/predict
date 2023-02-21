package com.mojieai.predict.dao;


import com.mojieai.predict.entity.po.PushSchedule;

public interface PushScheduleDao {

    void insert(PushSchedule pushSchedule);

    PushSchedule getPushSchedule(long gameId, String periodId);

    int updatePushSchedule(long gameId, String periodId, String flagColumn, String timeColumn);
}
