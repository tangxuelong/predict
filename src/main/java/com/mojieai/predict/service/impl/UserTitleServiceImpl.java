package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.TitleCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserSocialIntegralVo;
import com.mojieai.predict.entity.vo.UserTitleVo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.UserTitleLogService;
import com.mojieai.predict.service.UserTitleService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import com.mojieai.predict.util.UserTitleUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserTitleServiceImpl implements UserTitleService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;
    @Autowired
    private UserTitleDao userTitleDao;
    @Autowired
    private UserTitleLogDao userTitleLogDao;
    @Autowired
    private UserTitleLogService userTitleLogService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleMonthRankDao monthEncircleRankDao;
    @Autowired
    private SocialKillMonthRankDao monthKillRankDao;
    @Autowired
    private SocialEncircleWeekRankDao weekEncircleRankDao;
    @Autowired
    private SocialKillWeekRankDao weekKillRankDao;

    private UserTitleService self;

    /* 给用户派发头衔*/
    @Override
    public Boolean distributeTitle2User(long gameId, Long userId, String titleEn, String dateStr, Integer date) {
        Boolean res = Boolean.FALSE;
        //1.头衔不存在直接返回
        Title title = TitleCache.getTitleByEn(titleEn);
        if (title == null) {
            log.error("distribute title 2 user is error, titleEn:" + titleEn + " is not exist");
            return res;
        }

        //2.判断title是否已经派发
        UserTitleLog userTitleLog = userTitleLogDao.getUserTitleLogByDistributeId(gameId, userId, title.getTitleId(),
                dateStr);
        if (userTitleLog != null && userTitleLog.getIsDistribute() != null && userTitleLog.getIsDistribute().equals
                (UserTitleConstant.USER_TITLE_DISTRIBUTE_YES)) {
            return true;
        }
        UserTitle userTitle = userTitleDao.getUserTitleByUserIdAndTitleId(gameId, userId, title.getTitleId(), false);
        //3.如果不存在先插入log
        if (userTitleLog == null) {
            Timestamp lastEndTime = null;
            if (userTitle != null && userTitle.getEndTime() != null) {
                lastEndTime = userTitle.getEndTime();
            }
            userTitleLog = new UserTitleLog();
            userTitleLog.setTitleId(title.getTitleId());
            userTitleLog.setGameId(gameId);
            userTitleLog.setTitleLogId(userTitleLogService.generateTitleLogId(userId));
            userTitleLog.setMemo(UserTitleUtil.getUserTitleMemo(title.getTitleEn(), dateStr, date, lastEndTime));
            userTitleLog.setUserId(userId);
            userTitleLog.setDateStr(dateStr);
            userTitleLog.setDateNum(date);
            userTitleLog.setIsDistribute(UserTitleConstant.USER_TITLE_DISTRIBUTE_NO);
            userTitleLogDao.insert(userTitleLog);
        }

        //4.开始给用户派发头衔
        try {
            if (userTitle == null) {
                userTitle = new UserTitle(userId, title.getTitleId());
                userTitle.setGameId(gameId);
                userTitleDao.insert(userTitle);
            }
            res = self.updateUserTitleAndInsertLog(gameId, userId, title.getTitleId(), date, userTitleLog
                    .getTitleLogId());
            refreshUserTitleRedis(gameId, userId);
        } catch (Exception e) {
            log.error("派发头衔失败", e);
            return false;
        }
        return res;
    }

    @Override
    public Boolean checkUserTitle(long gameId, Long userId, String titleEn) {
        if (userId == null || StringUtils.isBlank(titleEn)) {
            throw new BusinessException("checkUserTitle params is error userId:" + userId + " titleEn:" + titleEn);
        }
        String key = RedisConstant.getUserTitleVoKey(gameId, userId);
        UserTitleVo userTitleVo = redisService.kryoGet(key, UserTitleVo.class);
        if (userTitleVo == null) {
            userTitleVo = refreshUserTitleRedis(gameId, userId);
        }
        if (userTitleVo == null) {
            return Boolean.FALSE;
        }
        Timestamp current = DateUtil.getCurrentTimestamp();
        if (titleEn.equals(UserTitleConstant.USER_TITLE_KILL_GOD_EN) && userTitleVo.getGodKillEndTime() != null) {
            return DateUtil.compareDate(current, userTitleVo.getGodKillEndTime());
        } else if (titleEn.equals(UserTitleConstant.USER_TITLE_ENCIRCLE_GOD_EN) && userTitleVo.getGodEncircleEndTime
                () != null) {
            return DateUtil.compareDate(current, userTitleVo.getGodEncircleEndTime());
        }
        return Boolean.FALSE;
    }

    @Override
    public List<String> getUserGodList(long gameId, Long userId, String versionCode) {
        Map<String, Object> userTitles = getUserTitleDetail(gameId, userId);
        List<String> godList = new ArrayList<>();

        if (StringUtils.isNotBlank(versionCode) && Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_3_3) {
            String userLevelKey = RedisConstant.getUserIntegralKey(gameId, userId);
            UserSocialIntegralVo userSocialIntegralVo = redisService.kryoGet(userLevelKey, UserSocialIntegralVo.class);
            String levelImg = SocialEncircleKillConstant.USER_SOCIAL_LEVEL_1;
            if (userSocialIntegralVo != null && StringUtils.isNotBlank(userSocialIntegralVo.getTitleSmallImg())) {
                levelImg = userSocialIntegralVo.getTitleSmallImg();
                if (StringUtils.isNotBlank(levelImg) && levelImg.contains("ovqsyejql.bkt.clouddn.com")) {
                    levelImg = levelImg.replace("ovqsyejql.bkt.clouddn.com", "sportsimg.mojieai.com");
                }
            }
            godList.add(levelImg);
        }

        if ((Boolean) userTitles.get("isGodKill")) {
            // 杀号大神
            godList.add("https://ohduoklem.qnssl.com/killGod@3x.png");
        }
        if ((Boolean) userTitles.get("isGodEncircle")) {
            // 围号大神
            godList.add("https://ohduoklem.qnssl.com/encircleGod@3x.png");
        }
        return godList;
    }

    @Override
    public Map<String, Object> getUserTitleDetail(long gameId, Long userId) {
        Map<String, Object> res = new HashMap<>();
        String key = RedisConstant.getUserTitleVoKey(gameId, userId);
        UserTitleVo userTitleVo = redisService.kryoGet(key, UserTitleVo.class);
        if (userTitleVo == null) {
            userTitleVo = refreshUserTitleRedis(gameId, userId);
        }
        if (userTitleVo == null) {
            return null;
        }
        boolean isGodKill = false;
        boolean isGodEncircle = false;
        Integer godKillTimes = 0;
        Integer godEncircleTimes = 0;
        Timestamp current = DateUtil.getCurrentTimestamp();
        if (userTitleVo.getGodKillEndTime() != null) {
            isGodKill = DateUtil.compareDate(current, userTitleVo.getGodKillEndTime());
            godKillTimes = userTitleVo.getGodKillTimes();
        }
        if (userTitleVo.getGodEncircleEndTime() != null) {
            isGodEncircle = DateUtil.compareDate(current, userTitleVo.getGodEncircleEndTime());
            godEncircleTimes = userTitleVo.getGodEncircleTimes();
        }
        res.put("isGodKill", isGodKill);
        res.put("isGodEncircle", isGodEncircle);
        res.put("godKillTimes", godKillTimes);
        res.put("godEncircleTimes", godEncircleTimes);
        return res;
    }

    @Transactional
    @Override
    public Boolean updateUserTitleAndInsertLog(long gameId, Long userId, Integer titleId, Integer date, String
            userTitleLogId) {
        Boolean res = Boolean.FALSE;
        UserTitle userTitle = userTitleDao.getUserTitleByUserIdAndTitleId(gameId, userId, titleId, true);
        Timestamp endTime = DateUtil.getCurrentTimestamp();
        if (userTitle.getEndTime() != null && DateUtil.compareDate(endTime, userTitle.getEndTime())) {
            endTime = userTitle.getEndTime();
        }
        endTime = DateUtil.getEndOfOneDay(DateUtil.getIntervalDays(endTime, date));
        Integer count = userTitle.getCounts() == null ? 1 : userTitle.getCounts() + 1;
        Integer resInsert = userTitleDao.updateUserTitleAviable(gameId, userId, titleId, count, endTime);
        //更新派发日志
        if (resInsert > 0) {
            userTitleLogDao.updateUserTitleLogDistributeStatus(userId, userTitleLogId, UserTitleConstant
                    .USER_TITLE_DISTRIBUTE_YES);
            res = Boolean.TRUE;
        }
        return res;
    }

    @Override
    public UserTitleVo refreshUserTitleRedis(long gameId, Long userId) {
        List<UserTitle> userTitles = userTitleDao.getUserAllTitle(userId);
        if (userTitles == null) {
            return null;
        }
        UserTitleVo userTitleVo = new UserTitleVo();
        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        Timestamp expireTime = currentTime;
        for (UserTitle userTitle : userTitles) {
            if (!userTitle.getGameId().equals(gameId)) {
                continue;
            }
            Title title = TitleCache.getTitleById(userTitle.getTitleId());
            //只添加可以用的title
            if (title != null && title.getEnable().equals(UserTitleConstant.USER_TITLE_ENABLE)) {
                if (title.getTitleEn().equals(UserTitleConstant.USER_TITLE_ENCIRCLE_GOD_EN)) {
                    userTitleVo.setGodEncircleEndTime(userTitle.getEndTime());
                    userTitleVo.setGodEncircleTimes(userTitle.getCounts());
                } else if (title.getTitleEn().equals(UserTitleConstant.USER_TITLE_KILL_GOD_EN)) {
                    userTitleVo.setGodKillEndTime(userTitle.getEndTime());
                    userTitleVo.setGodKillTimes(userTitle.getCounts());
                }
            }
            //过期时间
            if (DateUtil.compareDate(expireTime, userTitle.getEndTime())) {
                expireTime = userTitle.getEndTime();
            }
        }
        String key = RedisConstant.getUserTitleVoKey(gameId, userId);
        redisService.del(key);
        int expireTimes = TrendUtil.getExprieSecond(expireTime, 432000);
        redisService.kryoSetEx(key, expireTimes, userTitleVo);
        return userTitleVo;
    }

    /*定时派发 头衔*/
    @Override
    public void distributeTitleTiming() {
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON) && !game.getGameEn().equals(GameConstant.FC3D)) {
                GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
                GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), gamePeriod
                        .getPeriodId());
                monthRankUserTitleDistribute(gamePeriod, nextPeriod);
                weekRankUserTitleDistribute(gamePeriod, nextPeriod);
            }
        }
    }

    /*月榜头衔派发*/
    private void monthRankUserTitleDistribute(GamePeriod gamePeriod, GamePeriod nextPeriod) {
        boolean distributeMonth = false;
        String monthId = UserTitleUtil.getTargetMonthId(gamePeriod.getEndTime());
        String nextMonthId = UserTitleUtil.getTargetMonthId(nextPeriod.getEndTime());
        //1.当月不派发
        if (!monthId.equals(nextMonthId)) {
            distributeMonth = true;
        }
        //2.月榜奖励
        if (distributeMonth) {
            String key = RedisConstant.getUserTitleDistrbuteFlag(gamePeriod.getGameId(), monthId);
            //2.1判断该期是否已经派发
            String lastPeriod = redisService.kryoGet(key, String.class);
            if (lastPeriod != null && lastPeriod.equals(gamePeriod.getPeriodId())) {
                return;
            }
            List<Long> encircleMonthRank = monthEncircleRankDao.getSocialEncircleMonthTop(gamePeriod.getGameId(),
                    monthId, UserTitleConstant.USER_TITLE_MONTH_RANK_TOP_10);
            List<Long> killMonthRank = monthKillRankDao.getSocialKillMonthTop(gamePeriod.getGameId(), monthId,
                    UserTitleConstant.USER_TITLE_MONTH_RANK_TOP_10);
            if (encircleMonthRank != null && killMonthRank != null) {
                getUserRankPeople2Distribute(gamePeriod.getGameId(), gamePeriod, nextPeriod, monthId, UserTitleConstant
                        .MONTH_RANK_TOP_10_AWARD, encircleMonthRank, killMonthRank);
            }
        }
    }

    /*周榜头衔派发*/
    private void weekRankUserTitleDistribute(GamePeriod gamePeriod, GamePeriod nextPeriod) {
        boolean distributeWeek = false;
        //1.当周不派发
        String weekId = UserTitleUtil.getTargetWeekId(gamePeriod.getEndTime());
        String nextWeekId = UserTitleUtil.getTargetWeekId(nextPeriod.getEndTime());
        if (!weekId.equals(nextWeekId)) {
            distributeWeek = true;
        }
        //2.周榜奖励
        if (distributeWeek) {
            String key = RedisConstant.getUserTitleDistrbuteFlag(gamePeriod.getGameId(), weekId);
            //2.1判断该期是否已经派发
            String lastPeriod = redisService.kryoGet(key, String.class);
            if (lastPeriod != null && lastPeriod.equals(gamePeriod.getPeriodId())) {
                return;
            }
            List<Long> encircleWeekRank = weekEncircleRankDao.getSocialEncircleWeekTop(gamePeriod.getGameId(), weekId,
                    UserTitleConstant.USER_TITLE_WEEK_RANK_TOP_20);
            List<Long> killRankWeekTop = weekKillRankDao.getSocialKillWeekTop(gamePeriod.getGameId(), weekId,
                    UserTitleConstant.USER_TITLE_WEEK_RANK_TOP_20);
            if (encircleWeekRank != null && killRankWeekTop != null) {
                getUserRankPeople2Distribute(gamePeriod.getGameId(), gamePeriod, nextPeriod, weekId, UserTitleConstant
                        .WEEK_RANK_TOP_20_AWARD, encircleWeekRank, killRankWeekTop);
            }
        }
    }

    private void getUserRankPeople2Distribute(Long gameId, GamePeriod gamePeriod, GamePeriod nextPeriod, String
            dateStr, int awardDate, List<Long> encircleUserIds, List<Long> killUserIds) {
        //1.开始给符合条件人派发
        boolean distributeFlag = true;
        //1.1给围号榜派发
        for (Long userId : encircleUserIds) {
            boolean tempRes = distributeTitle2User(gameId, userId, UserTitleConstant.USER_TITLE_ENCIRCLE_GOD_EN,
                    dateStr, awardDate);
            if (!tempRes) {
                distributeFlag = false;
            }
        }
        //1.2给杀号榜派发
        for (Long userId : killUserIds) {
            boolean tempRes = distributeTitle2User(gameId, userId, UserTitleConstant.USER_TITLE_KILL_GOD_EN, dateStr,
                    awardDate);
            if (!tempRes) {
                distributeFlag = false;
            }
        }
        //3.置位
        if (distributeFlag) {
            //3.1设置redis标志位
            String key = RedisConstant.getUserTitleDistrbuteFlag(gameId, dateStr);
            int expireTime = TrendUtil.getExprieSecond(nextPeriod.getAwardTime(), 7200);
            redisService.kryoSetEx(key, expireTime, gamePeriod.getPeriodId());

            //3.2记录派发人数 方便弹窗
            if (encircleUserIds.size() > 0) {
                String userEncircleGoldPopFlagKey = RedisConstant.getUserAchievePopList(gameId, nextPeriod
                        .getPeriodId(), CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE, SocialEncircleKillCodeUtil
                        .getAwardTypeByAwardDate(awardDate, SocialEncircleKillConstant
                                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED));
                redisService.kryoSAddSet(userEncircleGoldPopFlagKey, encircleUserIds);
                redisService.expire(userEncircleGoldPopFlagKey, expireTime);
            }

            if (killUserIds.size() > 0) {
                String userKillGoldPopFlagKey = RedisConstant.getUserAchievePopList(gameId, nextPeriod.getPeriodId(),
                        CommonConstant.SOCIAL_CODE_TYPE_KILL, SocialEncircleKillCodeUtil.getAwardTypeByAwardDate
                                (awardDate, SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED));
                redisService.kryoSAddSet(userKillGoldPopFlagKey, killUserIds);
                redisService.expire(userKillGoldPopFlagKey, expireTime);
            }
        }
    }


    @Override
    public void setSelf(Object proxyBean) {
        this.self = (UserTitleService) proxyBean;
    }
}
