package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.SocialClassicEncircleCodeDao;
import com.mojieai.predict.dao.SocialEncircleCodeDao;
import com.mojieai.predict.entity.bo.UserEncircleInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.SocialClassicEncircle;
import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.classicEncirleFilter.ClassicEncircleFilterEnum;
import com.mojieai.predict.enums.classicEncirleFilter.EncircleClaissicFilter;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialClassicEncircleCodeServiceImpl implements SocialClassicEncircleCodeService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private SocialClassicEncircleCodeDao socialClassicEncircleCodeDao;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;

    @Override
    public Map<String, Object> getClassicEncircleList(long gameId, String lastPeriodId, Integer socialType) {
        boolean hasNext = false;
        Map<String, Object> result = new HashMap<>();
        String key = RedisConstant.getClassicEncircleListKey(gameId, socialType);
        Integer pageSize = SocialEncircleKillConstant.SOCIAL_CLASSIC_ENCIRCLE_LIST_PERIOD_SIZE;

        //1.从缓存中取出数据
        List<Map> allEncircles = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, Long.valueOf(lastPeriodId),
                HashMap.class);
        List<Map> encircles = new ArrayList<>();
        if (allEncircles == null || allEncircles.size() <= 0) {
            rebuildSocialClassicRedis(gameId);
        }
        if (allEncircles != null && allEncircles.size() > 0) {
            int subSize = pageSize - 1;
            if (allEncircles.size() <= pageSize) {
                subSize = allEncircles.size();
            } else {
                hasNext = true;
                lastPeriodId = allEncircles.get(pageSize).get("periodId").toString();
            }
            encircles = allEncircles.subList(0, subSize);

            for (Map temp : encircles) {
                List<UserEncircleInfo> userEncircleInfos = (List<UserEncircleInfo>) temp.get("encircles");
                for (UserEncircleInfo tempInfo : userEncircleInfos) {
                    tempInfo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(DateUtil.formatTime
                            (tempInfo.getEncircleTimeBak())));
                    UserLoginVo userLoginVo = loginService.getUserLoginVo(tempInfo.getEncircleUserId());
                    tempInfo.setEncircleUserName(userLoginVo.getNickName());
                    tempInfo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
                    tempInfo.setVip(vipMemberService.checkUserIsVip(tempInfo.getEncircleUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_DIGIT));
                }
            }
        }

        result.put("hasNext", hasNext);
        result.put("encircles", encircles);
        result.put("lastPeriodId", lastPeriodId);
        return result;
    }

    @Override
    public void saveClassicEncircle2Redis(long gameId, String periodId, Map<String, Integer> socialKillAwardLevel) {
        String key = RedisConstant.getClassicEncircleListKey(gameId, SocialEncircleKillConstant
                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        log.info("saveClassicEncircle2Redis" + periodId);
        //1.获取某一期经典围号数据
        List<SocialClassicEncircle> encircleList = socialClassicEncircleCodeDao.getSocialClassicEncircleByCondition
                (gameId, periodId, null, null, 0);
        if (encircleList != null && encircleList.size() > 0) {
            //2.拼装Map保存数据
            List<UserEncircleInfo> userEncircleInfos = new ArrayList<>();
            for (SocialClassicEncircle encircle : encircleList) {
                UserEncircleInfo userEncircleInfo = socialEncircleCodeService.convertSocialEncircle2EncircleInfo
                        (encircle, socialKillAwardLevel);
                userEncircleInfos.add(userEncircleInfo);
            }
            if (userEncircleInfos.size() > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("periodId", periodId);
                result.put("encircles", userEncircleInfos);
                redisService.kryoZAddSet(key, Long.valueOf(periodId), result);
                GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
                int expireTime = TrendUtil.getExprieSecond(currentPeriod.getAwardTime(), 7200);
                redisService.expire(key, expireTime);
            }
        }
    }

    @Override
    public void saveSocialClassicEncircle(SocialEncircle socialEncircle) {
        if (socialEncircle == null || (socialEncircle.getIsDistribute() != null && socialEncircle.getIsDistribute() ==
                CommonStatusEnum.NO.getStatus())) {
            return;
        }
        Game game = GameCache.getGame(socialEncircle.getGameId());
        EncircleClaissicFilter[] ecfs = ClassicEncircleFilterEnum.getClassicEncircleFilterEnum(game.getGameEn())
                .getFilterEnum();
        for (EncircleClaissicFilter ecf : ecfs) {
            if (ecf.filterClassicEncircle(socialEncircle.getEncircleNums(), socialEncircle.getRightNums())) {
                try {
                    SocialClassicEncircle socialClassicEncircleEncircle = SocialEncircleKillCodeUtil
                            .convertSocialEncircle2ClassicPo(socialEncircle);
                    socialClassicEncircleCodeDao.insert(socialClassicEncircleEncircle);
                    break;
                } catch (Exception e) {
                    log.error("保存经典圈号异常", e);
                    break;
                }

            }
        }
    }

    @Override
    public void rebuildSocialClassicRedis(long gameId) {
        String beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_SSQ;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_DLT;
        }
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

        String key = RedisConstant.getClassicEncircleListKey(gameId, SocialEncircleKillConstant
                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        redisService.del(key);
        while (Integer.valueOf(beginPeriod) < Integer.valueOf(currentPeriod.getPeriodId())) {
            try {
                saveClassicEncircle2Redis(gameId, beginPeriod, socialKillAwardLevel);
                beginPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, beginPeriod).getPeriodId();
            } catch (Exception e) {
                log.error("重构经典围号异常", e);
                continue;
            }
        }
    }

    @Override
    public void saveSocialEncircle2ClassicDb(long gameId) {
        String beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_SSQ;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_DLT;
        }

        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        while (Integer.valueOf(beginPeriod) <= Integer.valueOf(currentPeriod.getPeriodId())) {
            try {
                List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByPeriodId(gameId,
                        beginPeriod);
                for (SocialEncircle socialEncircle : socialEncircles) {
                    saveSocialClassicEncircle(socialEncircle);
                }
                beginPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, beginPeriod).getPeriodId();
            } catch (Exception e) {
                log.error("重构经典围号异常", e);
                beginPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, beginPeriod).getPeriodId();
                continue;
            }
        }
    }
}
