package com.mojieai.predict.redis.base.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.PeriodEnum;
import com.mojieai.predict.enums.TimelineEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.ReflectHelperUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//redis基础操作类，业务组合操作请使用RedisUtilServiceImpl

@Service
public class PeriodRedisServiceImpl implements PeriodRedisService {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private RedisService redisService;

    @Autowired
    private GamePeriodDao gamePeriodDao;

    @Override
    public void refreshTimeline() {
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIds) {
            refreshTimeline(gameId);
        }
    }

    @Override
    public void refreshTimeline(Long gameId) {
        Game gameDetails = GameCache.getGame(gameId);
        if (gameDetails == null) {
            throw new BusinessException("彩种信息不存在！");
        }
        // 查询该彩种所涉及的期次信息
        List<GamePeriod> periodList = gamePeriodDao.getLoadedGamePeriod(gameId, gameDetails.getPeriodLoaded());
        storePeriod2Timeline(gameId, periodList);
    }

    // 重建一级缓存基础信息(包括periodDetailInfo) 会先删除key 慎用！
    @Override
    public void rebuildTimeline(Long gameId) {
        List<String> allKeys = TimelineEnum.getTimelineKeys(gameId);
        JedisCluster jedisCluster = redisService.getJedisCluster();
        allKeys.forEach(jedisCluster::del);
        refreshTimeline(gameId);
    }

    //刷新不同的timeling的period
    @Override
    public void refreshPeriodInfo() {
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIds) {
            refreshPeriodInfo(gameId);
        }
    }

    // 根据不同的timeline缓存不同的Period
    @Override
    public void refreshPeriodInfo(Long gameId) {
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            periodEnum.calcPeriodInfo(gameId, redisService);
        }
    }

    // 重建二级缓存支撑业务信息 会先删除key 慎用！
    @Override
    public void rebuildPeriodInfo(Long gameId) {
        List<String> allKeys = PeriodEnum.getPeriodKeys(gameId);
        JedisCluster jedisCluster = redisService.getJedisCluster();
        allKeys.forEach(jedisCluster::del);
        refreshPeriodInfo(gameId);
    }

    // 提供一个方法刷新所有3秒内过期的二级缓存
    @Override
    public void refreshExpirePeriodInfo(Long gameId) {
        List<Future<Boolean>> frResults = new ArrayList<>();
        ExecutorService exec = ThreadPool.getInstance().getPeriodExec();
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            frResults.add(exec.submit(() -> {
                String key = periodEnum.getPeriodKey(gameId);
                try {
                    Long numExpire = redisService.ttl(key);
                    if (numExpire <= IniCache.getIniIntValue(IniConstant.PERIOD_EXPIRE, 3)) {
                        Long startTime = System.currentTimeMillis();
                        periodEnum.calcPeriodInfo(gameId, redisService);
                        Long escapeTime = System.currentTimeMillis() - startTime;
                        if (escapeTime >= IniCache.getIniIntValue(IniConstant.PERIOD_THRESHOLD_MILLIS, 200)) {
                            log.info("escapeTime > " + IniCache.getIniIntValue(IniConstant.PERIOD_THRESHOLD_MILLIS,
                                    200) + " ms, please check " +
                                    "refreshExpirePeriodInfo!!" + key + ", time:" + escapeTime);
                        }
                    }

                } catch (Throwable t) {
                    log.error("refreshExpirePeriodInfo error." + key, t);
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }));
        }

        // 刷新期次失败的次数
        int failedCount = 0;
        for (
                Future<Boolean> fr : frResults) {
            try {
                Boolean result = fr.get();
                if (!result) {
                    failedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                log.error("refreshExpirePeriodInfo frResults get error." + CommonUtil.mergeUnionKey(gameId,
                        failedCount), e);
            }
        }
    }

    // Period 增量发布更新
    @Override
    public void producePeriodChangeList(List<String> changeList) {
        String changeKey = RedisConstant.PERIOD_CHANGE_LIST;
        if (changeList != null && !changeList.isEmpty()) {
            // 调度发布者投放增量发布的信息, redisPeriod:changeList => List[game_id:period_id, ......]
            JedisCluster jedisCluster = redisService.getJedisCluster();
            for (String changeInfo : changeList) {
                jedisCluster.rpush(changeKey, changeInfo);
            }
            log.info("producePeriodChangeList, changeList=" + changeList);
        }
    }

    // Period 增量消费
    @Override
    public void consumePeriodChangeList() {
        String changeKey = RedisConstant.PERIOD_CHANGE_LIST;
        List<String> changeList = redisService.getJedisCluster().lrange(changeKey, 0L, RedisConstant.REDIS_LAST_INDEX);
        //将changeList合并分类
        Map<String, HashSet<String>> gamePeriodMap = new HashMap<>();
        for (String changeInfo : changeList) {
            String[] infos = changeInfo.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_COLON_STR);
            if (!gamePeriodMap.containsKey(infos[0])) {
                gamePeriodMap.put(infos[0], new HashSet<>());
            }
            gamePeriodMap.get(infos[0]).add(infos[1]);
        }
        for (Map.Entry<String, HashSet<String>> entry : gamePeriodMap.entrySet()) {
            String gameIdStr = entry.getKey();
            Set<String> periodIds = entry.getValue();
            consumePeriods(Long.parseLong(gameIdStr), periodIds);
        }
        redisService.getJedisCluster().ltrim(changeKey, changeList.size(), RedisConstant.REDIS_LAST_INDEX);
        log.info("consumePeriodChangeList, changeList=" + changeList);
    }

    @Override
    public void consumePeriods(Long gameId, Set<String> periodIds) {
        List<GamePeriod> periods = gamePeriodDao.getPeriodsByGameIdAndPeriods(gameId, periodIds);

        storePeriod2Timeline(gameId, periods);

        log.info("[consumePeriods] storePeriod2Timeline is done.consumePeriods is start");

        // 更新历史开奖结果的timeline
        for (GamePeriod period : periods) {
            if (!StringUtils.isBlank(period.getWinningNumbers())) {
                PeriodEnum.LAST_AWARD_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_100AWARD_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.RECENT_3PERIODS.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_OPEN_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_ALL_OPEN_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_100AWARD_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_50AWARD_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_30AWARD_PERIOD.calcPeriodInfo(period.getGameId(), redisService);
                PeriodEnum.LAST_100_PREDICT_HISTORY.calcPeriodInfo(period.getGameId(), redisService);
                break;
            }
        }
        log.info("[consumePeriods] is done");
    }

    // 维护timeline缓存，应设置poll size大小，确保不能无限大
    @Override
    public Boolean trimTimeline() {
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIds) {
            Game game = GameCache.getGame(gameId);
            if (game == null) {
                throw new BusinessException("彩种信息不存在！");
            }
            JedisCluster jedisCluster = redisService.getJedisCluster();
            for (TimelineEnum tlEnum : TimelineEnum.values()) {
                try {
                    byte[] tlKeyByte = tlEnum.getTimelineKey(gameId).getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                    jedisCluster.zremrangeByRank(tlKeyByte, 0, -game.getPeriodLoaded());
                } catch (Exception e) {
                    log.error("trimTimeline error." + CommonUtil.mergeUnionKey(gameId, tlEnum.toString()), e);
                }
            }
        }
        log.info("trimTimeline execute end");
        return Boolean.TRUE;
    }

    @Override
    public void storePeriod2Timeline(Long gameId, List<GamePeriod> periodList) {
        try {
            log.info(">>>>>>>>>>>>>>>>>start");
            Map<TimelineEnum, Map<Object, Long>> timelineEnumMapMap = new HashMap<>();
            for (GamePeriod period : periodList) {

                // 构建一级缓存periodDetailsInfo数据结构
                String periodId = period.getPeriodId();
                String key = RedisConstant.getPeriodDetailKey(gameId, periodId);
                redisService.kryoSetEx(key, IniCache.getIniIntValue(IniConstant.PERIOD_DETAILS_EXPIRE_TIME, 1036800)
                        , period);

                //构建timeline
                for (TimelineEnum e : TimelineEnum.values()) {
                    if (!timelineEnumMapMap.containsKey(e)) {
                        timelineEnumMapMap.put(e, new HashMap<>());
                    }
                    Timestamp time = (Timestamp) ReflectHelperUtil.getValueByFieldName(period, e.getName());
                    if (time != null && StringUtils.isNotBlank(periodId)) {
                        timelineEnumMapMap.get(e).put(periodId, time.getTime());
                    }
                }
            }
            for (Map.Entry<TimelineEnum, Map<Object, Long>> entry : timelineEnumMapMap.entrySet()) {
                String tlKey = entry.getKey().getTimelineKey(gameId);
                redisService.kryoZAddSet(tlKey, entry.getValue());
            }

        } catch (Exception e) {
            log.error("storePeriod2Timeline error." + CommonUtil.mergeUnionKey(gameId, periodList.size()), e);
        }
    }
}
