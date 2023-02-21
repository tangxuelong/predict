package com.mojieai.predict.redis.base.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.PeriodEnum;
import com.mojieai.predict.enums.TimelineEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodStaticService;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//redis基础操作类，业务组合操作请使用RedisUtilServiceImpl

@Service
public class PeriodStaticServiceImpl implements PeriodStaticService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;

    @Override
    public GamePeriod getPeriodByGameIdAndPeriod(Long gameId, String periodId) {
        return redisService.kryoGet(RedisConstant.getPeriodDetailKey(gameId, periodId), GamePeriod.class);
    }

    @Override
    public List<GamePeriod> getTodayAllPeriods(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.TODAY_PERIODS);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.TODAY_PERIODS, list);
        return list;
    }

    @Override
    public GamePeriod getCurrentPeriod(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.CURRENT_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.CURRENT_PERIOD, list);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Override
    public List<GamePeriod> getCurrentPeriods(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.CURRENT_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.CURRENT_PERIOD, list);
        return (list == null || list.isEmpty()) ? null : list;
    }

    @Override
    public List<GamePeriod> getRecent3Periods(Long gameId) {
        String recentKey = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.RECENT_SALE_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(recentKey, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.RECENT_SALE_PERIOD, list);
        return list;
    }

    @Override
    public List<GamePeriod> getLastPeriodsByGameIds(List<Long> gameIds) {
        List<GamePeriod> periods = new ArrayList<>();
        for (Long gameId : gameIds) {
            String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_OPEN_PERIOD);
            GamePeriod period = redisService.kryoGet(key, GamePeriod.class);
            if (period != null) {
                periods.add(period);
            }
        }
        return periods;
    }

    @Override
    public List<GamePeriod> getLastAllOpenPeriodsByGameId(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_ALL_OPEN_PERIOD);
        List<GamePeriod> periods = redisService.kryoGet(key, ArrayList.class);
        return periods;
    }

    @Override
    public List<GamePeriod> getHistory100AwardPeriod(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.HISTORY_100_AWARD_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.HISTORY_100_AWARD_PERIOD, list);
        return list;
    }

    @Override
    public List<GamePeriod> getHistory50AwardPeriod(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.HISTORY_50_AWARD_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.HISTORY_50_AWARD_PERIOD, list);
        return list;
    }

    @Override
    public List<GamePeriod> getHistory30AwardPeriod(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.HISTORY_30_AWARD_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.HISTORY_30_AWARD_PERIOD, list);
        return list;
    }

    @Override
    public GamePeriod getLastPeriodByGameIdAndPeriodId(Long gameId, String periodId) {
        String key = TimelineEnum.END_TIME.getTimelineKey(gameId);
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (gamePeriod == null) {
            return null;
        }
        List<String> periods = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, gamePeriod.getEndTime()
                .getTime() - 1, 0, 1, String.class);
        if (periods == null || periods.isEmpty()) {
            return null;
        }
        String detailKey = RedisConstant.getPeriodDetailKey(gameId, periods.get(0));
        GamePeriod nextPeriod = redisService.kryoGet(detailKey, GamePeriod.class);
        return nextPeriod;
    }

    @Override
    public GamePeriod getNextPeriodByGameIdAndPeriodId(Long gameId, String periodId) {
        String key = TimelineEnum.END_TIME.getTimelineKey(gameId);
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (gamePeriod == null) {
            return null;
        }
        List<String> periods = redisService.kryoZRangeByScoreGet(key, gamePeriod.getEndTime().getTime() + 1,
                Long.MAX_VALUE, 0, 1, String.class);
        if (periods == null || periods.isEmpty()) {
            return null;
        }
        String detailKey = RedisConstant.getPeriodDetailKey(gameId, periods.get(0));
        GamePeriod nextPeriod = redisService.kryoGet(detailKey, GamePeriod.class);
        return nextPeriod;
    }

    @Override
    public List<GamePeriod> getLastAwardPeriodByGameId(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_AWARD_PERIOD);
        List<GamePeriod> list = redisService.kryoGet(key, ArrayList.class);
        list = resetRedisTimeline(gameId, RedisConstant.LAST_AWARD_PERIOD, list);
        return list;
    }

    @Override
    public GamePeriod getLastOpenPeriodByGameId(Long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_OPEN_PERIOD);
        GamePeriod period = redisService.kryoGet(key, GamePeriod.class);
        return period;
    }

    public List<GamePeriod> resetRedisTimeline(Long gameId, String periodKey, List<GamePeriod> list) {
        if (list == null) {
            String key = RedisConstant.getPeriodDetailKey(gameId, periodKey);
            PeriodEnum.setPeriodTimeLine(periodKey, gameId, redisService);
            list = redisService.kryoGet(key, ArrayList.class);
            log.info("ResetRedisTimeline RedisKey is empty, we can reflush this key: " + key);
        }
        return list;
    }

    @Override
    public GamePeriod getAwardCurrentPeriod(long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.CURRENT_AWARD_PERIOD);
        GamePeriod gamePeriod = redisService.kryoGet(key, GamePeriod.class);
        return gamePeriod;
    }

    @Override
    public Map<String, Object> getLast100PredictHistory(long gameId) {
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_100_PREDICT_HISTORY);
        Map<String, Object> awardDetails = redisService.kryoGet(key, HashMap.class);
        return awardDetails;
    }
}
