package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.AchievementVo;
import com.mojieai.predict.entity.vo.FollowInfoVo;
import com.mojieai.predict.entity.vo.SocialRankVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.achievement.DltAchievementEnum;
import com.mojieai.predict.enums.achievement.SocialAchievement;
import com.mojieai.predict.enums.achievement.SsqAchievementEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/10/11.
 */
@Service
public class SocialServiceImpl implements SocialService, BeanSelfAware {
    @Autowired
    private SocialCodeScheduleDao socialCodeScheduleDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private SocialEncircleAwardLevelDao socialEncircleAwardLevelDao;
    @Autowired
    private SocialEncirclePeriodRankDao socialEncirclePeriodRankDao;
    @Autowired
    private SocialEncircleWeekRankDao socialEncircleWeekRankDao;
    @Autowired
    private SocialEncircleMonthRankDao socialEncircleMonthRankDao;
    @Autowired
    private SocialKillPeriodRankDao socialKillPeriodRankDao;
    @Autowired
    private SocialKillWeekRankDao socialKillWeekRankDao;
    @Autowired
    private SocialKillMonthRankDao socialKillMonthRankDao;
    @Autowired
    private SocialKillCodeDao socialKillCodeDao;
    @Autowired
    private SocialKillAwardLevelDao socialKillAwardLevelDao;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;
    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private IndexUserSocialCodeService indexUserSocialCodeService;
    @Autowired
    private UserSocialRecordDao userSocialRecordDao;
    @Autowired
    private UserSocialRecordService userSocialRecordService;
    @Autowired
    private SocialUserFollowInfoDao socialUserFollowInfoDao;
    @Autowired
    private SocialUserFollowDao socialUserFollowDao;
    @Autowired
    private SocialUserFansDao socialUserFansDao;
    @Autowired
    private SocialClassicEncircleCodeService socialClassicEncircleCodeService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserTitleService userTitleService;

    private SocialService self;


    private static final Logger log = LogConstant.commonLog;

    /* 排行榜接口*/
    @Override
    public Map<String, Object> getSocialRankList(Long gameId, Long userId, String socialType, String rankType,
                                                 Integer pageIndex) {
        // 排行榜
        Integer pageCount = SocialEncircleKillConstant.SOCIAL_KILL_NUM_RANK_PAGE_SIZE;
        Integer nextPage;
        Integer isHaveNextPage;
//        if (null == pageIndex) {
        pageIndex = 0;
//        }

        Map<String, Object> resultMap = new HashMap<>();
        List<SocialRankVo> socialRankVos = new ArrayList<>();
        Map<String, Object> currentUserRank = new HashMap<>();
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        String redisRankKey = SocialEncircleKillCodeUtil.getRedisRankKey(gameId, lastOpenPeriod.getPeriodId(),
                rankType, socialType);
        List<Long> userIdList = redisService.kryoZRange(redisRankKey, Long.valueOf(pageIndex *
                pageCount), Long.valueOf(pageIndex * pageCount + (pageCount - 1)), Long.class);
        for (Long rankUserId : userIdList) {
            SocialRankVo socialRankVo = new SocialRankVo();
            socialRankVo.setUserId(rankUserId + "");
            UserLoginVo userLoginVo = loginService.getUserLoginVo(rankUserId);
            socialRankVo.setHeadImgUrl(userLoginVo.getHeadImgUrl());
            socialRankVo.setNickName(userLoginVo.getNickName());
            socialRankVo.setRank(redisService.kryoZRank(redisRankKey, rankUserId).intValue() + 1);
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                /* 杀号重复排名*/
                Integer userScore = -redisService.kryoZScore(redisRankKey, rankUserId).intValue();
                if (socialRankVos.size() == 0) {
                    Double userSameScore = redisService.kryoZScore(redisRankKey, rankUserId);
                    List<Long> userSameScoreList = redisService.kryoZRangeByScoreGet(redisRankKey, userSameScore
                            .longValue(), userSameScore.longValue(), Long.class);
                    Long rank = redisService.kryoZRank(redisRankKey, rankUserId);
                    List<Long> userIdsL = redisService.kryoZRange(redisRankKey, rank - userSameScoreList.size(), rank
                            - 1, Long.class);
                    int count = 0;
                    for (int i = userIdsL.size() - 1; i >= 0; i--) {
                        Double sameScoreUser = redisService.kryoZScore(redisRankKey, userIdsL.get(i));
                        if (!sameScoreUser.equals(userSameScore)) {
                            socialRankVo.setRank(socialRankVo.getRank() - count);
                            break;
                        }
                        count++;
                    }
                }
                if (socialRankVos.size() > 0 && userScore.equals(socialRankVos.get(socialRankVos.size()
                        - 1).getUserScore())) {
                    socialRankVo.setRank(socialRankVos.get(socialRankVos.size() - 1).getRank());
                }
            }
            socialRankVo.setUserScore(-redisService.kryoZScore(redisRankKey, rankUserId).intValue());
            socialRankVo.setIsCurrentUser(CommonStatusEnum.NO.getStatus());
            socialRankVo.setUserAwardDesc(getUserAwardDesc(rankType, socialRankVo.getRank().toString(), 0, gameId));
            if (userId != null && rankUserId.equals(userId)) {
                socialRankVo.setIsCurrentUser(CommonStatusEnum.YES.getStatus());
            }
            socialRankVo.setVip(vipMemberService.checkUserIsVip(rankUserId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
            socialRankVos.add(socialRankVo);
        }

        nextPage = getNextPage(redisService.kryoZCount(redisRankKey, Long.MIN_VALUE, Long.MAX_VALUE).intValue(),
                pageCount, pageIndex);
        isHaveNextPage = nextPage == pageIndex ? 0 : 1;
        resultMap.put("rankList", socialRankVos);
        /* 当前用户*/
        if (userId != null) {
            Long currentUserRankL = redisService.kryoZRank(redisRankKey, userId);
            if (null != currentUserRankL) {
                Double currentUserScore = redisService.kryoZScore(redisRankKey, userId);
                UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
                currentUserRank.put("headImgUrl", userLoginVo.getHeadImgUrl());
                currentUserRank.put("nickName", userLoginVo.getNickName());

                if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                    List<Long> userList = redisService.kryoZRangeByScoreGet(redisRankKey, currentUserScore.longValue
                            (), currentUserScore.longValue(), Long.class);

                    if (null != userList && userList.size() > 1) {
                        List<Long> userIdsL = redisService.kryoZRange(redisRankKey, currentUserRankL - userList.size
                                (), currentUserRankL - 1, Long.class);
                        int count = 0;
                        for (int i = userIdsL.size() - 1; i >= 0; i--) {
                            Double sameScoreUser = redisService.kryoZScore(redisRankKey, userIdsL.get(i));
                            if (!sameScoreUser.equals(currentUserScore)) {
                                currentUserRankL = currentUserRankL - count;
                                break;
                            }
                            count++;
                        }
                    }
                }

                currentUserRank.put("rank", currentUserRankL.intValue() + 1);
                currentUserRank.put("userAwardDesc", getUserAwardDesc(rankType, String.valueOf(currentUserRankL
                        .intValue() + 1), 1, gameId));
                currentUserRank.put("userId", userLoginVo.getUserId() + "");
                currentUserRank.put("userScore", -currentUserScore.intValue());
                currentUserRank.put("isVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant
                        .VIP_MEMBER_TYPE_DIGIT));
            }
        }
        if (null == currentUserRank.get("headImgUrl")) {
            currentUserRank = null;
        }
        resultMap.put("currentUserRank", currentUserRank);
        resultMap.put("showText", getSocialRankShowText(lastOpenPeriod, rankType));
        resultMap.put("currentPage", pageIndex);
        resultMap.put("nextPage", nextPage);
        resultMap.put("isHaveNextPage", isHaveNextPage);
        resultMap.put("noMoreMsg", "只展示前100名");
        resultMap.put("isAwardFlag", getAwardText(gameId, lastOpenPeriod.getPeriodId(), rankType));
        return resultMap;
    }

    private Integer getAwardText(Long gameId, String periodId, String rankType) {
        Integer isShow = 0;
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_WEEK)) {
            if (getCurrentWeekId().equals(getWeekIdByDate(nextPeriod.getAwardTime()))) {
                isShow = 1;
            }
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            if (getCurrentMonthId().equals(getMonthIdByDate(nextPeriod.getAwardTime()))) {
                isShow = 1;
            }
        }
        return isShow;
    }

    private String getSocialRankShowText(GamePeriod lastOpenPeriod, String rankType) {
        StringBuffer showText = new StringBuffer();
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_PERIOD)) {
            showText.append(lastOpenPeriod.getPeriodId()).append("期");
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_WEEK)) {
            showText.append(DateUtil.formatDate(DateUtil.getBeginDayOfWeek(lastOpenPeriod.getAwardTime()), DateUtil
                    .DATE_FORMAT_M_D)).append("-").append(DateUtil.formatDate(DateUtil.getEndDayOfWeek(lastOpenPeriod
                    .getAwardTime()), DateUtil.DATE_FORMAT_M_D));
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            showText.append(DateUtil.getOnlyMonth(lastOpenPeriod.getAwardTime())).append("月");
        }
        return showText.toString();
    }

    /* 用户奖励文案*/
    private String getUserAwardDesc(String rankType, String rank, Integer type, Long gameId) {
        StringBuffer userAwardDesc = new StringBuffer();
        Map<String, Integer> predictNumsMap = new HashMap<>();
        Integer predictNums = 0;
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_PERIOD)) {
            predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant.SOCIAL_RANK_TYPE_PERIOD);
            predictNumsMap.putIfAbsent(rank, 1);
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_WEEK)) {
            userAwardDesc.append("下周每期");
            predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant.SOCIAL_RANK_TYPE_WEEK);
            if (type == CommonStatusEnum.YES.getStatus()) {
                userAwardDesc.append("\n");
            }
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            /* 上一期的下一个月*/
            GamePeriod lastPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
            userAwardDesc.append(DateUtil.getNextMonthMM(lastPeriod.getAwardTime())).append("月每期");
            predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant.SOCIAL_RANK_TYPE_MONTH);
            if (type == CommonStatusEnum.YES.getStatus()) {
                userAwardDesc.append("\n");
            }
        }

        if (type == CommonStatusEnum.YES.getStatus()) {
            userAwardDesc.append("智慧次数");
        }
        if (null != predictNumsMap.get(rank)) {
            predictNums = predictNumsMap.get(rank);
        }
        userAwardDesc.append("+").append(predictNums);
        if (type == CommonStatusEnum.NO.getStatus()) {
            userAwardDesc.append("次");
        }
        return userAwardDesc.toString();
    }

    /* 开奖号码更新后开始算奖*/
    @Override
    public void doDistribute() {
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            try {
                Game game = GameCache.getGame(gameId);
                if (null == game) {
                    log.error("doDistribute get game is null");
                    throw new BusinessException("doDistribute getgame is null");
                }
                /* 大盘彩*/
                if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                    if (game.getGameEn().equals(GameConstant.FC3D)) {
                        continue;
                    }
                    doDisTributeByGame(game);
                    updateUserAchievement(game.getGameId());
                }
            } catch (Exception e) {
                // // 报警 继续下一个彩种
                log.error("doDistribute error gameId is " + gameId, e);
            }
        }
    }

    /* 积分更新 奖励派发*/
    private void doDisTributeByGame(Game game) {
        /* 最近开奖期次*/
        List<GamePeriod> openPeriodList = PeriodRedis.getLastAwardPeriodByGameId(game.getGameId());
        GamePeriod openingPeriod = openPeriodList.get(0);
        /* 检查redis标志位*/
        String gameDistributeFlag = redisService.kryoGet(RedisConstant.getDisTributeFlag(openingPeriod.getGameId()),
                String.class);
        if (StringUtils.isNotBlank(gameDistributeFlag) && gameDistributeFlag.contains(openingPeriod.getPeriodId())) {
            return;
        }
        /* 检查period中是否有开奖号码*/
        if (StringUtils.isBlank(openingPeriod.getWinningNumbers())) {
            return;
        }
        /* 检查预测schedule中是否已经算奖*/
        SocialCodeSchedule socialCodeSchedule = socialCodeScheduleDao.getSocialCodeSchedule(game.getGameId(),
                openingPeriod.getPeriodId());
        /* 如果没有插入*/
        if (socialCodeSchedule == null) {
            socialCodeSchedule = new SocialCodeSchedule(game.getGameId(), openingPeriod.getPeriodId(), DateUtil
                    .getCurrentTimestamp());
            socialCodeScheduleDao.insert(socialCodeSchedule);
        }
        /* 如果数据库已经置位 设置缓存*/
        if (socialCodeSchedule.getIfDistribute() == CommonStatusEnum.YES.getStatus()) {
            redisService.kryoSet(RedisConstant.getDisTributeFlag(openingPeriod.getGameId()), openingPeriod
                    .getPeriodId());
            return;
        }
        /* 围号计算*/
        analysisEncircleAward(game, openingPeriod);
        /* 杀号计算*/
        analysisKillAward(game, openingPeriod);

        /* 本期未参与用户*/
        updateCurrentWeekRanks(game.getGameId(), getCurrentWeekId());
        updateCurrentMonthRanks(game.getGameId(), getCurrentMonthId());
        /* 置位*/
        socialCodeScheduleDao.updateSocialCodeSchedule(openingPeriod.getGameId(), openingPeriod.getPeriodId(),
                "IF_DISTRIBUTE", "IF_DISTRIBUTE_TIME");
        redisService.kryoSet(RedisConstant.getDisTributeFlag(openingPeriod.getGameId()), openingPeriod.getPeriodId());
        /* 计算开奖期sortSet*/
        socialEncircleCodeService.updateKillNumList(game.getGameId(), openingPeriod.getPeriodId());

        String markdown = "#### 社区服务 \n" + "> 排行榜已更新，速速查看\n" + "> ###### " + DateUtil.formatNowTime
                (15) + "发布 \n";
        List<String> at = new ArrayList<>();
        dingTalkRobotService.sendMassageToAll("社区服务", markdown, at);
    }

    private void analysisEncircleAward(Game game, GamePeriod period) {
        try {
            /* 围号*/
            /* 获取本期发布号码的列表*/
            // 2017/10/19 计算状态字段
            List<SocialEncircle> socialEncircleList = socialEncircleCodeDao.getUnDistributeSocialEncircle(game
                    .getGameId(), period.getPeriodId());
            // 2017/10/19 size >5000 报警
            if (socialEncircleList.size() > 5000) {
                log.error("本期围号记录超过5000");
            }

            Map<String, Integer> awardLevelMap = getAwardLevelMap(game.getGameId(), CommonConstant
                    .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE);
            for (SocialEncircle socialEncircle : socialEncircleList) {

                Integer rightNums = 0;
                /* 红球 篮球 计算中几个*/
                String redBalls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
                /* 红球*/
                if (socialEncircle.getCodeType() == CommonConstant.RED_BALL_TYPE) {
                    if (null == socialEncircle.getIsDistribute() || socialEncircle.getIsDistribute() !=
                            CommonStatusEnum.YES.getStatus()) {
                        socialEncircle.setUserEncircleCode(socialEncircle.getUserEncircleCode().replaceAll
                                (CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR, ""));
                        for (String redBall : redBalls.split(CommonConstant.SPACE_SPLIT_STR)) {
                            if (socialEncircle.getUserEncircleCode().contains(redBall)) {
                                socialEncircle.setUserEncircleCode(socialEncircle.getUserEncircleCode().replaceAll
                                        (redBall, CommonConstant.COMMON_STAR_STR + redBall));
                                rightNums++;
                            }
                        }
                    }
                    /* 更新命中*/
                    socialEncircle.setRightNums(rightNums);
                    /* 更新积分*/
                    Integer userRankScore = awardLevelMap.get(socialEncircle.getEncircleNums() + CommonConstant
                            .COMMON_COLON_STR + rightNums);
                    if (null == userRankScore) {
                        userRankScore = 0;
                    }
                    /* 用户积分更新*/
                    socialEncircle.setUserAwardScore(userRankScore);

                    /* 判断是否经典*/
                    socialClassicEncircleCodeService.saveSocialClassicEncircle(socialEncircle);

                    /* 检查是否已经更新过积分*/
                    if (null == socialEncircle.getIsDistribute() || socialEncircle.getIsDistribute() != CommonStatusEnum
                            .YES.getStatus()) {
                        updateEncircleUserRanks(game.getGameId(), period.getPeriodId(), socialEncircle);
                    }
                }
                // 蓝球 补充
            }

            distributeEnCirclePredictNums(game.getGameId(), period.getPeriodId());
        } catch (Exception e) {
            log.error("analysisAward error gameId:" + game.getGameEn() + period.getPeriodId(), e);
            throw new BusinessException(" analysisAward error gameId:" + game.getGameEn() + period.getPeriodId() + e);
        }
    }

    private void analysisKillAward(Game game, GamePeriod period) {
        try {
            /* sha号*/
            /* 获取本期发布号码的列表*/
            List<SocialKillCode> socialKillCodeList = socialKillCodeDao.getUnDistributeKillNums(game
                    .getGameId(), period.getPeriodId());
            if (socialKillCodeList.size() > 5000) {
                log.error("本期围号记录超过5000");
            }
            for (SocialKillCode socialKillCode : socialKillCodeList) {

                Integer rightNums = 0;
                /* 红球 篮球 计算中几个*/
                String redBalls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
                /* 红球*/
                if (socialKillCode.getCodeType() == CommonConstant.RED_BALL_TYPE) {
                    if (null == socialKillCode.getIsDistribute() || socialKillCode.getIsDistribute() !=
                            CommonStatusEnum.YES.getStatus()) {
                        socialKillCode.setUserKillCode(socialKillCode.getUserKillCode().replaceAll(CommonConstant
                                .COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR, ""));
                        for (String killBall : socialKillCode.getUserKillCode().split(CommonConstant.COMMA_SPLIT_STR)) {
                            if (!redBalls.contains(killBall)) {
                                socialKillCode.setUserKillCode(socialKillCode.getUserKillCode().replaceAll
                                        (killBall, CommonConstant.COMMON_STAR_STR + killBall));

                                rightNums++;
                            }
                        }
                    }
                    /* 更新命中*/
                    socialKillCode.setRightNums(rightNums);
                    Map<String, Integer> awardLevelMap = getAwardLevelMap(game.getGameId(), CommonConstant
                            .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
                    Integer userRankScore = awardLevelMap.get(socialKillCode.getKillNums() + CommonConstant
                            .COMMON_COLON_STR + socialKillCode.getRightNums());
                    if (null == userRankScore) {
                        userRankScore = awardLevelMap.get(socialKillCode.getKillNums() + CommonConstant
                                .COMMON_COLON_STR + CommonStatusEnum.NO.getStatus());
                    }

                    if (null == userRankScore) {
                        userRankScore = 0;
                    }
                    /* 用户积分更新*/
                    socialKillCode.setUserAwardScore(userRankScore);
                    /* 检查是否已经更新过积分*/
                    if (null == socialKillCode.getIsDistribute() || socialKillCode.getIsDistribute() != CommonStatusEnum
                            .YES.getStatus()) {
                        updateKillUserRanks(game.getGameId(), period.getPeriodId(), socialKillCode);
                    }
                }
                // 蓝球 补充
            }
            distributeKillPredictNums(game.getGameId(), period.getPeriodId());
        } catch (Exception e) {
            log.error("analysisAward error gameId:" + game.getGameEn() + period.getPeriodId(), e);
            throw new BusinessException(" analysisAward error gameId:" + game.getGameEn() + period.getPeriodId() + e);
        }

    }

    /* 用户围号积分更新*/
    private void updateEncircleUserRanks(Long gameId, String periodId, SocialEncircle socialEncircle) {
        Long userId = socialEncircle.getUserId();
        Integer userAwardScore = socialEncircle.getUserAwardScore();

        /* 更新用户积分 命中 号码*/
        socialEncircleCodeDao.updateSocialEncircle(socialEncircle);
        indexUserSocialCodeDao.updateRightNums(gameId, socialEncircle.getUserId(), periodId,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, socialEncircle.getEncircleCodeId(),
                socialEncircle.getRightNums(), null);

        /* 更新期次排行榜积分*/
        SocialRank enPeriodRank = new SocialRank(userId, gameId, periodId, getCurrentWeekId(),
                getCurrentMonthId(), userAwardScore, periodId, DateUtil.getCurrentTimestamp(), DateUtil
                .getCurrentTimestamp());

        /* 设置排行榜缓存 期榜 周榜 月榜*/
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime()) +
                (60 * 60 * 24);
        String periodRankKey = RedisConstant.getEncirclePeriodRank(gameId, periodId);
        String weekRankKey = RedisConstant.getEncircleWeekRank(gameId, getCurrentWeekId());
        String monthRankKey = RedisConstant.getEncircleMonthRank(gameId, getCurrentMonthId());

        SocialRank periodRank = socialEncirclePeriodRankDao.getSocialEncirclePeriodRankByUserId(gameId,
                periodId, userId, Boolean.FALSE);
        SocialRank weekRank = socialEncircleWeekRankDao.getSocialEncircleWeekRankByUserId(gameId, getCurrentWeekId(),
                userId, Boolean.FALSE);
        SocialRank monthRank = socialEncircleMonthRankDao.getSocialEncircleMonthRankByUserId(gameId,
                getCurrentMonthId(),
                userId, Boolean.FALSE);
        Integer[] userWeekMonthScore = updateUserRankToRedis(userId, periodRankKey, weekRankKey, monthRankKey,
                userAwardScore, expireSeconds, periodRank, weekRank, monthRank, periodId);

        self.updateSocialEncircleRankDao(userId, gameId, periodId, enPeriodRank, userWeekMonthScore[0],
                userWeekMonthScore[1], userWeekMonthScore[2]);
        /* 算奖标志位*/
        socialEncircleCodeDao.updateToDistribute(socialEncircle.getEncircleCodeId(), periodId);

    }

    /* 更新数据库*/
    @Transactional
    public void updateSocialEncircleRankDao(Long userId, Long gameId, String periodId, SocialRank enPeriodRank,
                                            Integer periodScore, Integer userWeekScore, Integer userMonthScore) {
        /* 更新排行榜数据库*/
        /* 期榜*/
        SocialRank enPeriodRankDao = socialEncirclePeriodRankDao.getSocialEncirclePeriodRankByUserId(gameId,
                periodId, enPeriodRank.getUserId(), Boolean.TRUE);
        if (null == enPeriodRankDao) {
            socialEncirclePeriodRankDao.insert(enPeriodRank);
        } else {
            socialEncirclePeriodRankDao.updateSocialEncirclePeriodRank(gameId, periodId, userId, periodScore);
        }

        /* 周榜*/
        SocialRank enWeekRank = socialEncircleWeekRankDao.getSocialEncircleWeekRankByUserId(gameId, getCurrentWeekId
                (), userId, Boolean.FALSE);
        if (null == enWeekRank) {
            socialEncircleWeekRankDao.insert(enPeriodRank);
        } else {
            socialEncircleWeekRankDao.updateSocialEncircleWeekRank(gameId, getCurrentWeekId(), userId, userWeekScore);
        }

        /* 月榜*/
        SocialRank enMonthRank = socialEncircleMonthRankDao.getSocialEncircleMonthRankByUserId(gameId,
                getCurrentMonthId(), userId, Boolean.FALSE);
        if (null == enMonthRank) {
            socialEncircleMonthRankDao.insert(enPeriodRank);
        } else {
            socialEncircleMonthRankDao.updateSocialEncircleMonthRank(gameId, getCurrentMonthId(), userId,
                    userMonthScore);
        }
    }

    @Transactional
    public void updateSocialKillDao(Long userId, Long gameId, String periodId, SocialRank enPeriodRank,
                                    Integer periodScore, Integer userWeekScore, Integer userMonthScore) {
        /* 更新排行榜数据库*/
        /* 期榜 锁期榜 */

        SocialRank periodRankDao = socialKillPeriodRankDao.getSocialKillPeriodRankByUserId(gameId,
                periodId, enPeriodRank.getUserId(), Boolean.TRUE);
        if (null == periodRankDao) {
            socialKillPeriodRankDao.insert(enPeriodRank);
        } else {
            socialKillPeriodRankDao.updateSocialKillPeriodRank(gameId, periodId, userId, periodScore);
        }


        /* 周榜*/
        SocialRank weekRank = socialKillWeekRankDao.getSocialKillWeekRankByUserId(gameId, getCurrentWeekId
                (), userId, Boolean.FALSE);
        if (null == weekRank) {
            socialKillWeekRankDao.insert(enPeriodRank);
        } else {
            socialKillWeekRankDao.updateSocialKillWeekRank(gameId, getCurrentWeekId(), userId, userWeekScore);
        }

        /* 月榜*/
        SocialRank monthRank = socialKillMonthRankDao.getSocialKillMonthRankByUserId(gameId,
                getCurrentMonthId(), userId, Boolean.FALSE);
        if (null == monthRank) {
            socialKillMonthRankDao.insert(enPeriodRank);
        } else {
            socialKillMonthRankDao.updateSocialKillMonthRank(gameId, getCurrentMonthId(), userId,
                    userMonthScore);
        }
    }

    /* 围号 预测次数发放*/
    public void distributeEnCirclePredictNums(Long gameId, String periodId) {
        /* 排行榜奖励预测次数*/
        /* 期次排行榜*/  //  // TODO: 2017/10/19 >5000 报警 发布围号 发放奖励 TODO: 2017/10/19 redis set 已经派发名单
        Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                .SOCIAL_RANK_TYPE_PERIOD);
        String periodRankKey = RedisConstant.getEncirclePeriodRank(gameId, periodId);
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        List<Long> pUserIdList = redisService.kryoZRange(periodRankKey, 0L, 9L, Long.class);
        updateUserPredictNums(gameId, nextPeriod.getPeriodId(), pUserIdList, periodRankKey, predictNumsMap,
                CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE);

        /* 周排行榜 只有当下一期不在本周时更新奖励为给下一周奖励*/

        if (getCurrentWeekId() != getWeekIdByDate(nextPeriod.getAwardTime())) {
            Map<String, Integer> weekPredictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                    .SOCIAL_RANK_TYPE_WEEK);
            String weekRankKey = RedisConstant.getEncircleWeekRank(gameId, periodId);
            List<Long> wUserIdList = redisService.kryoZRange(weekRankKey, 0L, 9L, Long.class);
            /* 每周最多三期*/
            GamePeriod distributePeriod = nextPeriod;
            for (int i = 0; i < 3; i++) {
                if (getWeekIdByDate(nextPeriod.getAwardTime()) == getWeekIdByDate(distributePeriod.getAwardTime())) {
                    //  2017/10/20 原子加
                    updateUserPredictNums(gameId, nextPeriod.getPeriodId(), wUserIdList, weekRankKey,
                            weekPredictNumsMap,
                            CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE);
                } else {
                    break;
                }
                distributePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, distributePeriod.getPeriodId());
            }
        }

        /* 月排行榜 只有当下一期不在本月时更新奖励为给下一月奖励*/
        if (getCurrentMonthId() != getMonthIdByDate(nextPeriod.getAwardTime())) {
            Map<String, Integer> mPredictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                    .SOCIAL_RANK_TYPE_MONTH);
            String mPeriodRankKey = RedisConstant.getEncirclePeriodRank(gameId, periodId);
            List<Long> mUserIdList = redisService.kryoZRange(mPeriodRankKey, 0L, 9L, Long.class);
            /* 每月最多14期*/
            GamePeriod distributePeriod = nextPeriod;
            for (int i = 0; i < 14; i++) {
                if (getMonthIdByDate(nextPeriod.getAwardTime()) == getMonthIdByDate(distributePeriod.getAwardTime())) {
                    updateUserPredictNums(gameId, nextPeriod.getPeriodId(), mUserIdList, mPeriodRankKey,
                            mPredictNumsMap,
                            CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE);
                } else {
                    break;
                }
                distributePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, distributePeriod.getPeriodId());
            }
        }
    }

    /* 杀号 预测次数发放*/
    @Override
    public void distributeKillPredictNums(Long gameId, String periodId) {
        /* 排行榜奖励预测次数*/
        /* 期次排行榜*/
        String userRankUpdateFlag = redisService.kryoGet(RedisConstant.USER_RANK_UPDATE_FLAG, String.class);
        if (null != userRankUpdateFlag && userRankUpdateFlag.equals(periodId)) {
            return;
        }
        redisService.kryoSet(RedisConstant.USER_RANK_UPDATE_FLAG, periodId);
        Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                .SOCIAL_RANK_TYPE_PERIOD);
        String periodRankKey = RedisConstant.getKillPeriodRank(gameId, periodId);
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        List<Long> pUserIdList = redisService.kryoZRange(periodRankKey, 0L, 9L, Long.class);
        if (pUserIdList.size() >= 10) {
            Long scoreTen = redisService.kryoZRank(periodRankKey, pUserIdList.get(9));
            List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(periodRankKey, scoreTen, scoreTen, Long.class);
            pUserIdList.addAll(repeatPUser);
        }
        updateUserPredictNums(gameId, nextPeriod.getPeriodId(), pUserIdList, periodRankKey, predictNumsMap,
                CommonConstant.SOCIAL_CODE_TYPE_KILL);

        /* 周排行榜 只有当下一期不在本周时更新奖励为给下一周奖励*/

        if (getCurrentWeekId() != getWeekIdByDate(nextPeriod.getAwardTime())) {
            Map<String, Integer> weekPredictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                    .SOCIAL_RANK_TYPE_WEEK);
            String weekRankKey = RedisConstant.getKillWeekRank(gameId, periodId);
            List<Long> wUserIdList = redisService.kryoZRange(weekRankKey, 0L, 9L, Long.class);
            if (wUserIdList.size() >= 10) {
                Long scoreTen = redisService.kryoZRank(weekRankKey, wUserIdList.get(9));
                List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(weekRankKey, scoreTen, scoreTen, Long.class);
                wUserIdList.addAll(repeatPUser);
            }

            /* 每周最多三期*/
            GamePeriod distributePeriod = nextPeriod;
            for (int i = 0; i < 3; i++) {
                if (getWeekIdByDate(nextPeriod.getAwardTime()) == getWeekIdByDate(distributePeriod.getAwardTime())) {
                    updateUserPredictNums(gameId, nextPeriod.getPeriodId(), wUserIdList, weekRankKey,
                            weekPredictNumsMap,
                            CommonConstant.SOCIAL_CODE_TYPE_KILL);
                } else {
                    break;
                }
                distributePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, distributePeriod.getPeriodId());
            }
        }
        /* 月排行榜 只有当下一期不在本月时更新奖励为给下一月奖励*/
        if (getCurrentMonthId() != getMonthIdByDate(nextPeriod.getAwardTime())) {
            Map<String, Integer> mPredictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                    .SOCIAL_RANK_TYPE_MONTH);
            String mPeriodRankKey = RedisConstant.getKillPeriodRank(gameId, periodId);
            List<Long> mUserIdList = redisService.kryoZRange(mPeriodRankKey, 0L, 9L, Long.class);

            if (mUserIdList.size() >= 10) {
                Long scoreTen = redisService.kryoZRank(mPeriodRankKey, mUserIdList.get(9));
                List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(mPeriodRankKey, scoreTen, scoreTen, Long
                        .class);
                mUserIdList.addAll(repeatPUser);
            }

            /* 每月最多14期*/
            GamePeriod distributePeriod = nextPeriod;
            for (int i = 0; i < 14; i++) {
                if (getMonthIdByDate(nextPeriod.getAwardTime()) == getMonthIdByDate(distributePeriod.getAwardTime())) {
                    updateUserPredictNums(gameId, nextPeriod.getPeriodId(), mUserIdList, mPeriodRankKey,
                            mPredictNumsMap,
                            CommonConstant.SOCIAL_CODE_TYPE_KILL);
                } else {
                    break;
                }
                distributePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, distributePeriod.getPeriodId());
            }
        }
    }

    private void updateUserPredictNums(Long gameId, String periodId, List<Long> pUserIdList, String periodRankKey,
                                       Map<String, Integer> predictNumsMap, String socialType) {
        List<Long> rankList = new ArrayList<>();
        List<Long> scoreList = new ArrayList<>();
        for (Long userId : pUserIdList) {
            Long rank = redisService.kryoZRank(periodRankKey, userId);
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                Long score = redisService.kryoZScore(periodRankKey, userId).longValue();
                if (rankList.size() > 0 && score.equals(scoreList.get(scoreList.size() - 1))) {
                    rank = rankList.get(rankList.size() - 1);
                }
                scoreList.add(score);
                rankList.add(rank);
            }
            Integer predictNums = predictNumsMap.get(String.valueOf(rank + 1));
            if (null == predictNumsMap.get(String.valueOf(rank + 1))) {
                continue;
            }
            predictNumService.updateUserPredictMaxNums(gameId, periodId, userId, predictNums);
        }
    }

    /* 用户杀号积分更新*/
    private void updateKillUserRanks(Long gameId, String periodId, SocialKillCode socialKillCode) {
        Long userId = socialKillCode.getUserId();
        Integer userAwardScore = socialKillCode.getUserAwardScore();
        /* 更新用户积分 命中 命中号码*/
        socialKillCodeDao.updateSocialKillCode(socialKillCode);
        Integer awardCount = socialKillCode.getRightNums();
        indexUserSocialCodeDao.updateRightNums(gameId, socialKillCode.getUserId(), periodId,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, socialKillCode.getKillCodeId(), awardCount,
                null);

        /* 更新期次排行榜积分*/
        SocialRank enPeriodRank = new SocialRank(userId, gameId, periodId, getCurrentWeekId(),
                getCurrentMonthId(), userAwardScore, periodId, DateUtil.getCurrentTimestamp(), DateUtil
                .getCurrentTimestamp());

        /* 设置排行榜缓存 期榜 周榜 月榜*/
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime()) +
                (60 * 60 * 24);
        String periodRankKey = RedisConstant.getKillPeriodRank(gameId, periodId);
        String weekRankKey = RedisConstant.getKillWeekRank(gameId, getCurrentWeekId());
        String monthRankKey = RedisConstant.getKillMonthRank(gameId, getCurrentMonthId());

        SocialRank periodRank = socialKillPeriodRankDao.getSocialKillPeriodRankByUserId(gameId, periodId,
                userId, Boolean.FALSE);

        SocialRank weekRank = socialKillWeekRankDao.getSocialKillWeekRankByUserId(gameId, getCurrentWeekId(),
                userId, Boolean.FALSE);

        SocialRank monthRank = socialKillMonthRankDao.getSocialKillMonthRankByUserId(gameId,
                getCurrentMonthId(), userId, Boolean.FALSE);

        Integer[] userWeekMonthScore = updateUserRankToRedis(userId, periodRankKey, weekRankKey, monthRankKey,
                userAwardScore, expireSeconds, periodRank, weekRank, monthRank, periodId);

        self.updateSocialKillDao(userId, gameId, periodId, enPeriodRank, userWeekMonthScore[0],
                userWeekMonthScore[1], userWeekMonthScore[2]);

        /*  已经算奖标志位*/
        socialKillCodeDao.updateToDistribute(socialKillCode.getKillCodeId(), periodId);
    }

    private Integer[] updateUserRankToRedis(Long userId, String periodRankKey, String weekRankKey, String MonthRankKey,
                                            Integer userAwardScore, Integer expireSeconds, SocialRank periodRank,
                                            SocialRank weekRank, SocialRank monthRank, String periodId) {
        Integer periodRankScore = userAwardScore;
        Integer weekRankScore = userAwardScore;
        Integer monthRankScore = userAwardScore;

        if (null != periodRank) {
            periodRankScore = periodRank.getUserScore() + userAwardScore;
        }
        if (null != weekRank) {
            weekRankScore = weekRank.getUserScore() + userAwardScore;
        }
        if (null != monthRank) {
            monthRankScore = monthRank.getUserScore() + userAwardScore;
        }
        Integer[] userWeekMonthScore = new Integer[]{periodRankScore, weekRankScore, monthRankScore};

        redisService.kryoZAddSet(periodRankKey, new Long(-periodRankScore),
                userId);
        /* 过期时间*/
        redisService.expire(periodRankKey, expireSeconds);

        /* 周榜*/

        userWeekMonthScore[1] = weekRankScore;

        redisService.kryoZAddSet(weekRankKey, new Long(-userWeekMonthScore[0]), userId);

        redisService.expire(weekRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                Timestamp(DateUtil.getEndDayOfWeek(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24);

        /* 月榜*/

        userWeekMonthScore[2] = monthRankScore;

        redisService.kryoZAddSet(MonthRankKey, new Long(-userWeekMonthScore[1]), userId);

        redisService.expire(MonthRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                Timestamp(DateUtil.getEndDayOfMonth(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24);

        return userWeekMonthScore;
    }

    /* 获取当前周ID*/
    private String getCurrentWeekId() {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYearOfCurrentDay() - 1)
                .toString();
    }

    /* 获取当前月ID*/
    private String getCurrentMonthId() {
        return new StringBuffer().append(DateUtil.getCurrentMonth()).toString();
    }

    /* 获取指定周ID*/
    private String getWeekIdByDate(Date date) {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYear(date) - 1)
                .toString();
    }

    /* 获取指定月ID*/
    private String getMonthIdByDate(Date date) {
        return new StringBuffer().append(DateUtil.getMonth(new Timestamp(date.getTime()))).toString();
    }

    public Integer getNextPage(Integer resultNum, Integer pageCount, Integer pageIndex) {
        if (resultNum > ((pageIndex + 1) * pageCount)) {
            pageIndex++;
        }
        return pageIndex;
    }

    @Override
    public Map<String, Integer> getAwardLevelMap(Long gameId, Integer ballType, String socialType) {
        Map<String, Integer> awardLevelMap = new HashMap<>();
        if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {
            List<SocialEncircleAwardLevel> enAwardLevelRedBalls = socialEncircleAwardLevelDao
                    .getSocialEncircleAwardLevel(gameId, ballType);
            for (SocialEncircleAwardLevel s : enAwardLevelRedBalls) {
                awardLevelMap.put(s.getEncircleNums() + CommonConstant.COMMON_COLON_STR + s.getRightNums(), s
                        .getRankScore
                                ());
            }
        }
        if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
            List<SocialKillAwardLevel> socialKillAwardLevels = socialKillAwardLevelDao
                    .getSocialKillAwardLevel(gameId, ballType);
            for (SocialKillAwardLevel s : socialKillAwardLevels) {
                if (s.getRightNums() == null) {
                    continue;
                }
                awardLevelMap.put(s.getKillNums() + CommonConstant.COMMON_COLON_STR + s.getRightNums(), s
                        .getRankScore());
            }

        }
        return awardLevelMap;
    }

    @Override
    public void setFivePredictNums(Long gameId, String periodId) {
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        /* sha hao */
        String periodRankKey = RedisConstant.getKillPeriodRank(gameId, periodId);
        List<Long> pUserIdList = redisService.kryoZRange(periodRankKey, 0L, -1L, Long.class);

        /* weihao */
        String enPeriodRankKey = RedisConstant.getEncirclePeriodRank(gameId, periodId);
        List<Long> enUserIdList = redisService.kryoZRange(enPeriodRankKey, 0L, -1L, Long.class);

        for (Long userId : pUserIdList) {
            String predictMaxNumbsKey = RedisConstant.getPredictMaxNumsKey(gameId, nextPeriod.getPeriodId(), userId);
            int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime
                    ());
            redisService.kryoSetEx(predictMaxNumbsKey, expireSeconds, 5);
        }
        for (Long userId : enUserIdList) {
            String predictMaxNumbsKey = RedisConstant.getPredictMaxNumsKey(gameId, nextPeriod.getPeriodId(), userId);
            int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime
                    ());
            redisService.kryoSetEx(predictMaxNumbsKey, expireSeconds, 5);
        }

    }

    /* 更新当期周榜*/
    private void updateCurrentWeekRanks(Long gameId, String weekId) {
        String enWeekRankKey = RedisConstant.getEncircleWeekRank(gameId, weekId);
        List<SocialRank> SocialRanks = socialEncircleWeekRankDao.getSocialEncircleWeekRank(gameId, weekId);
        for (SocialRank socialRank : SocialRanks) {
            redisService.kryoZAddSet(enWeekRankKey, -new Long(socialRank.getUserScore()), socialRank.getUserId());
            redisService.expire(enWeekRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                    Timestamp(DateUtil.getEndDayOfWeek(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24 * 30);
        }

        String killWeekRankKey = RedisConstant.getKillWeekRank(gameId, weekId);
        List<SocialRank> SocialKillRanks = socialKillWeekRankDao.getSocialKillWeekRank(gameId, weekId);
        for (SocialRank socialRank : SocialKillRanks) {
            redisService.kryoZAddSet(killWeekRankKey, -new Long(socialRank.getUserScore()), socialRank.getUserId());
            redisService.expire(killWeekRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                    Timestamp(DateUtil.getEndDayOfWeek(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24 * 30);
        }
    }

    /* 更新当期月榜*/
    private void updateCurrentMonthRanks(Long gameId, String monthId) {
        String enMonthRankKey = RedisConstant.getEncircleMonthRank(gameId, monthId);
        List<SocialRank> SocialRanks = socialEncircleMonthRankDao.getSocialEncircleMonthRank(gameId, monthId);
        for (SocialRank socialRank : SocialRanks) {
            redisService.kryoZAddSet(enMonthRankKey, -new Long(socialRank.getUserScore()), socialRank.getUserId());
            redisService.expire(enMonthRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                    Timestamp(DateUtil.getEndDayOfMonth(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24);
        }

        String killMonthRankKey = RedisConstant.getKillMonthRank(gameId, monthId);
        List<SocialRank> SocialKillRanks = socialKillMonthRankDao.getSocialKillMonthRank(gameId, monthId);
        for (SocialRank socialRank : SocialKillRanks) {
            redisService.kryoZAddSet(killMonthRankKey, -new Long(socialRank.getUserScore()), socialRank.getUserId());
            redisService.expire(killMonthRankKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new
                    Timestamp(DateUtil.getEndDayOfMonth(DateUtil.getCurrentTimestamp()).getTime())) + 60 * 60 * 24);
        }
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (SocialService) proxyBean;
    }

    @Override
    public void updateUserAchievement(long gameId) {
        /* 最近开奖期次*/
        List<GamePeriod> openPeriodList = PeriodRedis.getLastAwardPeriodByGameId(gameId);
        GamePeriod openingPeriod = openPeriodList.get(0);
        /* 检查redis标志位*/
        String gameAchieveFlag = redisService.kryoGet(RedisConstant.getAchievementFlag(openingPeriod.getGameId()),
                String.class);
        if (openingPeriod.getPeriodId().equals(gameAchieveFlag)) {
            return;
        }
        /* 检查预测schedule中是否已经算奖*/
        SocialCodeSchedule socialCodeSchedule = socialCodeScheduleDao.getSocialCodeSchedule(gameId, openingPeriod
                .getPeriodId());
        if (socialCodeSchedule == null || socialCodeSchedule.getIfDistribute() == CommonStatusEnum.NO.getStatus()) {
            return;
        }
        updateUserAchievement(gameId, openingPeriod.getPeriodId());
        redisService.kryoSet(RedisConstant.getAchievementFlag(openingPeriod.getGameId()), openingPeriod.getPeriodId());
        Map<String, Integer> socialKillAwardLevel = getAwardLevelMap(gameId, CommonConstant.RED_BALL_TYPE,
                CommonConstant.SOCIAL_CODE_TYPE_KILL);
        socialClassicEncircleCodeService.saveClassicEncircle2Redis(gameId, openingPeriod.getPeriodId(),
                socialKillAwardLevel);
    }

    //重构用户名片展示数据
    @Override
    public Map<String, List<AchievementVo>> rebuildUserSocial2Redis(long gameId, Long userId, String periodId) {
        List<Map> userSocialIndex = null;
        List<UserSocialRecord> userAchieve = null;
        Map<String, List<AchievementVo>> result = new HashMap();
        Map<String, List<AchievementVo>> achieveMap = null;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)) {
            for (SsqAchievementEnum achievementEnum : SsqAchievementEnum.values()) {
                Integer total = achievementEnum.getRecentAchieveCount() * achievementEnum.getMaxSocialTimes();
                userSocialIndex = indexUserSocialCodeService.getRecentOpenedSocialIndex(gameId, userId, total,
                        achievementEnum.getSocialType());
                userAchieve = userSocialRecordService.getUserLastestSocialRecords(gameId, userId, achievementEnum
                        .getSocialType());

                achieveMap = achievementEnum.makeAchievementVo(userSocialIndex, userAchieve);
                if (achieveMap != null && achieveMap.size() > 0) {
                    result.putAll(achieveMap);
                }
                achieveMap.clear();
                userAchieve.clear();
                userSocialIndex.clear();
            }
        }
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            for (DltAchievementEnum achievementEnum : DltAchievementEnum.values()) {
                Integer total = achievementEnum.getRecentAchieveCount() * achievementEnum.getMaxSocialTimes();
                userSocialIndex = indexUserSocialCodeService.getRecentOpenedSocialIndex(gameId, userId, total,
                        achievementEnum.getSocialType());
                userAchieve = userSocialRecordService.getUserLastestSocialRecords(gameId, userId, achievementEnum
                        .getSocialType());

                achieveMap = achievementEnum.makeAchievementVo(userSocialIndex, userAchieve);
                if (achieveMap != null && achieveMap.size() > 0) {
                    result.putAll(achieveMap);
                }
                achieveMap.clear();
                userAchieve.clear();
                userSocialIndex.clear();
            }
        }
        if (result.size() > 0) {
            String redisKeys = RedisConstant.getPersionalAchieveKey(gameId, userId);
            GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
            int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 1800);
            redisService.kryoSetEx(redisKeys, expireTime, result);
        }
        return result;
    }

    //更新用户成就
    @Override
    public void updateUserAchievement(long gameId, String periodId) {
        //获取期榜拿到杀号和圈号人id
        Set<Long> userIds = new HashSet<>();
        List<Long> encircleRank = socialEncirclePeriodRankDao.getSocialEncirclePeriodUserId(gameId, periodId);
        List<Long> killRank = socialKillPeriodRankDao.getSocialKillPeriodUserId(gameId, periodId);
        userIds.addAll(encircleRank);
        userIds.addAll(killRank);
        //围号计算
        saveAchieveMent2Db(gameId, periodId, userIds);
    }


    /* 奖励*/
    @Override
    public List<Map<String, String>> awardPopup(Game game, Long userId) {
        List<Map<String, String>> resultList = new ArrayList<>();

        /* 上期是上月的最后一期*/
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());

        String popupFlagKey = RedisConstant.getAwardPopupFlag(game.getGameId(), lastOpenPeriod.getPeriodId(), userId);
        String popupFlag = redisService.kryoGet(popupFlagKey, String.class);
        if (StringUtils.isNotBlank(popupFlag)) {
            return resultList;
        }
        redisService.kryoSetEx(popupFlagKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                currentPeriod.getAwardTime()), String.valueOf(CommonStatusEnum.YES.getStatus()));

        if (!getMonthIdByDate(lastOpenPeriod.getAwardTime()).equals(getMonthIdByDate(currentPeriod.getAwardTime()))) {
            /* 进入围号榜和杀号榜才有*/
            Boolean inMonthRank = Boolean.FALSE;
            Integer awardTimes = 0;
            String killRank = "";
            String enRank = "";
            String mKillMonthRankKey = RedisConstant.getKillMonthRank(game.getGameId(), getMonthIdByDate
                    (lastOpenPeriod.getAwardTime()));
            List<Long> mKillUserIdList = redisService.kryoZRange(mKillMonthRankKey, 0L, 9L, Long.class);

            if (mKillUserIdList.size() >= 10) {
                Double scoreTen = redisService.kryoZScore(mKillMonthRankKey, mKillUserIdList.get(9));
                List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(mKillMonthRankKey, scoreTen.longValue(),
                        scoreTen.longValue(), Long.class);
                mKillUserIdList.addAll(repeatPUser);
            }

            String enMonthRankKey = RedisConstant.getEncircleMonthRank(game.getGameId(), getMonthIdByDate
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
                awardPopupAddToAwardResultList(resultList, awardTimes, killRank + enRank, "month", game.getGameId());
            }
        }
        if (!getWeekIdByDate(lastOpenPeriod.getAwardTime()).equals(getWeekIdByDate(currentPeriod.getAwardTime()))) {
            /* 进入围号榜和杀号榜才有*/
            Boolean inWeekRank = Boolean.FALSE;
            Integer awardTimes = 0;
            String killRank = "";
            String enRank = "";
            String killWeekRankKey = RedisConstant.getKillWeekRank(game.getGameId(), getWeekIdByDate(lastOpenPeriod
                    .getAwardTime()));
            List<Long> killUserIdList = redisService.kryoZRange(killWeekRankKey, 0L, 9L, Long.class);

            if (killUserIdList.size() >= 10) {
                Double scoreTen = redisService.kryoZScore(killWeekRankKey, killUserIdList.get(9));
                List<Long> repeatPUser = redisService.kryoZRangeByScoreGet(killWeekRankKey, scoreTen.longValue(),
                        scoreTen.longValue(), Long.class);
                killUserIdList.addAll(repeatPUser);
            }

            String enWeekRankKey = RedisConstant.getEncircleWeekRank(game.getGameId(), getWeekIdByDate(lastOpenPeriod
                    .getAwardTime()));
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
                awardPopupAddToAwardResultList(resultList, awardTimes, killRank + enRank, "week", game.getGameId());
            }
        }
        /* 期榜*/
        Boolean inPeriodRank = Boolean.FALSE;
        Integer awardTimes = 0;
        String killRank = "";
        String enRank = "";
        String killPeriodRankKey = RedisConstant.getKillPeriodRank(game.getGameId(), lastOpenPeriod.getPeriodId());
        List<Long> killUserIdList = redisService.kryoZRange(killPeriodRankKey, 0L, -1L, Long.class);

        String enPeriodRankKey = RedisConstant.getEncirclePeriodRank(game.getGameId(), lastOpenPeriod
                .getPeriodId());
        List<Long> enUserIdList = redisService.kryoZRange(enPeriodRankKey, 0L, -1L, Long.class);

        Map<String, Integer> predictNumsMap = SocialEncircleKillCodeUtil.getPredictNumsMap(CommonConstant
                .SOCIAL_RANK_TYPE_PERIOD);
        if (killUserIdList.contains(userId)) {
            inPeriodRank = Boolean.TRUE;
            Map<String, Object> socialRankList = getSocialRankList(game.getGameId(), userId, "kill",
                    "period", null);
            Map<String, Object> currentUserRank = (Map<String, Object>) socialRankList.get("currentUserRank");
            Long rank = new Long((int) currentUserRank.get("rank") - 1);
            killRank = "杀号期榜第" + String.valueOf(rank + 1L) + "  ";
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
            awardPopupAddToAwardResultList(resultList, awardTimes, killRank + enRank, "period", game.getGameId());
        }

        return resultList;
    }

    @Override
    public Map<String, Object> getSocialPersonTitle(Long userId, Long lookUpUserId) {
        Map<String, Object> result = new HashMap<>();
        //1.构建用户信息
        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        boolean isMe = false;
        if (userId.equals(lookUpUserId)) {
            isMe = true;
        }
        int followCount = 0;
        int fansCount = 0;
        SocialUserFollowInfo socialUserFollowInfo = socialUserFollowInfoDao.getUserFollowInfo(userId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_DIGIT);
        if (socialUserFollowInfo != null) {
            fansCount = socialUserFollowInfo.getFansCount();
            followCount = socialUserFollowInfo.getFollowCount();
        }
        //2.关注状态
        Integer followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_NO;
        SocialUserFans socialUserFans = socialUserFansDao.getUserFans(userId, lookUpUserId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_DIGIT);
        if (socialUserFans != null && socialUserFans.getIsFans() == SocialEncircleKillConstant
                .SOCIAL_FOLLOW_STATUS_YES) {
            followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_YES;
        }
        boolean isVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);

        result.put("isMe", isMe);
        result.put("isVip", isVip);
        result.put("fansCount", fansCount);
        result.put("followCount", followCount);
        result.put("followStatus", followStatus);
        result.put("userName", userLoginVo.getNickName());
        result.put("userImg", userLoginVo.getHeadImgUrl());
        result.put("userId", userLoginVo.getUserId() + "");
        return result;
    }

    @Override
    public Integer follow(Long userId, Long followUserId, Integer followType) {
        // 获取关注详情
        Integer type;
        SocialUserFollow socialUserFollow = socialUserFollowDao.getFollowUser(userId, followUserId, followType,
                Boolean.TRUE);
        SocialUserFans socialUserFans = socialUserFansDao.getUserFans(followUserId, userId, followType);
        if (null == socialUserFollow) {
            socialUserFollow = new SocialUserFollow(userId, followUserId, followType, CommonStatusEnum.YES.getStatus());
            socialUserFans = new SocialUserFans(followUserId, userId, followType, CommonStatusEnum.YES.getStatus());
            type = CommonStatusEnum.YES.getStatus();
        } else {
            Integer isFollow = socialUserFollow.getIsFollow().equals(CommonStatusEnum.YES.getStatus())
                    ? CommonStatusEnum.NO.getStatus() : CommonStatusEnum.YES.getStatus();
            socialUserFollow.setIsFollow(isFollow);
            socialUserFans.setIsFans(isFollow);
            type = CommonStatusEnum.NO.getStatus();
        }

        self.updateSocialFollow(socialUserFollow, socialUserFans, type, followType);

        SocialUserFollowInfo fansInfo = socialUserFollowInfoDao.getUserFollowInfo(socialUserFollow.getUserId(),
                followType);
        // 更新缓存
//        String redisKeyFans = RedisConstant.PREFIX_FOLLOW_INFO_VO + String.valueOf(socialUserFollow
//                .getUserId());
        String redisKeyFans = RedisConstant.getUserFollowKey(socialUserFollow.getUserId(), followType);

        FollowInfoVo fansInfoVo = redisService.kryoGet(redisKeyFans, FollowInfoVo.class);
        if (null == fansInfoVo) {
            fansInfoVo = new FollowInfoVo();
            fansInfoVo.setUserId(socialUserFollow.getUserId());
            UserLoginVo userLoginVo = loginService.getUserLoginVo(socialUserFollow.getUserId());
            fansInfoVo.setNickName(userLoginVo.getNickName());
            fansInfoVo.setHeadImgUrl(userLoginVo.getHeadImgUrl());
        }
        Integer followCount = 0;
        Integer fansCount = 0;
        if (fansInfo != null) {
            if (fansInfo.getFollowCount() != null) {
                followCount = fansInfo.getFollowCount();
            }
            if (fansInfo.getFansCount() != null) {
                fansCount = fansInfo.getFansCount();
            }
        }
        fansInfoVo.setFollowCount(followCount);
        fansInfoVo.setFansCount(fansCount);
        redisService.kryoSetEx(redisKeyFans, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, fansInfoVo);


        SocialUserFollowInfo followInfo = socialUserFollowInfoDao.getUserFollowInfo(socialUserFans.getUserId(),
                followType);
//        String redisKeyFollow = RedisConstant.PREFIX_FOLLOW_INFO_VO + String.valueOf();
        String redisKeyFollow = RedisConstant.getUserFollowKey(socialUserFans.getUserId(), followType);

        FollowInfoVo followInfoVo = redisService.kryoGet(redisKeyFollow, FollowInfoVo.class);

        if (null == followInfoVo) {
            followInfoVo = new FollowInfoVo();
            followInfoVo.setUserId(socialUserFans.getUserId());
            UserLoginVo userLoginVo = loginService.getUserLoginVo(socialUserFans.getUserId());
            followInfoVo.setNickName(userLoginVo.getNickName());
            followInfoVo.setHeadImgUrl(userLoginVo.getHeadImgUrl());
        }
        Integer followCount1 = 0;
        Integer fansCount1 = 0;
        if (followInfo != null) {
            if (followInfo.getFollowCount() != null) {
                followCount1 = followInfo.getFollowCount();
            }
            if (followInfo.getFansCount() != null) {
                fansCount1 = followInfo.getFansCount();
            }
        }
        followInfoVo.setFollowCount(followCount1);
        followInfoVo.setFansCount(fansCount1);
        redisService.kryoSetEx(redisKeyFollow, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, followInfoVo);
        return socialUserFollow.getIsFollow();
    }

    @Transactional
    public void updateSocialFollow(SocialUserFollow socialUserFollow, SocialUserFans socialUserFans, Integer type,
                                   Integer followType) {
        if (type.equals(CommonStatusEnum.YES.getStatus())) {
            socialUserFollowDao.insert(socialUserFollow);
            socialUserFansDao.insert(socialUserFans);
        } else {
            socialUserFollow.setUpdateTime(DateUtil.getCurrentTimestamp());
            socialUserFollowDao.update(socialUserFollow);
            socialUserFans.setUpdateTime(DateUtil.getCurrentTimestamp());
            socialUserFansDao.update(socialUserFans);
        }

        // 俩个用户
        SocialUserFollowInfo followInfo = socialUserFollowInfoDao.getUserFollowInfo(socialUserFollow.getUserId(),
                followType);
        if (null == followInfo) {
            followInfo = new SocialUserFollowInfo(socialUserFollow.getUserId(), (socialUserFollow.getIsFollow()
                    .equals(1) ? 1 : 0), followType, 0);
            socialUserFollowInfoDao.insert(followInfo);
        } else {
            Integer count = followInfo.getFollowCount() + (socialUserFollow.getIsFollow().equals(1) ? 1 :
                    -1);
            if (count < 0) {
                count = 0;
            }
            followInfo.setFollowCount(count);
            followInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
            socialUserFollowInfoDao.update(followInfo);
        }

        SocialUserFollowInfo fansInfo = socialUserFollowInfoDao.getUserFollowInfo(socialUserFans.getUserId(),
                followType);
        if (null == fansInfo) {
            fansInfo = new SocialUserFollowInfo(socialUserFans.getUserId(), 0, followType, (socialUserFollow
                    .getIsFollow().equals(1) ? 1 : 0));
            socialUserFollowInfoDao.insert(fansInfo);
        } else {
            Integer count = fansInfo.getFansCount() + (socialUserFollow.getIsFollow().equals(1) ? 1 :
                    -1);
            if (count < 0) {
                count = 0;
            }
            fansInfo.setFansCount(count);
            fansInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
            socialUserFollowInfoDao.update(fansInfo);
        }

    }

    /*
     * @params
     * userId 当前用户ID
     * followListUserId 查看列表用户ID
     * */

    @Override
    public Map<String, Object> getFollowList(Long userId, Long followListUserId, String followType, Integer pageIndex) {
        Map<String, Object> resultMap = new HashMap<>();
        List<FollowInfoVo> resultList = new ArrayList<>();
        int pageCount = 200;
        if (null == pageIndex) {
            pageIndex = 1;
        }
        Integer isHaveNextPage = 0; // 是否有下一页
        // 获取查看用户的关注数量
        SocialUserFollowInfo socialUserFollowInfo = socialUserFollowInfoDao.getUserFollowInfo(followListUserId,
                CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT);
        if (null != socialUserFollowInfo && socialUserFollowInfo.getFollowCount() > 0) {
            // 获取关注列表
            if (followType.equals(CommonConstant.SOCIAL_FOLLOW_TYPE_FOLLOW)) {
                // 分页获取查看用户的列表
                PaginationList<SocialUserFollow> socialUserFollowList = socialUserFollowDao.getFollowUserListByPage
                        (followListUserId, CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT, pageIndex, pageCount);
                for (SocialUserFollow socialUserFollow : socialUserFollowList) {
                    // 遍历查看人的列表 得到每一个人的个人信息
                    FollowInfoVo followInfoVo = getFollowInfo(socialUserFollow.getFollowUserId());
                    if (null != followInfoVo) {
                        // 当前用户是否关注 这些人
                        SocialUserFollow currentUserFollow = socialUserFollowDao.getFollowUser(userId, socialUserFollow
                                .getFollowUserId(), CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT, Boolean.FALSE);
                        followInfoVo.setIsFollow(CommonStatusEnum.NO.getStatus());
                        if (null != currentUserFollow) {
                            followInfoVo.setIsFollow(currentUserFollow.getIsFollow());
                        }
                        followInfoVo.setIsMe(Boolean.FALSE);
                        // 关注者的列表中是否关注到了自己
                        if (userId.equals(socialUserFollow.getFollowUserId())) {
                            followInfoVo.setIsMe(Boolean.TRUE);
                        }
                        resultList.add(followInfoVo);
                    }
                    followInfoVo.setIsVip(vipMemberService.checkUserIsVip(socialUserFollow.getFollowUserId(),
                            VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
                    List<String> godList = new ArrayList<>();
                    Boolean isGod = Boolean.FALSE;
                    Map<String, Object> userSsqTitle = userTitleService.getUserTitleDetail(GameCache.getGame
                            (GameConstant.SSQ).getGameId(), followInfoVo.getUserId());
                    if ((Boolean) userSsqTitle.get("isGodKill") || (Boolean) userSsqTitle.get("isGodEncircle")) {
                        isGod = Boolean.TRUE;

                    }
                    Map<String, Object> userDltTitle = userTitleService.getUserTitleDetail(GameCache.getGame
                            (GameConstant.DLT).getGameId(), followInfoVo.getUserId());
                    if ((Boolean) userDltTitle.get("isGodKill") || (Boolean) userDltTitle.get("isGodEncircle")) {
                        isGod = Boolean.TRUE;
                    }
                    if (isGod) {
                        godList.add("https://ohduoklem.qnssl.com/followgod@3x.png");
                    }
                    followInfoVo.setGodList(godList);
                }
            }
            if (followType.equals(CommonConstant.SOCIAL_FOLLOW_TYPE_FOLLOW)) {
                if (socialUserFollowInfo.getFollowCount() > (pageIndex * pageCount)) {
                    isHaveNextPage = 1;
                }
            } else {
                if (socialUserFollowInfo.getFansCount() > (pageIndex * pageCount)) {
                    isHaveNextPage = 1;
                }
            }
        }

        // 获取查看用户的粉丝数量
        if (null != socialUserFollowInfo && socialUserFollowInfo.getFansCount() > 0) {
            if (followType.equals(CommonConstant.SOCIAL_FOLLOW_TYPE_FANS)) {
                // 分页获取查看用户的列表
                PaginationList<SocialUserFans> socialUserFansList = socialUserFansDao.getUserFansListByPage
                        (followListUserId, CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT, pageIndex, pageCount);
                // 遍历查看人的列表 得到每一个人的个人信息
                for (SocialUserFans socialUserFans : socialUserFansList) {
                    FollowInfoVo followInfoVo = getFollowInfo(socialUserFans.getFansUserId());
                    if (null != followInfoVo) {
                        // 当前用户是否关注 这些人
                        SocialUserFollow socialUserFollow = socialUserFollowDao.getFollowUser(userId, socialUserFans
                                .getFansUserId(), CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT, Boolean.FALSE);
                        followInfoVo.setIsFollow(CommonStatusEnum.NO.getStatus());
                        if (null != socialUserFollow) {
                            followInfoVo.setIsFollow(socialUserFollow.getIsFollow());
                        }
                        followInfoVo.setIsMe(Boolean.FALSE);
                        // 粉丝列表中者的列表中是否有自己
                        if (userId.equals(socialUserFans.getFansUserId())) {
                            followInfoVo.setIsMe(Boolean.TRUE);
                        }
                        resultList.add(followInfoVo);
                    }
                    followInfoVo.setIsVip(vipMemberService.checkUserIsVip(socialUserFans.getFansUserId(),
                            VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
                    List<String> godList = new ArrayList<>();
                    Boolean isGod = Boolean.FALSE;
                    Map<String, Object> userSsqTitle = userTitleService.getUserTitleDetail(GameCache.getGame
                            (GameConstant.SSQ).getGameId(), followInfoVo.getUserId());
                    if ((Boolean) userSsqTitle.get("isGodKill") || (Boolean) userSsqTitle.get("isGodEncircle")) {
                        isGod = Boolean.TRUE;
                    }
                    Map<String, Object> userDltTitle = userTitleService.getUserTitleDetail(GameCache.getGame
                            (GameConstant.DLT).getGameId(), followInfoVo.getUserId());
                    if ((Boolean) userDltTitle.get("isGodKill") || (Boolean) userDltTitle.get("isGodEncircle")) {
                        isGod = Boolean.TRUE;
                    }
                    if (isGod) {
                        godList.add("https://ohduoklem.qnssl.com/followgod@3x.png");
                    }
                    followInfoVo.setGodList(godList);
                }
            }
            if (socialUserFollowInfo.getFansCount() > (pageIndex * pageCount)) {
                isHaveNextPage = 1;
            }
        }

        //isHaveNextPage nextPage currentPage
        Integer currentPage = pageIndex;
        resultMap.put("followList", resultList);
        resultMap.put("currentPage", currentPage);
        resultMap.put("isHaveNextPage", isHaveNextPage);
        resultMap.put("nextPage", isHaveNextPage.equals(1) ? pageIndex + 1 : pageIndex);
        return resultMap;
    }

    @Override
    public FollowInfoVo getFollowInfo(Long userId) {
        if (!loginService.checkUser(userId)) {
            return null;
        }
        String redisKey = RedisConstant.PREFIX_FOLLOW_INFO_VO + String.valueOf(userId);
        //FollowInfoVo followInfo = redisService.kryoGet(redisKey, FollowInfoVo.class);
        FollowInfoVo followInfo = null;
        if (null == followInfo) {
            followInfo = new FollowInfoVo();
            SocialUserFollowInfo socialUserFollowInfo = socialUserFollowInfoDao.getUserFollowInfo(userId, CommonConstant
                    .SOCIAL_FOLLOW_FANS_TYPE_DIGIT);
            followInfo.setFollowCount(socialUserFollowInfo.getFollowCount());
            followInfo.setFansCount(socialUserFollowInfo.getFansCount());
            followInfo.setUserId(socialUserFollowInfo.getUserId());
            redisService.kryoSetEx(redisKey, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, followInfo);
        }
        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        followInfo.setNickName(userLoginVo.getNickName());
        followInfo.setHeadImgUrl(userLoginVo.getHeadImgUrl());
        return followInfo;
    }

    private void awardPopupAddToAwardResultList(List<Map<String, String>> resultList, Integer awardTimes, String
            rankText, String type, Long gameId) {
        Map<String, String> monthAward = new HashMap<>();
        /* 围号榜和杀号榜*/
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        String periodText = "";
        if (type.equals("month")) {
            periodText = DateUtil.getCurrentMonth("yyyy年MM") + "月每期";
        }
        if (type.equals("week")) {
            String startPeriod = "";
            String endPeriod = "";
            GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            for (int i = 0; i < 3; i++) {
                if (getWeekIdByDate(lastPeriod.getAwardTime()).equals(getWeekIdByDate(currentPeriod.getAwardTime()))) {
                    startPeriod = lastPeriod.getPeriodId();
                    lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, lastPeriod.getPeriodId());
                }
                if (getWeekIdByDate(nextPeriod.getAwardTime()).equals(getWeekIdByDate(currentPeriod.getAwardTime()))) {
                    endPeriod = nextPeriod.getPeriodId();
                    nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastPeriod.getPeriodId());
                }
            }
            periodText = startPeriod + "期-" + endPeriod + "期";
        }
        if (type.equals("period")) {
            periodText = currentPeriod.getPeriodId() + "期";
        }
        monthAward.put("period", periodText);
        monthAward.put("awardText", "智慧次数");
        monthAward.put("awardTimes", "+" + String.valueOf(awardTimes));
        monthAward.put("rankText", rankText);
        monthAward.put("awardType", type);
        resultList.add(monthAward);
    }


    private void saveAchieveMent2Db(long gameId, String periodId, Set<Long> userIds) {
        List<IndexUserSocialCode> indexUserSocialCodes = null;
        Map<String, List<IndexUserSocialCode>> userSocialCodeMap = null;
        for (Long userId : userIds) {
            indexUserSocialCodes = indexUserSocialCodeDao.getIndexSocialByCondition(null, periodId, userId, null);
            userSocialCodeMap = SocialEncircleKillCodeUtil.splitUserSocialDataByType(indexUserSocialCodes);

            UserSocialRecord userSocialRecord = null;
            if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)) {
                for (SsqAchievementEnum ac : SsqAchievementEnum.values()) {
                    for (SocialAchievement socialAchievement : ac.getSocialAchievement()) {
                        try {
                            //获取上次积分 todo 考虑抽出去
                            userSocialRecord = userSocialRecordDao.getLatestUserSocialRecord(gameId, userId,
                                    socialAchievement.getAchievementType());
                            if (userSocialRecord == null || userSocialRecord.getUserId() == null) {
                                userSocialRecord = initUserSocialRecord(gameId, userId, periodId, socialAchievement
                                        .getAchievementType(), ac.getSocialType());
                            } else if (userSocialRecord.getPeriodId().equals(periodId)) {
                                continue;
                            }
                            //获取用户参与的社交中奖数据
                            String mapKey = SocialEncircleKillCodeUtil.getUserSocialIndexMapName(gameId, ac
                                            .getSocialType(),

                                    socialAchievement.getAchievementSocialCount());
                            List<IndexUserSocialCode> userAllSocial = userSocialCodeMap.get(mapKey);
                            //计算用户战绩
                            socialAchievement.generateNewRecord(periodId, userAllSocial, userSocialRecord);
                            // TODO: 17/11/16 考虑批量插入 优化
                            userSocialRecordDao.insert(userSocialRecord);
                        } catch (DuplicateKeyException e) {
                            continue;
                        }
                    }
                }
            }
            if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
                for (DltAchievementEnum ac : DltAchievementEnum.values()) {
                    for (SocialAchievement socialAchievement : ac.getSocialAchievement()) {
                        try {
                            //获取上次积分 todo 考虑抽出去
                            userSocialRecord = userSocialRecordDao.getLatestUserSocialRecord(gameId, userId,
                                    socialAchievement.getAchievementType());
                            if (userSocialRecord == null || userSocialRecord.getUserId() == null) {
                                userSocialRecord = initUserSocialRecord(gameId, userId, periodId, socialAchievement
                                        .getAchievementType(), ac.getSocialType());
                            } else if (userSocialRecord.getPeriodId().equals(periodId)) {
                                continue;
                            }
                            //获取用户参与的社交中奖数据
                            String mapKey = SocialEncircleKillCodeUtil.getUserSocialIndexMapName(gameId, ac
                                            .getSocialType(),

                                    socialAchievement.getAchievementSocialCount());
                            List<IndexUserSocialCode> userAllSocial = userSocialCodeMap.get(mapKey);
                            //计算用户战绩
                            socialAchievement.generateNewRecord(periodId, userAllSocial, userSocialRecord);
                            // TODO: 17/11/16 考虑批量插入 优化
                            userSocialRecordDao.insert(userSocialRecord);
                        } catch (DuplicateKeyException e) {
                            continue;
                        }
                    }
                }
            }
            indexUserSocialCodes.clear();
            userSocialCodeMap.clear();
            try {
                rebuildUserSocial2Redis(gameId, userId, periodId);
            } catch (Exception e) {
                log.error("重构用户成就名片失败", e);
            }
        }
    }

    private UserSocialRecord initUserSocialRecord(long gameId, long userId, String periodId, Integer achieveTye,
                                                  Integer socialType) {
        UserSocialRecord userSocialRecord = new UserSocialRecord();
        userSocialRecord.setGameId(gameId);
        userSocialRecord.setUserId(userId);
        userSocialRecord.setPeriodId(periodId);
        userSocialRecord.setRecordType(achieveTye);
        userSocialRecord.setMaxContinueTimes(0);
        userSocialRecord.setCurrentContinueTimes(0);
        userSocialRecord.setTotalCount(0);
        userSocialRecord.setSocialType(socialType);
        return userSocialRecord;
    }
}
