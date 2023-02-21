package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PeriodSchedule;

import java.util.List;

public interface PeriodScheduleDao {
    PeriodSchedule insert(Long gameId, String periodId);

    void insert(PeriodSchedule periodSchedule);

    List<PeriodSchedule> getUnFinishedSchedules(Long gameId, String periodId);

    PeriodSchedule getPeriodSchedule(Long gameId, String periodId);

    int updatePeriodSchedule(Long gameId, String periodId, String flagColumn, String timeColumn);
}
