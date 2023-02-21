package com.mojieai.predict.dao;


import com.mojieai.predict.entity.po.SocialCodeSchedule;

import java.util.List;

public interface SocialCodeScheduleDao {

    SocialCodeSchedule insert(Long gameId, String periodId);

    void insert(SocialCodeSchedule socialCodeSchedule);

    List<SocialCodeSchedule> getUnFinishedSchedules(Long gameId, String periodId);

    SocialCodeSchedule getSocialCodeSchedule(long gameId, String periodId);

    int updateSocialCodeSchedule(long gameId, String periodId, String flagColumn, String timeColumn);
}
