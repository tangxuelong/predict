package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.vo.ColdHotNumVo;

import java.util.List;
import java.util.Map;

public interface TrendService {
    void saveTrend2Db(Task task, PeriodSchedule dirtyPeriodSchedule);

    boolean spiderAward(Task task, PeriodSchedule dirtyPeriodSchedule);

    void saveTrend2DbManul(long gameId, String periodId);

    Map<String, Object> getHotColdSelectNum(Game game);
}
