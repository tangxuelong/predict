package com.mojieai.predict.redis.base;

import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;
import java.util.Map;

//Periods业务操作类
public interface PeriodStaticService {

    GamePeriod getPeriodByGameIdAndPeriod(Long gameId, String periodId);

    List<GamePeriod> getTodayAllPeriods(Long gameId);

    GamePeriod getCurrentPeriod(Long gameId);

    List<GamePeriod> getCurrentPeriods(Long gameId);

    List<GamePeriod> getRecent3Periods(Long gameId);

    List<GamePeriod> getLastPeriodsByGameIds(List<Long> gameIds);

    List<GamePeriod> getLastAllOpenPeriodsByGameId(Long gameId);

    List<GamePeriod> getHistory100AwardPeriod(Long gameId);

    List<GamePeriod> getHistory50AwardPeriod(Long gameId);

    List<GamePeriod> getHistory30AwardPeriod(Long gameId);

    GamePeriod getLastPeriodByGameIdAndPeriodId(Long gameId, String periodId);

    GamePeriod getNextPeriodByGameIdAndPeriodId(Long gameId, String periodId);

    List<GamePeriod> getLastAwardPeriodByGameId(Long gameId);

    GamePeriod getLastOpenPeriodByGameId(Long gameId);

    GamePeriod getAwardCurrentPeriod(long gameId);

    Map<String, Object> getLast100PredictHistory(long gameId);
}
