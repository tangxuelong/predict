package com.mojieai.predict.enums;

import com.mojieai.predict.cache.AwardInfoCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.redis.refresh.handler.pub.RedisPublishHandler;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public enum PeriodEnum {
    CURRENT_PERIOD(RedisConstant.CURRENT_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            getCurrentPeriod(gameId, TimelineEnum.START_TIME, TimelineEnum.END_TIME, redisService,
                    RedisConstant.GET_1_PERIOD);
        }
    }, CURRENT_PERIODS(RedisConstant.CURRENT_PERIODS) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            getCurrentPeriod(gameId, TimelineEnum.START_TIME, TimelineEnum.END_TIME, redisService,
                    RedisConstant.GET_100_PERIOD);
        }
    }, LAST_OPEN_PERIOD(RedisConstant.LAST_OPEN_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcHistoryPeriod(gameId, TimelineEnum.OPEN_TIME, redisService, RedisConstant.GET_1_PERIOD);
        }
    }, LAST_ALL_OPEN_PERIOD(RedisConstant.LAST_ALL_OPEN_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            //仅限于大盘彩，如果是其他彩种而且取所有中奖信息，直接赋值为空
            if (GameCache.getGame(gameId).getGameType() != Game.GAME_TYPE_COMMON) {
                redisService.kryoSetEx(getPeriodKey(gameId), Integer.MAX_VALUE, new ArrayList<>());
                return;
            }
            calcHistoryPeriod(gameId, TimelineEnum.OPEN_TIME, redisService, Integer.MAX_VALUE);
            RedisPublishHandler redisPublishHandler = SpringContextHolder.getBean("redisPublishHandler");
            redisPublishHandler.publish(RedisPubEnum.PERIOD_CONFIG.getChannel(), CommonUtil.mergeUnionKey(gameId,
                    getKeyName()));
        }
    }, RECENT_3PERIODS(RedisConstant.RECENT_SALE_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcRecentPeriod(gameId, TimelineEnum.START_TIME, TimelineEnum.END_TIME, redisService,
                    RedisConstant.GET_10_PERIOD);
        }
    }, LAST_100AWARD_PERIOD(RedisConstant.HISTORY_100_AWARD_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcHistoryAwardPeriod(gameId, TimelineEnum.AWARD_TIME, redisService, RedisConstant.GET_100_PERIOD);
        }
    }, LAST_50AWARD_PERIOD(RedisConstant.HISTORY_50_AWARD_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcHistoryAwardPeriod(gameId, TimelineEnum.AWARD_TIME, redisService, RedisConstant.GET_50_PERIOD);
        }
    }, LAST_30AWARD_PERIOD(RedisConstant.HISTORY_30_AWARD_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcHistoryAwardPeriod(gameId, TimelineEnum.AWARD_TIME, redisService, RedisConstant.GET_30_PERIOD);
        }
    }, LAST_AWARD_PERIOD(RedisConstant.LAST_AWARD_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calcHistoryAwardPeriod(gameId, TimelineEnum.AWARD_TIME, redisService, RedisConstant.GET_1_PERIOD);
        }
    }, CURRENT_AWARD_PERIOD(RedisConstant.CURRENT_AWARD_PERIOD) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calAwardCurrentPeriod(gameId, TimelineEnum.AWARD_TIME, redisService);
        }
    }, LAST_100_PREDICT_HISTORY(RedisConstant.LAST_100_PREDICT_HISTORY) {
        @Override
        public void calcPeriodInfo(Long gameId, RedisService redisService) {
            calPredcitHistory(gameId, redisService, 100);
        }
    };

    private String keyName;
    private static final Logger log = CronEnum.PERIOD.getLogger();

    PeriodEnum(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getPeriodKey(Long gameId) {
        return new StringBuffer().append(RedisConstant.PREFIX_PERIOD).append(gameId).append(getKeyName()).toString();
    }

    public static List<String> getPeriodKeys(Long gameId) {
        List<String> allKeys = new ArrayList<>();
        for (PeriodEnum periodEnum : values()) {
            allKeys.add(periodEnum.getPeriodKey(gameId));
        }
        return allKeys;
    }

    public static void setPeriodTimeLine(String periodTimeLineName, Long gameId, RedisService redisService) {
        for (PeriodEnum periodEnum : values()) {
            if (periodEnum.getKeyName().equals(periodTimeLineName)) {
                periodEnum.calcPeriodInfo(gameId, redisService);
            }
        }
    }

    abstract public void calcPeriodInfo(Long gameId, RedisService redisService);

    protected void calcHistoryAwardPeriod(Long gameId, TimelineEnum timeline, RedisService redisService, int count) {
        try {
            Long currentTime = System.currentTimeMillis();
            List<GamePeriod> periods = new ArrayList<>();

            String key = timeline.getTimelineKey(gameId);
            List<String> list = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, currentTime, 0, count,
                    String.class);

            for (String awaryPeriod : list) {
                String period = RedisConstant.getPeriodDetailKey(gameId, awaryPeriod);
                GamePeriod awardPeriod = redisService.kryoGet(period, GamePeriod.class);
                if (awardPeriod != null) {
                    periods.add(awardPeriod);
                }
            }
            // 默认20分更新, 由开奖通知完成更新以及过期时间
            int expireTime = RedisConstant.PERIOD_LARGE_EXPIRE;
            redisService.kryoSetEx(getPeriodKey(gameId), expireTime, periods);
        } catch (Exception e) {
            log.error("calcHistoryAwardPeriod error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected void calcFuturePeriodId(Long gameId, TimelineEnum timeline, RedisService redisService, int count) {
        try {
            List<GamePeriod> periods = new ArrayList<>();
            Long currentTime = System.currentTimeMillis();
            Long endDayTime = DateUtil.getEndOfToday().getTime();
            Long beginDayTime = endDayTime + 1000;

            String key = timeline.getTimelineKey(gameId);
            int expireTime = RedisConstant.PERIOD_EXPIRE;

            Game game = GameCache.getGame(gameId);
            GameFactory gameFactory = GameFactory.getInstance();
            AbstractGame gi = gameFactory.getGameBean(game.getGameEn());
            int future3Days = gi.getDailyPeriod() * count;

            //这里+1是为了防止取到正好结束的期次
            List<String> currentList = redisService.kryoZRangeByScoreGet(key, currentTime + 1, endDayTime, String
                    .class);
            List<String> futureList = redisService.kryoZRangeByScoreGet(key, beginDayTime + 1, Long.MAX_VALUE, 0,
                    future3Days, String.class);
            if (currentList.size() > 0) {
                String currentKey = RedisConstant.getPeriodDetailKey(gameId, currentList.get(0));
                GamePeriod period = redisService.kryoGet(currentKey, GamePeriod.class);
                expireTime = calcKeyExpireTime(period, timeline);
            }
            if (futureList.size() > 0) {
                currentList.addAll(futureList);
            }

            for (String periodId : currentList) {
                String periodDetailKey = RedisConstant.getPeriodDetailKey(gameId, periodId);
                periods.add(redisService.kryoGet(periodDetailKey, GamePeriod.class));
            }
            if (expireTime > 0) {
                redisService.kryoSetEx(getPeriodKey(gameId), expireTime, periods);
            }
        } catch (Exception e) {
            log.error("calcFuturePeriod error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected void calcHistoryPeriod(Long gameId, TimelineEnum timeline, RedisService redisService, int count) {
        try {
            Long currentTime = System.currentTimeMillis();
            String key = timeline.getTimelineKey(gameId);
            List<String> list = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, currentTime, 0, count,
                    String.class);
            if (list != null && list.size() > 0) {
                List<GamePeriod> periods = new ArrayList<>();
                if (count > 1) {
                    list = count > list.size() ? list : list.subList(0, count);
                    for (String awardPeriod : list) {
                        String period = RedisConstant.getPeriodDetailKey(gameId, awardPeriod);
                        periods.add(redisService.kryoGet(period, GamePeriod.class));
                    }
                    //这里+1是为了防止取到正好结束的期次
                    List<String> nextPeriods = redisService.kryoZRangeByScoreGet(TimelineEnum.AWARD_TIME.getTimelineKey
                            (gameId), currentTime + 1, Long.MAX_VALUE, 0, 1, String.class);
                    if (nextPeriods != null && nextPeriods.size() > 0) {
                        String periodDetailKey = RedisConstant.getPeriodDetailKey(gameId, nextPeriods.get(0));
                        GamePeriod period = redisService.kryoGet(periodDetailKey, GamePeriod.class);

                        if (period != null) {
                            int expireTime = (int) DateUtil.getDiffSeconds(new Timestamp(System.currentTimeMillis()),
                                    period.getAwardTime());
                            if (expireTime > 0) {
                                redisService.kryoSetEx(getPeriodKey(gameId), expireTime, periods);
                            }
                        } else {
                            log.error("calcHistoryPeriod cannot get currentPeriod, please check it!.period." + key);
                        }
                    } else {
                        log.error("calcHistoryPeriod cannot get currentPeriod, please check it!.nextPeriods." + key);
                    }
                } else {
                    GamePeriod period = redisService.kryoGet(RedisConstant.getPeriodDetailKey(gameId, list.get
                            (list.size() - 1)), GamePeriod.class);
                    int expireTime = calcKeyExpireTime(period, timeline);
                    if (expireTime > 0) {
                        redisService.kryoSetEx(getPeriodKey(gameId), expireTime, period);
                    }
                }
            } else {
                log.error("calcTimelinePeriod list is not exit!" + CommonUtil.mergeUnionKey(key, list));
            }
        } catch (Exception e) {
            log.error("calcTimelinePeriod error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected void calcRecentPeriod(Long gameId, TimelineEnum start, TimelineEnum end, RedisService
            redisService, int count) {
        try {
            int expireTime;
            List<Object> recentList = setTimelineList(gameId, start.getTimelineKey(gameId), end.getTimelineKey(gameId),
                    redisService, count);
            if (recentList.get(1) == null) {
                expireTime = RedisConstant.PERIOD_EXPIRE;
            } else {
                expireTime = calcKeyExpireTime((GamePeriod) recentList.get(1), end);
            }
            if (expireTime > 0) {
                redisService.kryoSetEx(getPeriodKey(gameId), expireTime, recentList);
            }
        } catch (Exception e) {
            log.error("calcRecentPeriod error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected void getFuturePeriod(Long gameId, TimelineEnum end, RedisService
            redisService, int count) {
        Long currentTime = System.currentTimeMillis();
        //这里+1是为了防止取到正好结束的期次
        List<String> futureList = redisService.kryoZRangeByScoreGet(end.getTimelineKey(gameId), currentTime + 1,
                Long.MAX_VALUE, 0, count, String.class);

        String futurePeriodId = futureList.size() > 0 ? futureList.get(0) : null;
        if (futurePeriodId != null) {
            String periodKey = RedisConstant.getPeriodDetailKey(gameId, futurePeriodId);
            GamePeriod period = redisService.kryoGet(periodKey, GamePeriod.class);
            if (period != null) {
                int expireTime = calcKeyExpireTime(period, end);
                if (expireTime > 0) {
                    redisService.kryoSetEx(getPeriodKey(gameId), expireTime, period);
                }
            } else {
                log.error("get futurePeriod does not exist in the redis, please check!, key: " + periodKey);
            }
        } else {
            log.error("get futureList is not exist, please check futureList timeline!, key: " + end.getTimelineKey
                    (gameId));
        }
    }

    protected void getCurrentPeriod(Long gameId, TimelineEnum start, TimelineEnum end, RedisService
            redisService, int count) {
        List<String> startList = new ArrayList<>();
        List<String> endList = new ArrayList<>();
        Long currentTime = System.currentTimeMillis();
        setRevTimelineList(start.getTimelineKey(gameId), startList, currentTime, Long.MIN_VALUE, 0, count,
                redisService);
        setTimelineList(end.getTimelineKey(gameId), endList, currentTime, Long.MAX_VALUE, 0, count, redisService);
        setPeriodRedis(gameId, startList, endList, start, end, count, redisService);
    }

    protected void setRevTimelineList(String key, List<String> list, Long max, Long min, int offset, int count,
                                      RedisService redisService) {
        try {
            list.addAll(redisService.kryoZRevRangeByScoreGet(key, min, max, offset, count, String.class));
        } catch (Exception e) {
            log.error("GetTimelineList error." + key + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected void setTimelineList(String key, List<String> list, Long min, Long max, int offset, int count,
                                   RedisService redisService) {
        try {
            //这里+1是为了防止取到正好结束的期次
            list.addAll(redisService.kryoZRangeByScoreGet(key, min + 1, max, offset, count, String.class));
        } catch (Exception e) {
            log.error("GetTimelineList error." + key + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected List<Object> setTimelineList(Long gameId, String startKey, String endKey, RedisService
            redisService, int count) {
        try {
            Long currentTime = System.currentTimeMillis();
            List<Object> recentList = new ArrayList<>();
            List<String> start = redisService.kryoZRevRangeByScoreGet(startKey, Long.MIN_VALUE, currentTime, 0,
                    count, String.class);
            //这里+1是为了防止取到正好结束的期次
            List<String> end = redisService.kryoZRangeByScoreGet(endKey, currentTime + 1, Long.MAX_VALUE, 0,
                    count, String.class);

            List<GamePeriod> currentPeriod = calcKeyPeriod(gameId, start, end, count, redisService);
            List<String> historyPeriod = redisService.kryoZRevRangeByScoreGet(endKey,
                    Long.MIN_VALUE, currentTime, 0, 1, String.class);
            List<String> futurePeriod = redisService.kryoZRangeByScoreGet(startKey, currentTime + 1, Long
                    .MAX_VALUE, 0, 1, String.class);

            GamePeriod history = null;
            GamePeriod future = null;
            if (historyPeriod.size() > 0) {
                String historyKey = historyPeriod.get(historyPeriod.size() - 1);
                history = redisService.kryoGet(RedisConstant.getPeriodDetailKey(gameId, historyKey), GamePeriod.class);
            }
            GamePeriod current = currentPeriod.size() > 0 ? currentPeriod.get(0) : null;
            if (futurePeriod.size() > 0) {
                future = redisService.kryoGet(RedisConstant.getPeriodDetailKey(gameId, futurePeriod.get(0)),
                        GamePeriod.class);
            }

            recentList.add(history);
            recentList.add(current);
            recentList.add(future);

            return recentList;
        } catch (Exception e) {
            log.error("setTimelineList error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
        return null;
    }

    protected void setPeriodRedis(Long gameId, List<String> startList, List<String> endList, TimelineEnum start,
                                  TimelineEnum end, int count, RedisService redisService) {
        try {
            List<GamePeriod> periods = calcKeyPeriod(gameId, startList, endList, count, redisService);
            if (periods != null && !periods.isEmpty()) {
                GamePeriod period = periods.get(periods.size() - 1);
                int expireTime = calcKeyExpireTime(period, end);
                if (expireTime > 0) {
                    redisService.kryoSetEx(getPeriodKey(gameId), expireTime, periods);
                }
            } else {
                String key = start.getTimelineKey(gameId);
                Long currentTime = System.currentTimeMillis();
                Long endDayTime = DateUtil.getEndOfToday().getTime();
                //这里+1是为了防止取到正好结束的期次
                List<String> currentList = redisService.kryoZRangeByScoreGet(key, currentTime + 1, endDayTime,
                        String.class);
                if (!currentList.isEmpty()) {
                    String periodKey = RedisConstant.getPeriodDetailKey(gameId, currentList.get(0));
                    GamePeriod period = redisService.kryoGet(periodKey, GamePeriod.class);
                    int expireTime = calcKeyExpireTime(period, start);
                    if (expireTime > 0) {
                        redisService.kryoSetEx(getPeriodKey(gameId), expireTime, new ArrayList<>());
                    }
                }
                log.error("setPeriodRedis period is not exit!" + CommonUtil.mergeUnionKey(gameId, periods));
            }
        } catch (Exception e) {
            log.error("setPeriodRedis error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
    }

    protected int calcKeyExpireTime(GamePeriod period, TimelineEnum end) {
        int expireTime = RedisConstant.PERIOD_EXPIRE;
        try {
            Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
            if (period != null) {
                Timestamp time = (Timestamp) ReflectHelperUtil.getValueByFieldName(period, end.getName());
                if (time != null) {
                    Long seconds = DateUtil.getDiffSeconds(currentTimeStamp, time);
                    int expireTimeTmp = (int) (seconds + IniCache.getIniIntValue(IniConstant.PERIOD_EXPIRE, 3));
                    expireTime = expireTimeTmp > 0 ? expireTimeTmp : expireTime;
                }
                return expireTime;
            } else {
                log.error("calcKeyExpireTime warning." + period + CommonConstant.COMMON_VERTICAL_STR + end.getName());
            }
        } catch (Exception e) {
            log.error("calcKeyexpireTime error." + period + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
        return expireTime;
    }

    protected List<GamePeriod> calcKeyPeriod(Long gameId, List<String> startList, List<String> endList, int count,
                                             RedisService redisService) {
        List<GamePeriod> periods = new ArrayList<>();
        try {
            for (String startPeriod : startList) {
                for (String endPeriod : endList) {
                    if (startPeriod.equals(endPeriod)) {
                        startPeriod = RedisConstant.getPeriodDetailKey(gameId, startPeriod);
                        GamePeriod period = redisService.kryoGet(startPeriod, GamePeriod.class);
                        if (period == null) {
                            log.error("period detail is not exit!" + CommonUtil.mergeUnionKey(gameId, startPeriod));
                            continue;
                        }
                        if (periods.size() >= count) {
                            return periods;
                        }
                        if (period != null) {
                            periods.add(period);
                        }
                    }
                }
            }
            return periods;
        } catch (Exception e) {
            log.error("calcKeyPeriod error." + gameId + CommonConstant.COMMON_VERTICAL_STR + this, e);
        }
        return periods;
    }

    protected void calAwardCurrentPeriod(long gameId, TimelineEnum timeline, RedisService redisService) {
        Long currentTime = System.currentTimeMillis();
        String key = timeline.getTimelineKey(gameId);
        List<String> award = redisService.kryoZRangeByScoreGet(key, currentTime + 1, Long.MAX_VALUE, 0,
                1, String.class);

        if (award != null && award.size() > 0) {
            GamePeriod awardCurrent = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, award.get(0));
            int expireTime = calcKeyExpireTime(awardCurrent, timeline);
            redisService.kryoSetEx(getPeriodKey(gameId), expireTime, awardCurrent);
        }

    }

    protected void calPredcitHistory(long gameId, RedisService redisService, int count) {
        long score = DateUtil.getCurrentTimestamp().getTime();
        String history100PredictWinkey = RedisConstant.getPredictNumsKey(gameId, "", RedisConstant
                .HISTORY_100_PREDICT_WIN, null);

        List<AwardDetail> awardDetails = redisService.kryoZRevRangeByScoreGet(history100PredictWinkey,
                Long.MIN_VALUE, score + 1, 0, count, AwardDetail.class);
        if (awardDetails != null && awardDetails.size() > 0) {
            GamePeriod awardCurrent = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, awardDetails.get(0)
                    .getPeriodId());
            Map<String, Object> awardDetailMaps = packageHistoryList(gameId, awardDetails);
            int expireTime = TrendUtil.getExprieSecond(awardCurrent.getAwardTime(), 3600);
            redisService.kryoSetEx(getPeriodKey(gameId), expireTime, awardDetailMaps);
        }

    }

    private Map<String, Object> packageHistoryList(long gameId, List<AwardDetail> awardDetails) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map> awardDetailMaps = new ArrayList<>();
        Map awardInfoMap = AwardInfoCache.getPeriodAwardInfoMap(gameId);

        for (AwardDetail awardDetail : awardDetails) {
            Map tempMap = new HashMap();
            String maxAwardLevelInfo = "";
            String[] awardLevelStr = null;
            int maxAwardLevel = awardDetail.getMaxAwardLevel();

            List<AwardInfo> awardInfos = (List<AwardInfo>) awardInfoMap.get(awardDetail.getPeriodId());
            Integer awardCount = null;
            if (awardInfos == null && maxAwardLevel >= 3) {
                awardInfos = GameFactory.getInstance().getGameBean(gameId).getDefaultAwardInfoList();
            }
            if (awardInfos != null && awardInfos.size() >= maxAwardLevel) {
                if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
                    for (AwardInfo awardInfo : awardInfos) {
                        if (awardInfo.getAwardLevel().equals(maxAwardLevel + "")) {
                            awardCount = awardInfo.getBonus().intValue();
                            break;
                        }
                    }
                } else {
                    awardCount = awardInfos.get(maxAwardLevel - 1).getBonus().intValue();
                }
                awardDetail.setSingleBonus(new BigDecimal(awardCount));
            } else {
                log.error("构建100期预测中奖异常，最大中奖奖级超过奖级size:" + maxAwardLevel + " 或者奖级为空");
            }
            if (awardDetail.getAwardLevelStr().length > 0) {
                maxAwardLevelInfo = awardDetail.getAwardLevelStr()[0].split(CommonConstant.SPACE_SPLIT_STR)[1];
                if (maxAwardLevel > 3) {
                    awardLevelStr = Arrays.copyOfRange(awardDetail.getAwardLevelStr(), 0, awardDetail
                            .getAwardLevelStr().length);
                } else {
                    awardLevelStr = Arrays.copyOfRange(awardDetail.getAwardLevelStr(), 1, awardDetail
                            .getAwardLevelStr().length);
                }
            }

            String singleBonus = "";
            if (awardDetail.getSingleBonus() != null) {
                singleBonus = TrendUtil.packageMoney(awardDetail.getSingleBonus().toString());
            }

            tempMap.put("gameId", awardDetail.getGameId());
            tempMap.put("periodId", awardDetail.getPeriodId());
            tempMap.put("periodName", awardDetail.getPeriodName());
            tempMap.put("singleBonus", "单注奖金:" + singleBonus + "元");//单注奖金
            tempMap.put("bonus", awardDetail.getBonus());//总奖金
            tempMap.put("awardLevel", awardDetail.getAwardLevel());
            tempMap.put("awardLevelStr", awardLevelStr);//中奖注数详情
            tempMap.put("maxAwardLevel", awardDetail.getMaxAwardLevel());
            tempMap.put("maxAwardLevelInfo", maxAwardLevelInfo);
            tempMap.put("historyPredictAwardSum", awardDetail.getHistoryPredictAwardSum());
            awardDetailMaps.add(tempMap);
        }

        String[] awardLevelSumArr = new String[]{"0注", "0注", "0注"};

        if (awardDetails != null && awardDetails.size() > 0 && StringUtils.isNotBlank(awardDetails.get(0)
                .getHistoryPredictAwardLevelSum())) {
            String[] historyAwardLevelSum = awardDetails.get(0).getHistoryPredictAwardLevelSum().split(CommonConstant
                    .COMMA_SPLIT_STR);

            if (historyAwardLevelSum != null && historyAwardLevelSum.length >= 3) {
                awardLevelSumArr[0] = historyAwardLevelSum[0] + "注";
                awardLevelSumArr[1] = historyAwardLevelSum[1] + "注";
                awardLevelSumArr[2] = historyAwardLevelSum[2] + "注";
            }
        }

        resultMap.put("awardLevelSumArr", awardLevelSumArr);
        resultMap.put("awardDetails", awardDetailMaps);
        return resultMap;
    }
}
