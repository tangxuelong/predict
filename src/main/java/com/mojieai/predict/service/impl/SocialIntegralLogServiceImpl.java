package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.SocialLevelIntegralCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.SocialEncircleCodeDao;
import com.mojieai.predict.dao.SocialIntegralLogDao;
import com.mojieai.predict.dao.SocialKillCodeDao;
import com.mojieai.predict.dao.UserSocialIntegralDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.SocialLevelIntegralVo;
import com.mojieai.predict.entity.vo.UserSocialIntegralVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialIntegralLogService;
import com.mojieai.predict.service.UserSocialIntegralService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialIntegralLogServiceImpl implements SocialIntegralLogService, BeanSelfAware {

    protected Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private SocialKillCodeDao socialKillCodeDao;
    @Autowired
    private SocialIntegralLogDao socialIntegralLogDao;
    @Autowired
    private UserSocialIntegralDao userSocialIntegralDao;
    @Autowired
    private UserSocialIntegralService userSocialIntegralService;

    private SocialIntegralLogService self;

    @Override
    public void distributeIntegralTiming() {
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType() != null && Game.GAME_TYPE_COMMON == game.getGameType()) {
                GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
                //1.判断期次是否已经派发
                String key = RedisConstant.getSocialIntegralDistributeFlag(game.getGameId());
                String lastDistributePeriod = redisService.kryoGet(key, String.class);
                if (StringUtils.isNotBlank(lastDistributePeriod) && lastDistributePeriod.equals(lastOpenPeriod
                        .getPeriodId())) {
                    continue;
                }
                //2.派发
                boolean distribteFlag = distributeUserIntegral(lastOpenPeriod);
                //3.置位
                if (distribteFlag) {
                    GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(),
                            lastOpenPeriod.getPeriodId());
                    int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 3600);
                    redisService.kryoSetEx(key, expireTime, lastOpenPeriod.getPeriodId());
                }
            }
        }
    }

    @Override
    public boolean distributeUserIntegral(GamePeriod lastOpenPeriod) {
        boolean res = false;
        boolean encircleFlag = false;
        boolean killFlag = false;
        //1.获取该期次围号积分并派发
        List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByPeriodId(lastOpenPeriod
                .getGameId(), lastOpenPeriod.getPeriodId());
        for (SocialEncircle socialEncircle : socialEncircles) {
            if (socialEncircle.getIsDistribute() == null || socialEncircle.getIsDistribute().equals(CommonStatusEnum.NO
                    .getStatus()) || socialEncircle.getUserAwardScore() == null || socialEncircle.getUserAwardScore()
                    == 0) {
                continue;
            }
            String name = "围" + socialEncircle.getEncircleNums() + "中" + socialEncircle.getRightNums();
            encircleFlag = distributeIntegral2User(socialEncircle.getUserId(), socialEncircle.getGameId(),
                    socialEncircle.getPeriodId(), SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED,
                    socialEncircle.getEncircleCodeId(), Long.valueOf(socialEncircle.getUserAwardScore()), name);
        }
        //2.获取杀号积分并派发
        List<SocialKillCode> socialKillCodes = socialKillCodeDao.getKillNumsByPeriodId(lastOpenPeriod.getGameId(),
                lastOpenPeriod.getPeriodId());
        for (SocialKillCode socialKillCode : socialKillCodes) {
            if (socialKillCode.getIsDistribute() == null || socialKillCode.getIsDistribute().equals(CommonStatusEnum
                    .NO.getStatus()) || socialKillCode.getUserAwardScore() == null || socialKillCode
                    .getUserAwardScore() == 0) {
                continue;
            }
            String name = "杀" + socialKillCode.getKillNums() + "中" + socialKillCode.getRightNums();
            killFlag = distributeIntegral2User(socialKillCode.getUserId(), socialKillCode.getGameId(), socialKillCode
                    .getPeriodId(), SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, socialKillCode
                    .getKillCodeId(), Long.valueOf(socialKillCode.getUserAwardScore()), name);
        }
        //3.返回结果
        if (encircleFlag && killFlag) {
            res = true;
        }
        return res;
    }

    @Override
    public Boolean distributeIntegral2User(Long userId, long gameId, String periodId, Integer socialType, Long
            socialCode, Long score, String name) {
        //1.判断是否已经派发
        SocialIntegralLog socialIntegralLog = socialIntegralLogDao.getSocialIntegralLogByPk(userId, socialType,
                socialCode);
        if (socialIntegralLog != null && socialIntegralLog.getIsDistribute() != null && socialIntegralLog
                .getIsDistribute().equals(CommonStatusEnum.YES.getStatus())) {
            return true;
        }
        //2.
        if (socialIntegralLog == null) {
            socialIntegralLog = new SocialIntegralLog(socialType, socialCode, userId, gameId, periodId, name, score);
            try {
                socialIntegralLogDao.insert(socialIntegralLog);
            } catch (DuplicateKeyException e) {
                log.info("socialIntegralLog has been insert " + socialIntegralLog.getPeriodId());
            }

        }
        UserSocialIntegral userSocialIntegral = userSocialIntegralDao.getUserSocialIntegralByUserId(gameId, userId,
                false);
        if (userSocialIntegral == null) {
            userSocialIntegral = new UserSocialIntegral(gameId, userId);
            try {
                userSocialIntegralDao.insert(userSocialIntegral);
            } catch (DuplicateKeyException e) {
                log.info("UserSocialIntegral has been insert " + userSocialIntegral.getUserId());
            }
        }
        //3.更新用户积分并置位
        Boolean res = self.updateUserIntegralAndSetLog(gameId, userId, socialType, socialCode, score);
        //4.刷新缓存
        if (res) {
            userSocialIntegralService.refreshUserIntegralRedis(gameId, userId);
            recordUpgradeLevelUserId(gameId, periodId, userSocialIntegral, score, socialType);
        }
        return res;
    }

    private void recordUpgradeLevelUserId(long gameId, String periodId, UserSocialIntegral userSocialIntegral, Long
            score, Integer socialType) {
        SocialLevelIntegralVo levelIntegralVo = SocialLevelIntegralCache.getUserLevelVoByIntegralByScore
                (userSocialIntegral.getUserScore(), socialType);
        Long newScore = userSocialIntegral.getUserScore() + score;
        SocialLevelIntegralVo levelIntegralVoNew = SocialLevelIntegralCache.getUserLevelVoByIntegralByScore
                (newScore, socialType);
        if (levelIntegralVo != null && levelIntegralVoNew != null && !levelIntegralVo.getLevelId().equals
                (levelIntegralVoNew.getLevelId())) {
            String upgradeLevelUserSet = RedisConstant.getUpgradeLevelUserSet(gameId, periodId);
            redisService.kryoSAddSet(upgradeLevelUserSet, userSocialIntegral.getUserId());
        }
    }

    @Transactional
    @Override
    public Boolean updateUserIntegralAndSetLog(long gameId, Long userId, Integer socialType, Long socialCode, Long
            score) {
        Boolean res = null;
        UserSocialIntegral userSocialIntegral = userSocialIntegralDao.getUserSocialIntegralByUserId(gameId, userId,
                true);
        //1.更新积分
        Long lastScore = userSocialIntegral.getUserScore() == null ? 0 : userSocialIntegral.getUserScore();
        score += lastScore;
        res = userSocialIntegralDao.updateUserScore(gameId, score, userId) > 0;
        //2.更新log
        if (res) {
            socialIntegralLogDao.updateIntegralLogDistribute(userId, CommonStatusEnum.YES.getStatus(), socialType,
                    socialCode);
        }
        return res;
    }

    @Override
    public Map getUserIntegralLogInfo(Long gameId, String maxPeriodId, Long userId) {
        Map result = new HashMap();
        boolean hasNext = false;
        String lastPeriodId = "";
        Integer pageSize = SocialEncircleKillConstant.USER_SOCIAL_INTEGRAL_DETAIL_PAGE_SIZE;
        //1.获取该次查询最后的期次
        List<String> periodIds = socialIntegralLogDao.getSomePeriodIntervalPeriodId(userId, gameId, maxPeriodId,
                pageSize + 1);
        if (periodIds != null && periodIds.size() > 0) {
            if (periodIds.size() == (pageSize + 1)) {
                hasNext = true;
                lastPeriodId = periodIds.get(pageSize - 1);
            } else {
                lastPeriodId = periodIds.get(periodIds.size() - 1);
            }
        }
        //2.查询数据
        List<Map<String, Object>> integralDetails = null;
        List<SocialIntegralLog> integralLogs = socialIntegralLogDao.getUserIntegralBySectionPeriodId(userId, gameId,
                maxPeriodId, lastPeriodId);
        if (integralLogs != null) {
            integralDetails = convertSocialIntegralLog2List(integralLogs);
        }

        String key = RedisConstant.getUserIntegralKey(gameId, userId);
        UserSocialIntegralVo userSocialIntegralVo = redisService.kryoGet(key, UserSocialIntegralVo.class);

        String userSocre = "";
        if (userSocialIntegralVo != null) {
            userSocre = userSocialIntegralVo.getIntegral();
        }
        result.put("userScore", userSocre);
        result.put("integralDetails", integralDetails);
        result.put("lastPeriodId", lastPeriodId);
        result.put("hasNext", hasNext);
        return result;
    }

    /**
     * 自定义给用户派发社区积分
     */
    @Override
    public boolean customDistributeUserIntegral(Long userId, long score, long gameId, String periodId, String name) {
        Integer socialType = SocialEncircleKillConstant.SOCIAL_NEW_USER_INTEGRAL_TYPE;
        Long socialCode = userId + socialType;
        boolean res = distributeIntegral2User(userId, gameId, periodId, socialType, socialCode, score, name);
        return res;
    }

    @Override
    public void newUserDistributeIntegral(Long userId) {
        try {
            Game game = GameCache.getGame(GameConstant.SSQ);
            Game gameDlt = GameCache.getGame(GameConstant.DLT);
            GamePeriod currentPeriodSsq = PeriodRedis.getCurrentPeriod(game.getGameId());
            GamePeriod currentPeriodDlt = PeriodRedis.getCurrentPeriod(gameDlt.getGameId());

            customDistributeUserIntegral(userId, 100, game.getGameId(), currentPeriodSsq.getPeriodId(), "新用户赠送");
            customDistributeUserIntegral(userId, 100, game.getGameId(), currentPeriodDlt.getPeriodId(), "新用户赠送");
        } catch (Exception e) {
            log.error("newUserDistributeIntegral error " + userId);
        }
    }

    private List<Map<String, Object>> convertSocialIntegralLog2List(List<SocialIntegralLog> integralLogs) {
        Map<String, Object> tempIntegralMap = new HashMap<>();
        for (SocialIntegralLog integralLog : integralLogs) {
            List<Map> tempList = null;
            String key = integralLog.getPeriodId();
            if (!tempIntegralMap.containsKey(key)) {
                tempList = new ArrayList<>();
            } else {
                tempList = (List<Map>) tempIntegralMap.get(key);
            }
            tempList.add(convertSocialIntegralLog2Map(integralLog));
            tempIntegralMap.put(key, tempList);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Object> tempMap : tempIntegralMap.entrySet()) {
            Map<String, Object> temp = new HashMap();
            temp.put("periodName", tempMap.getKey() + "期");
            temp.put("integrals", tempMap.getValue());
            result.add(temp);
        }
        return result;
    }

    private Map convertSocialIntegralLog2Map(SocialIntegralLog integralLog) {
        Map result = new HashMap();

        result.put("name", integralLog.getName());
        result.put("score", "+" + integralLog.getScore());
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (SocialIntegralLogService) proxyBean;
    }
}
