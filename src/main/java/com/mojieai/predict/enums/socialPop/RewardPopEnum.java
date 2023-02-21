package com.mojieai.predict.enums.socialPop;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RewardPopEnum {
    PERIOD_RANK_POP(CommonConstant.SOCIAL_REWARD_POPUP_AWARD_TYPE_PERIOD) {
        @Override
        public Map<String, Object> getRewardPopMap(long gameId, Long userId, RedisService redisService) {
            Map<String, Object> periodAward = new HashMap<>();
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);

            /* 期榜*/
            Boolean inPeriodRank = Boolean.FALSE;
            Integer awardTimes = 0;
            String killRank = "";
            String enRank = "";
            String killPeriodRankKey = RedisConstant.getKillPeriodRank(gameId, lastOpenPeriod.getPeriodId());
            List<Long> killUserIdList = redisService.kryoZRange(killPeriodRankKey, 0L, -1L, Long.class);

            String enPeriodRankKey = RedisConstant.getEncirclePeriodRank(gameId, lastOpenPeriod.getPeriodId());
            List<Long> enUserIdList = redisService.kryoZRange(enPeriodRankKey, 0L, -1L, Long.class);

            Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                    .SOCIAL_RANK_TYPE_PERIOD);
            if (killUserIdList.contains(userId)) {
                inPeriodRank = Boolean.TRUE;
                int rank = SocialEncircleKillCodeUtil.getOnePeriodUserRank(gameId, lastOpenPeriod.getPeriodId(),
                        getAwardType(), "kill", userId, redisService);

                killRank = "杀号期榜第" + rank + "  ";
                if (null == predictNumsMap.get(String.valueOf(rank + 1L))) {
                    awardTimes += 1;
                } else {
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
            }
            if (enUserIdList.contains(userId)) {
                inPeriodRank = Boolean.TRUE;
                Long rank = redisService.kryoZRank(enPeriodRankKey, userId);
                enRank = "围号期榜第" + String.valueOf(rank + 1L);
                if (null == predictNumsMap.get(String.valueOf(rank + 1L))) {
                    awardTimes += 1;
                } else {
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
            }
            if (inPeriodRank) {
                periodAward.put("awardText", "智慧次数");
                periodAward.put("awardTimes", "+" + String.valueOf(awardTimes));
                periodAward.put("rankText", killRank + enRank);
                periodAward.put("awardType", getAwardType());
                periodAward.put("period", getPeriodName(gameId));
            }
            return periodAward;
        }

        @Override
        public String getPeriodName(long gameId) {
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            return currentPeriod.getPeriodId() + "期";
        }
    }, WEEK_RANK_POP(CommonConstant.SOCIAL_REWARD_POPUP_AWARD_TYPE_WEEK) {
        @Override
        public Map<String, Object> getRewardPopMap(long gameId, Long userId, RedisService redisService) {
            Map<String, Object> periodAward = new HashMap<>();
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
            if (!CommonUtil.getWeekIdByDate(lastOpenPeriod.getAwardTime()).equals(CommonUtil.getWeekIdByDate
                    (currentPeriod.getAwardTime()))) {
                /* 进入围号榜和杀号榜才有*/
                Boolean inWeekRank = Boolean.FALSE;
                Integer awardTimes = 0;
                String killRank = "";
                String enRank = "";
                String killWeekRankKey = RedisConstant.getKillWeekRank(gameId, CommonUtil.getWeekIdByDate(lastOpenPeriod
                        .getAwardTime()));
                List<Long> killUserIdList = redisService.kryoZRange(killWeekRankKey, 0L, 9L, Long.class);

                if (killUserIdList.size() >= 10) {
                    Double scoreTen = redisService.kryoZScore(killWeekRankKey, killUserIdList.get(9));
                    List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(killWeekRankKey, scoreTen.longValue(),
                            scoreTen.longValue(), Long.class);
                    killUserIdList.addAll(repeatPUser);
                }

                String enWeekRankKey = RedisConstant.getEncircleWeekRank(gameId, CommonUtil.getWeekIdByDate
                        (lastOpenPeriod.getAwardTime()));
                List<Long> enUserIdList = redisService.kryoZRange(enWeekRankKey, 0L, 9L, Long.class);

                Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                        .SOCIAL_RANK_TYPE_WEEK);
                if (killUserIdList.contains(userId)) {
                    inWeekRank = Boolean.TRUE;
                    Long rank = redisService.kryoZRank(killWeekRankKey, userId);
                    if (rank >= 10L) {
                        rank = 9L;
                    }
                    killRank = "杀号周榜第" + String.valueOf(rank + 1L) + "  ";
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
                if (enUserIdList.contains(userId)) {
                    inWeekRank = Boolean.TRUE;
                    Long rank = redisService.kryoZRank(enWeekRankKey, userId);
                    enRank = "围号周榜第" + String.valueOf(rank + 1L);
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
                if (inWeekRank) {
                    periodAward.put("awardText", "智慧次数");
                    periodAward.put("awardTimes", "+" + String.valueOf(awardTimes));
                    periodAward.put("rankText", killRank + enRank);
                    periodAward.put("awardType", getAwardType());
                    periodAward.put("period", getPeriodName(gameId));
                }
            }
            return periodAward;
        }

        @Override
        public String getPeriodName(long gameId) {
            String startPeriod = "";
            String endPeriod = "";
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            for (int i = 0; i < 3; i++) {
                if (CommonUtil.getWeekIdByDate(lastPeriod.getAwardTime()).equals(CommonUtil.getWeekIdByDate
                        (currentPeriod.getAwardTime())
                )) {
                    startPeriod = lastPeriod.getPeriodId();
                    lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, lastPeriod.getPeriodId());
                }
                if (CommonUtil.getWeekIdByDate(nextPeriod.getAwardTime()).equals(CommonUtil.getWeekIdByDate
                        (currentPeriod.getAwardTime()))) {
                    endPeriod = nextPeriod.getPeriodId();
                    nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastPeriod.getPeriodId());
                }
            }
            return startPeriod + "期-" + endPeriod + "期";
        }
    }, MONTH_RANK_POP(CommonConstant.SOCIAL_REWARD_POPUP_AWARD_TYPE_MONTH) {
        @Override
        public Map<String, Object> getRewardPopMap(long gameId, Long userId, RedisService redisService) {
            Map<String, Object> periodAward = new HashMap<>();
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
            if (!CommonUtil.getMonthIdByDate(lastOpenPeriod.getAwardTime()).equals(CommonUtil.getMonthIdByDate
                    (currentPeriod.getAwardTime()))) {
            /* 进入围号榜和杀号榜才有*/
                Boolean inMonthRank = Boolean.FALSE;
                Integer awardTimes = 0;
                String killRank = "";
                String enRank = "";
                String mKillMonthRankKey = RedisConstant.getKillMonthRank(gameId, CommonUtil.getMonthIdByDate
                        (lastOpenPeriod.getAwardTime()));
                List<Long> mKillUserIdList = redisService.kryoZRange(mKillMonthRankKey, 0L, 9L, Long.class);

                if (mKillUserIdList.size() >= 10) {
                    Double scoreTen = redisService.kryoZScore(mKillMonthRankKey, mKillUserIdList.get(9));
                    List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(mKillMonthRankKey, scoreTen.longValue(),
                            scoreTen.longValue(), Long.class);
                    mKillUserIdList.addAll(repeatPUser);
                }

                String enMonthRankKey = RedisConstant.getEncircleMonthRank(gameId, CommonUtil.getMonthIdByDate
                        (lastOpenPeriod.getAwardTime()));
                List<Long> mEnUserIdList = redisService.kryoZRange(enMonthRankKey, 0L, 9L, Long.class);

                Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                        .SOCIAL_RANK_TYPE_MONTH);

                if (mKillUserIdList.contains(userId)) {
                    inMonthRank = Boolean.TRUE;
                    Long rank = redisService.kryoZRank(mKillMonthRankKey, userId);
                    if (rank >= 10L) {
                        rank = 9L;
                    }
                    killRank = "杀号月榜第" + String.valueOf(rank + 1L) + "  ";
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
                if (mEnUserIdList.contains(userId)) {
                    inMonthRank = Boolean.TRUE;
                    Long rank = redisService.kryoZRank(enMonthRankKey, userId);
                    enRank = "围号月榜第" + String.valueOf(rank + 1L);
                    awardTimes += predictNumsMap.get(String.valueOf(rank + 1L));
                }
                if (inMonthRank) {
                    periodAward.put("awardText", "智慧次数");
                    periodAward.put("awardTimes", "+" + String.valueOf(awardTimes));
                    periodAward.put("rankText", killRank + enRank);
                    periodAward.put("awardType", getAwardType());
                    periodAward.put("period", getPeriodName(gameId));
                }
            }
            return periodAward;
        }

        @Override
        public String getPeriodName(long gameId) {
            return DateUtil.getCurrentMonth("yyyy年MM") + "月每期";
        }
    };

    RewardPopEnum(String awardType) {
        this.awardType = awardType;
    }

    private String awardType;

    public String getAwardType() {
        return awardType;
    }

    public abstract Map<String, Object> getRewardPopMap(long gameId, Long userId, RedisService redisService);

    public abstract String getPeriodName(long gameId);
}
