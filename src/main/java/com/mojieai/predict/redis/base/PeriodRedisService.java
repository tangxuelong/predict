package com.mojieai.predict.redis.base;

import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;
import java.util.Set;

//redis基础操作类，业务组合操作请使用RedisUtilService
public interface PeriodRedisService {
    void refreshTimeline(Long gameId);

    void rebuildTimeline(Long gameId);

    void refreshPeriodInfo(Long gameId);

    void rebuildPeriodInfo(Long gameId);

    void refreshExpirePeriodInfo(Long gameId);

    void producePeriodChangeList(List<String> changeList);

    void consumePeriodChangeList();

    void consumePeriods(Long gameId, Set<String> periodIds);

    Boolean trimTimeline();

    void refreshTimeline();

    void storePeriod2Timeline(Long gameId, List<GamePeriod> periodList);

    void refreshPeriodInfo();
}
