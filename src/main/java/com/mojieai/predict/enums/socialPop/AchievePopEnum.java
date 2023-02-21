package com.mojieai.predict.enums.socialPop;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum AchievePopEnum {
    WEEK_ENCIRCLE_GOLD(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_WEEK_ENCIRCLE, CommonConstant
            .SOCIAL_CODE_TYPE_ENCIRCLE, CommonConstant.SOCIAL_RANK_TYPE_WEEK) {
        @Override
        public String getAwardDesc(Long userId, long gameId, String periodId, RedisService redisService) {
            Integer rank = SocialEncircleKillCodeUtil.getUserSocialRank(userId, gameId, periodId, getRankType(),
                    getSocialType(), redisService);
            return "周排名第<font color='#FF5050'>" + rank + "</font>，打败全国<font color='#FF5050'>" + 99.9 + "%</font>的彩迷朋友";
        }

        @Override
        public String getAwardTitle(Long userId, Timestamp periodTime) {
            return "上周战绩";
        }
    }, WEEK_KILL_GOLD(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_WEEK_KILL, CommonConstant
            .SOCIAL_CODE_TYPE_KILL, CommonConstant.SOCIAL_RANK_TYPE_WEEK) {
        @Override
        public String getAwardDesc(Long userId, long gameId, String periodId, RedisService redisService) {
            Integer rank = SocialEncircleKillCodeUtil.getUserSocialRank(userId, gameId, periodId, getRankType(),
                    getSocialType(), redisService);
            Integer random = new Random().nextInt(100) + 300;
            return "杀号周排名第<font color='#FF5050'>" + rank + "</font>，帮助彩迷朋友提高彩票命中率<font color='#FF5050'>" + random +
                    "%</font>，打败全国<font color='#FF5050'>" + 99.9 + "%</font>的彩迷朋友";
        }

        @Override
        public String getAwardTitle(Long userId, Timestamp periodTime) {
            return "上周战绩";
        }
    }, MONTH_ENCIRCLE_GOLD(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_MONTH_ENCIRCLE, CommonConstant
            .SOCIAL_CODE_TYPE_ENCIRCLE, CommonConstant.SOCIAL_RANK_TYPE_MONTH) {
        @Override
        public String getAwardDesc(Long userId, long gameId, String periodId, RedisService redisService) {
            Integer rank = SocialEncircleKillCodeUtil.getUserSocialRank(userId, gameId, periodId, getRankType(),
                    getSocialType(), redisService);
            return "月排名第<font color='#FF5050'>" + rank + "</font>，打败全国<font color='#FF5050'>" +
                    99.9 + "%</font>的彩迷朋友<br>";
        }

        @Override
        public String getAwardTitle(Long userId, Timestamp periodTime) {
            return DateUtil.getMonth(periodTime) + "月份战绩";
        }
    }, MONTH_KILL_GOLD(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_MONTH_KILL, CommonConstant
            .SOCIAL_CODE_TYPE_KILL, CommonConstant.SOCIAL_RANK_TYPE_MONTH) {
        @Override
        public String getAwardDesc(Long userId, long gameId, String periodId, RedisService redisService) {
            Integer rank = SocialEncircleKillCodeUtil.getUserSocialRank(userId, gameId, periodId, getRankType(),
                    getSocialType(), redisService);
            Integer random = new Random().nextInt(100) + 300;
            return "杀号月榜排名第<font color='#FF5050'>" + rank + "</font>，帮助彩迷朋友提高彩票命中率<font color='#FF5050'>" + random +
                    "%</font>，打败全国<font color='#FF5050'>" + 99.9 + "%</font>的彩迷朋友";
        }

        @Override
        public String getAwardTitle(Long userId, Timestamp periodTime) {
            return DateUtil.getMonth(periodTime) + "月份战绩";
        }
    };

    AchievePopEnum(Integer awardType, String socialType, String rankType) {
        this.awardType = awardType;
        this.socialType = socialType;
        this.rankType = rankType;
    }

    private Integer awardType;
    private String socialType;
    private String rankType;

    public abstract String getAwardDesc(Long userId, long gameId, String periodId, RedisService redisService);

    public abstract String getAwardTitle(Long userId, Timestamp periodTime);

    public Integer getAwardType() {
        return awardType;
    }

    public String getSocialType() {
        return socialType;
    }

    public String getRankType() {
        return rankType;
    }

    public Map<String, Object> getUserAchievePopContent(Long userId, long gameId, String periodId, RedisService
            redisService) {
        String userGoldPopFlagKey = RedisConstant.getUserAchievePopList(gameId, periodId, getSocialType(),
                getAwardType());

        if (!redisService.kryoSismemberSet(userGoldPopFlagKey, userId)) {
            return null;
        }
        Map<String, Object> content = new HashMap<>();
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        content.put("awardTitle", getAwardTitle(userId, lastOpenPeriod.getStartTime()));
        content.put("awardType", getAwardType());
        content.put("gameEn", GameCache.getGame(gameId).getGameEn());
        content.put("awardDesc", getAwardDesc(userId, gameId, lastOpenPeriod.getPeriodId(), redisService));
        content.put("socialType", getSocialType());
        return content;
    }

}
