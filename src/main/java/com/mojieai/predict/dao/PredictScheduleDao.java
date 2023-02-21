package com.mojieai.predict.dao;


import com.mojieai.predict.entity.po.PredictSchedule;

import java.util.List;

public interface PredictScheduleDao {

    PredictSchedule insert(Long gameId, String periodId);

    void insert(PredictSchedule periodSchedule);

    List<PredictSchedule> getUnFinishedSchedules(Long gameId, String periodId);

    PredictSchedule getPredictSchedule(long gameId, String periodId);

    int updatePredictSchedule(long gameId, String periodId, String flagColumn, String timeColumn);
}
