package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;
import java.util.Set;

/**
 * Created by qiwang on 2016/11/11.
 */
public interface PeriodService {

    List<GamePeriod> getPeriodsByGameIdAndPeriods(Long gameId, Set<String> periodIds);

    void prepareInitPeriods();

    void predictAllGamePeriods();

    void predictGamePeriods(Long gameId);

    void checkUnfinishedWorks();
}