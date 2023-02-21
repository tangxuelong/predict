package com.mojieai.predict.enums.socialPop;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.ActivityIni;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.UserSocialIntegralVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum SocialPopEnum {
    SOCIAL_REWARD_POP(CommonConstant.SOCIAL_POP_UP_TYPE_REWARD, CommonConstant.LOTTERY_TYPE_DIGIT) {
        @Override
        public List<Map<String, Object>> getPopContent(Long userId, long gameId, RedisService redisService) {
            if (checkIfPop(gameId, userId, getPopType(), redisService)) {
                return null;
            }
            List<Map<String, Object>> res = new ArrayList<>();
            for (RewardPopEnum rpe : RewardPopEnum.values()) {
                Map temp = rpe.getRewardPopMap(gameId, userId, redisService);
                if (temp != null && !temp.isEmpty()) {
                    Map<String, Object> popMap = new HashMap<>();
                    popMap.put("popupType", this.getPopType());
                    popMap.put("popupContent", temp);
                    res.add(popMap);
                }
            }
            return res;
        }
    }, SOCIAL_ACHIEVE_POP(CommonConstant.SOCIAL_POP_UP_TYPE_ACHIEVE, CommonConstant.LOTTERY_TYPE_DIGIT) {
        @Override
        public List<Map<String, Object>> getPopContent(Long userId, long gameId, RedisService redisService) {
            if (checkIfPop(gameId, userId, getPopType(), redisService)) {
                return null;
            }
            List<Map<String, Object>> res = new ArrayList<>();
            GamePeriod lastOpen = PeriodRedis.getLastOpenPeriodByGameId(gameId);
            for (AchievePopEnum ape : AchievePopEnum.values()) {
                Map<String, Object> temp = new HashMap<>();
                Map content = ape.getUserAchievePopContent(userId, gameId, lastOpen.getPeriodId(), redisService);
                if (content != null && !content.isEmpty()) {
                    temp.put("popupType", getPopType());
                    temp.put("popupContent", content);
                    res.add(temp);
                }
            }
            return res;
        }
    }, SOCIAL_LEVEL_POP(CommonConstant.SOCIAL_POP_UP_TYPE_LEVEL, CommonConstant.LOTTERY_TYPE_DIGIT) {
        @Override
        public List<Map<String, Object>> getPopContent(Long userId, long gameId, RedisService redisService) {
            if (checkIfPop(gameId, userId, getPopType(), redisService)) {
                return null;
            }
            List<Map<String, Object>> res = new ArrayList<>();
            Map<String, Object> userLevelPop = new HashMap<>();
            Map<String, Object> popupContent = new HashMap<>();

            GamePeriod lastPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
            String upgradeLevelUserSet = RedisConstant.getUpgradeLevelUserSet(gameId, lastPeriod.getPeriodId());
            if (!redisService.kryoSismemberSet(upgradeLevelUserSet, userId)) {
                return null;
            }

            //1.现获取该期用户所得积分
            String userIntegralKey = RedisConstant.getUserIntegralKey(gameId, userId);
            UserSocialIntegralVo userSocialIntegralVo = redisService.kryoGet(userIntegralKey, UserSocialIntegralVo
                    .class);
            if (userSocialIntegralVo == null || userSocialIntegralVo.getSocialLevel() == null) {
                return null;
            }
            Game game = GameCache.getGame(gameId);
            String key = ActivityIniConstant.USER_UPDATE_SOCIAL_LEVEl_POP_AD;
            String blessStr = ActivityIniCache.getActivityIniValue(key, "相信平凡的脚步也可以走出伟大的行程，祝你早日上岸！");
            String bless = "";
            if (StringUtils.isNotBlank(blessStr)) {
                String[] blessArr = blessStr.split("#");
                int bound = blessArr.length - 1;
                int i = new Random().nextInt(bound);
                bless = blessArr[i];
            }

            Integer passNum = getPassNum(userSocialIntegralVo.getSocialLevel());//todo 人数

            popupContent.put("gameEn", game.getGameEn());
            popupContent.put("awardType", userSocialIntegralVo.getSocialLevel());
            popupContent.put("awardTitle", "恭喜你的等级达到Lv." + userSocialIntegralVo.getSocialLevel() + "</font>");
            popupContent.put("awardDesc", "预测社区影响力超过了<font color='#FF8149'>" + passNum + "</font>人，<br>" + bless);
            userLevelPop.put("popupType", getPopType());
            userLevelPop.put("popupContent", popupContent);
            res.add(userLevelPop);
            return res;
        }
    }, FOOTBALL_MASTER_POP(CommonConstant.SOCIAL_POP_UP_TYPE_MASTER, CommonConstant.LOTTERY_TYPE_SPORTS) {
        @Override
        public List<Map<String, Object>> getPopContent(Long userId, long gameId, RedisService redisService) {

            String becomeSportsMaster = RedisConstant.getBecomeSportsMaster(SportsProgramConstant
                    .LOTTERY_LOTTERY_CODE_FOOTBALL);
            if (!redisService.kryoSismemberSet(becomeSportsMaster, userId)) {
                return null;
            }
            redisService.kryoSRem(becomeSportsMaster, userId);
            List<Map<String, Object>> res = new ArrayList<>();

            Map<String, Object> userMasterPop = new HashMap<>();
            Map<String, Object> popupContent = new HashMap<>();

            popupContent.put("gameEn", "");
            popupContent.put("awardType", "");
            popupContent.put("awardTitle", "");
            popupContent.put("awardDesc", "");
            userMasterPop.put("popupType", getPopType());
            userMasterPop.put("popupContent", popupContent);
            res.add(userMasterPop);
            return res;
        }
    };

    private static Integer getPassNum(Integer userSocialLevel) {
        Integer passNum = 0;
        Map<Integer, Map<String, Integer>> level = SocialEncircleKillConstant.SOCIAL_LEVEL;
        for (Map.Entry<Integer, Map<String, Integer>> tempLevel : level.entrySet()) {
            if (tempLevel.getKey() < userSocialLevel) {
                Integer begin = tempLevel.getValue().get("begin");
                Integer end = tempLevel.getValue().get("end");
                Integer diff = end - begin;
                passNum += new Random().nextInt(diff) + (begin);
            }
        }
        return passNum;
    }

    SocialPopEnum(Integer popType, Integer lotteryType) {
        this.popType = popType;
        this.lotteryType = lotteryType;
    }

    private Integer popType;
    private Integer lotteryType;

    public Integer getPopType() {
        return popType;
    }

    public Integer getLotteryType() {
        return lotteryType;
    }

    public abstract List<Map<String, Object>> getPopContent(Long userId, long gameId, RedisService redisService);

    public SocialPopEnum getSocialPopEnumByType(Integer popType) {
        for (SocialPopEnum spe : SocialPopEnum.values()) {
            if (spe.popType.equals(popType)) {
                return spe;
            }
        }
        return null;
    }

    public Boolean checkIfPop(long gameId, Long userId, Integer popType, RedisService redisService) {
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        String popupFlagKey = RedisConstant.getSocialPopupFlag(gameId, lastOpenPeriod.getPeriodId(), userId, popType);
        String popupFlag = redisService.kryoGet(popupFlagKey, String.class);
        if (StringUtils.isNotBlank(popupFlag)) {
            return Boolean.TRUE;
        }
        redisService.kryoSetEx(popupFlagKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                currentPeriod.getAwardTime()), String.valueOf(CommonStatusEnum.YES.getStatus()));
        return Boolean.FALSE;
    }
}
