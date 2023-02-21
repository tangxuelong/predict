package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.SportSocialRankVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.FootballCalculateResultEnum;
import com.mojieai.predict.enums.SportsRobotEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.GodPredictTask;
import com.mojieai.predict.thread.SocialTaskAwardTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by tangxuelong on 2017/7/24.
 */
@Service
public class SportSocialServiceImpl implements SportSocialService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private RecommendIdSequenceDao recommendIdSequenceDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IndexMatchRecommendDao indexMatchRecommendDao;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private SportSocialRankDao sportSocialRankDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private MarqueeService marqueeService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private PushService pushService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private StarUserMatchService starUserMatchService;
    @Autowired
    private MatchInfoService matchInfoService;
    @Autowired
    private UserFollowMatchesDao userFollowMatchesDao;
    @Autowired
    private MatchInfoDao matchInfoDao;
    @Autowired
    private SendEmailService sendEmailService;

    private SportSocialService self;

    @Override
    public Map<String, Object> getRecommendInfo(String matchId, Long userId, Integer playType) {
        Map<String, Object> resultMap = new HashMap<>();
        // 玩法类型
        resultMap.put("playType", playType);
        resultMap.put("playTypeInfo", getPlayTypeInfo(playType));
        // 比赛信息
        DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchListByMatchIds(matchId).get(0);
        Map<String, Object> matchMap = BeanMapUtil.beanToMap(detailMatchInfo);
        matchMap.put("tags", SportsUtils.getMatchTags(playType, detailMatchInfo.getTag()));
        resultMap.put("matchInfo", matchMap);

        //酬金
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        resultMap.put("remuneration", getSportsRecommendRemuneration(userInfo));

        // 4.6.2版本新增是否显示推荐标题
        Integer isShowRecommendTitle = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .FOOTBALL_IS_SHOW_RECOMMEND_TITLE, 1);

        resultMap.put("isShowRecommendTitle", isShowRecommendTitle);
        resultMap.put("recommendReasonTitle", "成为智慧预测师才能编辑标题和推荐理由");
        if (isShowRecommendTitle.equals(0)) {
            resultMap.put("recommendReasonTitle", "成为智慧预测师才能编辑推荐理由");
        }
        resultMap.put("recommendFontNumbers", ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .FOOTBALL_RECOMMEND_TITLE_NUMBERS, 20));


        //according 依据 ACTIVITY_INI
        String accordingStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .FOOTBALL_PLAY_TYPE_ACCORDING + playType);
        List<String> according = JSONObject.parseObject(accordingStr, ArrayList.class);
        resultMap.put("according", according);

        //推荐时跳转
        Map<String, Object> masterExplain = new HashMap<>();
        masterExplain.put("title", "<font color='#FF9317'>如何成为智慧预测师?</font>");
        masterExplain.put("jumpUrl", "https://m.caiqr.com/daily/zucaiyuceshi/index.htm");

        // 是不是 专家
        Integer wisdomer = 0;
        if (null != userInfo.getIsReMaster()) {
            wisdomer = userInfo.getIsReMaster();
        }
        resultMap.put("wisdomer", wisdomer);

        if (wisdomer != 0) {
            Integer ratio = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.FOOTBALL_WITHDRAW_OCCUPY_RATIO,
                    SportsProgramConstant.SPORT_WITHDRAW_DEFAULT_OCCUPY_RATIO);
            masterExplain.put("title", "<font color='#FF965B'>注：可提成酬金的" + ratio + "%</font>");
            masterExplain.put("jumpUrl", "");
        }
        resultMap.put("masterExplain", masterExplain);
        resultMap.put("opDataFlag", 0);
        resultMap.put("opDataUrl", "");
        resultMap.put("isSportsVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS));
        return resultMap;
    }

    @Override
    public void saveUserRecommend(UserSportSocialRecommend recommend, String clientIp, Integer clientId) {
        IndexMatchRecommend indexRecommend = new IndexMatchRecommend();
        indexRecommend.initIndexMatchRecommend(recommend.getMatchId(), recommend.getUserId(), recommend
                .getRecommendId());
        Boolean insertRes = self.addUserRecommendAndIndex(recommend, indexRecommend);
        if (insertRes) {
            //任务奖励
            userSocialTaskAwardService.recordSocialTask(recommend.getUserId(), GoldTask
                    .TASK_TYPE_ADD_SPORTS_RECOMMEND, clientIp, clientId);

            // 大咖不推送到缓存
            List<Long> idList = new ArrayList<>();
            idList.add(1804241000095370L);
            idList.add(1804241000095374L);
            idList.add(1804241000095381L);
            idList.add(1804241000095383L);
            if (idList.contains(recommend.getUserId())) {
                return;
            }
            //发布后推送到缓存
            String sportRecommendTempStorageListKey = RedisConstant.getSportSocialRecommendTempStorageListKey();
            redisService.kryoRPush(sportRecommendTempStorageListKey, recommend);

            // 添加到新推荐的缓存中
            addUserNewRecommendNums(recommend.getUserId(), recommend.getMatchId(), recommend.getEndTime());

            //check是否加入跑马灯
            UserInfo userInfo = userInfoDao.getUserInfo(recommend.getUserId());
            if (userInfo != null && userInfo.getUserId() != null) {
                boolean tempRes = addUserRecommend2Marquee(userInfo.getNickName(), userInfo.getUserId(),
                        SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT, recommend.getRecommendId());
                if (!tempRes) {
                    tempRes = addUserRecommend2Marquee(userInfo.getNickName(), userInfo.getUserId(),
                            SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM, recommend.getRecommendId());
                    if (!tempRes) {
                        tempRes = addUserRecommend2Marquee(userInfo.getNickName(), userInfo.getUserId(),
                                SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE, recommend.getRecommendId());
                    }
                }
            }

            //保存赛事缓存列表
            userSportSocialRecommendService.saveRecommendMap2MatchRedis(recommend);
            //大神推单推单
//            PushUtil.godPredictPush(userInfo.getUserId(), "您关注的足彩大神" + userInfo.getNickName() + "发推单了", CommonUtil
//                    .getSportSocialRecommendDetailPushUrl(recommend.getRecommendId()), pushService);
        }
    }

    private Boolean addUserRecommend2Marquee(String nickName, Long userId, Integer rankType, String recommendId) {
        if (!checkUserIfNeedAdd2Redis(userId, rankType)) {
            return false;
        }
        String marqueeTitle = CommonUtil.getGoldRecommendMarqueeTitle(SportsUtils.getSocialRankCn(rankType), nickName);
        String pushUrl = CommonUtil.getSportSocialRecommendDetailPushUrl(recommendId);
        marqueeService.saveContent2Marquee(marqueeTitle, pushUrl);
        return true;
    }

    private boolean checkUserIfNeedAdd2Redis(Long userId, Integer rankType) {
        String redisKey = RedisConstant.getSportSocialRankKey(rankType, 3);
        Double res = redisService.kryoZScore(redisKey, userId);
        return res != null;

    }

    @Override
    public String generateRecommendId(Long userId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = recommendIdSequenceDao.getRecommendIdSequence();
        String recommendId = (timePrefix + CommonUtil.formatSequence(seq)) + String.valueOf(userId).substring
                (String.valueOf(userId).length() - 2);
        return recommendId;
    }

    @Override
    public Long getRecommendPriceById(Integer itemId) {
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
        return exchangeMall.getItemPrice();
    }

    @Override
    public Timestamp getMatchEndTime(String matchId) {
        return thirdHttpService.getMatchListByMatchIds(matchId).get(0).getEndTime();
    }

    private Map<String, Object> getPlayTypeInfo(Integer playType) {
        Map<Integer, Map<String, Object>> playTypes = new HashMap();
        Map<String, Object> spf = new HashMap<>();
        spf.put("playTypeEn", "spf");
        spf.put("playTypeName", "胜平负");
        Map<String, Object> rqSpf = new HashMap<>();
        rqSpf.put("playTypeEn", "rqSpf");
        rqSpf.put("playTypeName", "让球胜平负");
        Map<String, Object> asia = new HashMap<>();
        asia.put("playTypeEn", "asia");
        asia.put("playTypeName", "亚盘");
        playTypes.put(0, spf);
        playTypes.put(1, rqSpf);
        playTypes.put(2, asia);
        return playTypes.get(playType);
    }


    @Override
    public void calculateCancelMatch() {
        List<MatchSchedule> matchSchedules = matchScheduleDao.getAllNeedDealCancelRankMatch();

        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }

        for (MatchSchedule matchSchedule : matchSchedules) {
            // 找到这场比赛所有的推荐
            List<IndexMatchRecommend> indexMatchRecommends = indexMatchRecommendDao.getRecommendUserByMatchId(String
                    .valueOf(matchSchedule.getMatchId()));
            for (IndexMatchRecommend indexMatchRecommend : indexMatchRecommends) {
                UserSportSocialRecommend userSportSocialRecommend = userSportSocialRecommendDao
                        .getSportSocialRecommendById(indexMatchRecommend.getUserId(), indexMatchRecommend
                                .getRecommendId(), Boolean.FALSE);
                if (null == userSportSocialRecommend) {
                    log.error("通过indexMatchRecommend 的recommendId查询不到用户推荐 recommendId" + indexMatchRecommend
                            .getRecommendId());
                    continue;
                }
                if (userSportSocialRecommend.getIsRight() == null || userSportSocialRecommend.getIsRight().equals
                        (SportsProgramConstant.RECOMMEND_STATUS_INIT)) {
                    userSportSocialRecommend.setIsRight(SportsProgramConstant.RECOMMEND_STATUS_CANCEL);
                    userSportSocialRecommendDao.update(userSportSocialRecommend);
                }

                indexMatchRecommend.setIsRank(CommonConstant.IN_RANK_NOT_NEED);
                indexMatchRecommendDao.update(indexMatchRecommend);
            }
            matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), CommonConstant.LOTTERY_CODE_FOOTBALL,
                    "IF_RANK", "RANK_TIME");
        }
    }

    /***************************************************************/
    /***************************************************************/
    /******************     排行榜抽出去            ******************/
    /***************************************************************/
    /***************************************************************/


    // 定时任务 当比赛结束时 ，把所有参与比赛的推荐更新到排行榜
    @Override
    public void cronUpdateRankEndMatch() {
        // 比赛结束时候
        List<MatchSchedule> matchSchedules = matchScheduleDao.getNeedDealRankMatch();

        if (null == matchSchedules || matchSchedules.size() == 0) {
            log.info("没有需要已结束需要计算推荐是否命中的比赛");
            return;
        }

        for (MatchSchedule matchSchedule : matchSchedules) {
            if (matchSchedule.getIfRank() != null && matchSchedule.getIfRank().equals(1)) {
                continue;
            }
            // 获取这场比赛的胜平负 让球胜平负
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchListByMatchIds(String.valueOf(matchSchedule
                    .getMatchId())).get(0);
            if (!detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END) && null ==
                    detailMatchInfo.getHostScore() || null == detailMatchInfo.getAwayScore()) {
                log.error("比赛状态已经结束，获取比分失败 matchId" + matchSchedule.getMatchId());
            }

            if (!tempCheckMatchEndTime(matchSchedule)) {
                continue;
            }

            // 找到这场比赛所有的推荐 获取结果算奖
            List<IndexMatchRecommend> indexMatchRecommends = indexMatchRecommendDao
                    .getRecommendUserByMatchId(String.valueOf(matchSchedule.getMatchId()));
            if (null == indexMatchRecommends || indexMatchRecommends.size() == 0) {
                log.info("没有推荐需要需要计算是否命中" + matchSchedule.getMatchId());
                // 更新match schedule
                matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), CommonConstant.LOTTERY_CODE_FOOTBALL,
                        "IF_RANK", "RANK_TIME");
                continue;
            }
//            matchMonitor(matchSchedule, detailMatchInfo);
            Integer rightCount = 0;
            for (IndexMatchRecommend indexMatchRecommend : indexMatchRecommends) {
                UserSportSocialRecommend userSportSocialRecommend = userSportSocialRecommendDao
                        .getSportSocialRecommendById(indexMatchRecommend.getUserId(), indexMatchRecommend
                                .getRecommendId(), Boolean.FALSE);
                if (null == userSportSocialRecommend) {
                    log.error("通过indexMatchRecommend 的recommendId查询不到用户推荐 recommendId" + indexMatchRecommend
                            .getRecommendId());
                    continue;
                }
//                if (null != userSportSocialRecommend.getIsRight()) {
//                    continue; //该推荐已经计算过命中
//                }
                // 计算命中
                if (!analysisUserRecommendIsRight(detailMatchInfo, userSportSocialRecommend)) {
                    continue;
                }
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    rightCount++;
                }

                // 更新用户收益信息到排行榜db
                updateUserRankToDB(userSportSocialRecommend.getUserId());
                // 更新用户 IndexMatchRecommend
                indexMatchRecommend.setIsRank(CommonConstant.IN_RANK_YES);
                indexMatchRecommendDao.update(indexMatchRecommend);

                try {
                    starUserMatchService.saveRecommend2StarUser(userSportSocialRecommend);
                } catch (Exception e) {
                    log.error("计算", e);
                }
            }
            updateUserRankYesterday();

            matchInfoService.saveTagMatchHit2Remark(detailMatchInfo.getMatchId(), rightCount);

            matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), CommonConstant.LOTTERY_CODE_FOOTBALL,
                    "IF_RANK", "RANK_TIME");
        }
    }

    private void matchMonitor(MatchSchedule matchSchedule, DetailMatchInfo detailMatchInfo) {
        log.info("cronUpdateRankEndMatch 发送邮件：" + matchSchedule.getMatchId() + matchSchedule.getLeagueMatchName());
        String title = "赛事算奖：" + detailMatchInfo.getMatchName() + detailMatchInfo.getMatchId();
        String content = "Calculate MatchId:" + matchSchedule.getMatchId() + " team:" + detailMatchInfo.getHostName()
                + " VS " + detailMatchInfo.getAwayName() + " 比分：" + detailMatchInfo.getHostScore() + ":" +
                detailMatchInfo.getAwayScore() + " 即将依据此赛果算奖";
        try {
            sendEmailService.SendEmail(title, content);
        } catch (Exception e) {
            log.error(e);
        }
    }

    //依据用户db rank更新排行榜缓存
    @Override
    public void updateUserRankToDB(Long userId) {
        // 最近七天
        Timestamp currentDate = DateUtil.getCurrentTimestamp();
        Timestamp begin = DateUtil.getBeginOfOneDay(DateUtil.getIntervalDays(currentDate, -7));
        Timestamp end = DateUtil.getEndOfOneDay(currentDate);
        // 获取用户最近七天的所有比赛
        List<UserSportSocialRecommend> userSportSocialRecommends = userSportSocialRecommendDao
                .getUserSportSocialRecommendByDate(userId, begin, end);
        if (null == userSportSocialRecommends || userSportSocialRecommends.size() == 0) {
            log.info("获取用户最近七天的所有比赛 为空+userId" + userId);
//            return;
        }
        Integer userTotalAwardAmount = 0; //收益综合
        Integer userSpfTotalAwardAmount = 0; //收益 胜平负
        Integer userRqSpfTotalAwardAmount = 0; //收益 让球胜平负
        Integer userAsiaTotalAwardAmount = 0; //收益 亚盘

        Integer userTotalRightNums = 0;
        Integer userSpfTotalRightNums = 0;
        Integer userRqSpfTotalRightNums = 0;
        Integer userAsiaTotalRightNums = 0;


//        Integer totalLastRight = 0;
//        Integer totalSpfLastRight = 0;
//        Integer totalRqSpfLastRight = 0;
//        Integer totalAsiaLastRight = 0;

        Integer userMaxRight = 0;
        Integer userSpfMaxRight = 0;
        Integer userRqSpfMaxRight = 0;
        Integer userAsiaMaxRight = 0;
//        List<Integer> userMaxRightArr = new ArrayList<>();
//        List<Integer> userSpfMaxRightArr = new ArrayList<>();
//        List<Integer> userRqSpfMaxRightArr = new ArrayList<>();
//        List<Integer> userAsiaMaxRightArr = new ArrayList<>();

        Integer matchCount = 0;
        Integer spfMatchCount = 0;
        Integer rqspfMatchCount = 0;
        Integer asiaMatchCount = 0;

        for (UserSportSocialRecommend userSportSocialRecommend : userSportSocialRecommends) {
            if (null == userSportSocialRecommend.getIsRight()) {
                continue;
            }
            if (null == userSportSocialRecommend.getAwardAmount()) {
                continue;
            }
            matchCount++;
            // 综合榜
            // 收益排行榜 获取用户的总收益
            Integer lastAwardAmount = userSportSocialRecommend.getAwardAmount() == null ? 0 : userSportSocialRecommend
                    .getAwardAmount();
            userTotalAwardAmount += lastAwardAmount;
            // 命中排行榜
            if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                userTotalRightNums++;
            }
            // 连中榜
            if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                userMaxRight++;
//                userMaxRightArr.add(userMaxRight);
            } else if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_GOES)) {
                userMaxRight += 0;
            } else {
//                userMaxRightArr.add(userMaxRight);
                userMaxRight = 0;
            }

            // 胜平负排行榜
            if (userSportSocialRecommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
                spfMatchCount++;
                userSpfTotalAwardAmount += userSportSocialRecommend.getAwardAmount();
                // 命中排行榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userSpfTotalRightNums++;
                }
                // 连中榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userSpfMaxRight++;
                } else {
//                    userSpfMaxRightArr.add(userSpfMaxRight);
                    userSpfMaxRight = 0;
                }
//                totalSpfLastRight = userSportSocialRecommend.getIsRight();
            }
            // 让球胜平负排行榜
            if (userSportSocialRecommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                rqspfMatchCount++;
                userRqSpfTotalAwardAmount += userSportSocialRecommend.getAwardAmount();
                // 命中排行榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userRqSpfTotalRightNums++;
                }
                // 连中榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userRqSpfMaxRight++;
                } else {
//                    userRqSpfMaxRightArr.add(userRqSpfMaxRight);
                    userRqSpfMaxRight = 0;
                }
//                totalRqSpfLastRight = userSportSocialRecommend.getIsRight();
            }
            // 亚盘排行榜
            if (userSportSocialRecommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
                asiaMatchCount++;
                userAsiaTotalAwardAmount += userSportSocialRecommend.getAwardAmount();
                // 命中排行榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userAsiaTotalRightNums++;
                }
                // 连中榜
                if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                    userAsiaMaxRight++;
                } else if (userSportSocialRecommend.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_GOES)) {
                    userAsiaMaxRight += 0;
                } else {
//                    userAsiaMaxRightArr.add(userAsiaMaxRight);
                    userAsiaMaxRight = 0;
                }
//                totalAsiaLastRight = userSportSocialRecommend.getIsRight();
            }
        }

        // 取最大连中
//        sortList(userMaxRightArr);
//        sortList(userSpfMaxRightArr);
//        sortList(userRqSpfMaxRightArr);
//        sortList(userAsiaMaxRightArr);
//        userMaxRight = userMaxRightArr.size() == 0 ? 0 : userMaxRightArr.get(0);
//        userSpfMaxRight = userSpfMaxRightArr.size() == 0 ? 0 : userSpfMaxRightArr.get(0);
//        userRqSpfMaxRight = userRqSpfMaxRightArr.size() == 0 ? 0 : userRqSpfMaxRightArr.get(0);
//        userAsiaMaxRight = userAsiaMaxRightArr.size() == 0 ? 0 : userAsiaMaxRightArr.get(0);
        // 更新数据
        // 收益榜
        updateSportSocialRank(userId, 0, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, matchCount, SportsUtils
                .getProfitRatio(userSpfTotalAwardAmount, spfMatchCount));
        updateSportSocialRank(userId, 0, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF, matchCount,
                SportsUtils.getProfitRatio(userRqSpfTotalAwardAmount, rqspfMatchCount));
        updateSportSocialRank(userId, 0, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA, matchCount, SportsUtils
                .getProfitRatio(userAsiaTotalAwardAmount, asiaMatchCount));
        updateSportSocialRank(userId, 0, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ALL, matchCount, SportsUtils
                .getProfitRatio(userTotalAwardAmount, matchCount));

        // 命中榜
        updateSportSocialRank(userId, 1, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, matchCount,
                spfMatchCount == 0 ? 0 : (userSpfTotalRightNums * 100 / spfMatchCount));
        updateSportSocialRank(userId, 1, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF, matchCount,
                rqspfMatchCount == 0 ? 0 : (userRqSpfTotalRightNums * 100 / rqspfMatchCount));
        updateSportSocialRank(userId, 1, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA, matchCount,
                asiaMatchCount == 0 ? 0 : (userAsiaTotalRightNums * 100 / asiaMatchCount));
        updateSportSocialRank(userId, 1, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ALL, matchCount,
                matchCount == 0 ? 0 : (userTotalRightNums * 100 / matchCount));

        // 连中榜
        updateSportSocialRank(userId, 2, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, matchCount, userSpfMaxRight);
        updateSportSocialRank(userId, 2, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF, matchCount, userRqSpfMaxRight);
        updateSportSocialRank(userId, 2, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA, matchCount, userAsiaMaxRight);
        updateSportSocialRank(userId, 2, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ALL, matchCount, userMaxRight);
    }

    @Override
    public void updateUserRankYesterday() {
        // 获取所有更新信息不是今天的排行榜用户 更新他们
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.getBeginOfToday());
        Date t = calendar.getTime();
        List<SportSocialRank> sportSocialRanks = sportSocialRankDao.getAllSportSocialRankByPlayTypeNotUpdate(0, 3,
                new Timestamp(t.getTime()));
        List<SportSocialRank> sportSocialRanks1 = sportSocialRankDao.getAllSportSocialRankByPlayTypeNotUpdate(1, 3,
                new Timestamp(t.getTime()));
        List<SportSocialRank> sportSocialRanks2 = sportSocialRankDao.getAllSportSocialRankByPlayTypeNotUpdate(2, 3,
                new Timestamp(t.getTime()));
        if (sportSocialRanks == null) {
            sportSocialRanks = new ArrayList<>();
        }
        sportSocialRanks.addAll(sportSocialRanks1);
        sportSocialRanks.addAll(sportSocialRanks2);
        for (SportSocialRank sportSocialRank : sportSocialRanks) {
            updateUserRankToDB(sportSocialRank.getUserId());
        }
    }

    // 每天早上10点执行
    @Override
    public void updateUserRankRedis() {

        List<MatchSchedule> matchSchedules = matchScheduleDao.getAllWaitRankRedisMatch();

        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }

        // 更新排行榜到大缓存
        // 所有在排行榜中的用户数据
        List<SportSocialRank> sportSocialRanks = sportSocialRankDao.getAllSportSocialRank();
        for (SportSocialRank sportSocialRank : sportSocialRanks) {
            String redisKey = RedisConstant.getSportSocialRankKey(sportSocialRank.getRankType(), sportSocialRank
                    .getPlayType());
//            redisService.del(redisKey);
            // 添加到各自的排行榜缓存
            if (sportSocialRank.getInRank().equals(CommonStatusEnum.YES.getStatus())) {
                redisService.kryoZRem(redisKey, sportSocialRank.getUserId());
                redisService.kryoZAddSet(redisKey, new Long(sportSocialRank.getUserScore()), sportSocialRank
                        .getUserId());
            }

            // 构建每个用户的 VO 先从缓存中取出VO
            SportSocialRankVo sportSocialRankVo = redisService.kryoGet(RedisConstant.getSportSocialVoKey
                            (sportSocialRank.getUserId()),
                    SportSocialRankVo.class);
            if (null == sportSocialRankVo) {
                sportSocialRankVo = new SportSocialRankVo();
                sportSocialRankVo.setUserId(sportSocialRank.getUserId());
            }
            // 收益榜
            if (sportSocialRank.getRankType().equals(0)) {
                // 玩法
                Map<Integer, Integer> awardAmountMap = sportSocialRankVo.getUserAwardAmountRank();
                if (null == awardAmountMap) {
                    awardAmountMap = new HashMap<>();
                }
                awardAmountMap.put(sportSocialRank.getPlayType(), sportSocialRank.getUserScore());
                sportSocialRankVo.setUserAwardAmountRank(awardAmountMap);
            }
            // 命中榜
            if (sportSocialRank.getRankType().equals(1)) {
                // 玩法
                Map<Integer, Integer> rightNumsMap = sportSocialRankVo.getUserRightNumsRank();
                if (null == rightNumsMap) {
                    rightNumsMap = new HashMap<>();
                }
                rightNumsMap.put(sportSocialRank.getPlayType(), sportSocialRank.getUserScore());
                sportSocialRankVo.setUserRightNumsRank(rightNumsMap);
            }
            // 连中榜
            if (sportSocialRank.getRankType().equals(2)) {
                // 玩法
                Map<Integer, Integer> maxRightNumsMap = sportSocialRankVo.getUserMaxNumsRank();
                if (null == maxRightNumsMap) {
                    maxRightNumsMap = new HashMap<>();
                }
                maxRightNumsMap.put(sportSocialRank.getPlayType(), sportSocialRank.getUserScore());
                sportSocialRankVo.setUserMaxNumsRank(maxRightNumsMap);
            }
            // 放入缓存
            redisService.kryoSet(RedisConstant.getSportSocialVoKey(sportSocialRank.getUserId()), sportSocialRankVo);

            // 成为推荐师
            becomeWisdomReMaster(sportSocialRank.getUserId());
        }
        //2. 排行榜刷新后刷新更新排行榜
        userSportSocialRecommendService.rebuildSportRecommendList();

        for (MatchSchedule matchSchedule : matchSchedules) {
            matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), CommonConstant.LOTTERY_CODE_FOOTBALL,
                    "IF_RANK_REDIS", "RANK_REDIS_TIME");
        }
    }

    @Override
    public SportSocialRankVo getSportSocialRankVo(Long userId) {
        SportSocialRankVo sportSocialRankVo = redisService.kryoGet(RedisConstant.getSportSocialVoKey(userId),
                SportSocialRankVo.class);
        return sportSocialRankVo;
    }

    /* 获取排行榜列表*/
    @Override
    public Map<String, Object> getSportSocialRankList(Long userId, Integer rankType, Integer playType, Integer
            nextPage) {
        Integer pageCount = 20;
        if (null == nextPage) {
            nextPage = 0;
        }
        Map<String, Object> resultMap = new HashMap<>();
        String redisKey = RedisConstant.getSportSocialRankKey(rankType, playType);

        List<Object> rankList = getSportSocialRankList(rankType, playType, nextPage, pageCount);
        resultMap.put("rank", rankList);
        if (null != userId) {
            //当前用户信息
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
            resultMap.put("userName", userLoginVo.getNickName());
            resultMap.put("userImg", userLoginVo.getHeadImgUrl());
            resultMap.put("userId", userLoginVo.getUserId());
            resultMap.put("isSportsVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant
                    .VIP_MEMBER_TYPE_SPORTS));
            Long rank = redisService.kryoZRevRank(redisKey, userId);
            SportSocialRankVo sportSocialRankVo = getSportSocialRankVo(userId);
            Integer score = null;
            if (sportSocialRankVo != null && sportSocialRankVo.getUserRankMapByType(rankType) != null) {
                Map<Integer, Integer> userRankMap = sportSocialRankVo.getUserRankMapByType(rankType);
                score = userRankMap.get(playType);
            }

            // 百分比 只要有积分就有颜色
            if (null == score) {
                resultMap.put("returnRate", "<font color='#999999'>--</font>");
                resultMap.put("unit", "");
                resultMap.put("unitPre", "");
            } else {
                if (score >= 0) {
                    resultMap.put("returnRate", "<font color='#ff5050'>" + score + "</font>");
                    if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT)) {
                        resultMap.put("unit", "<font color='#ff5050'>%</font>");
                        resultMap.put("unitPre", "<font color='#ff5050'>+</font>");
                    } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE)) {
                        resultMap.put("unit", "<font color='#ff5050'>连中</font>");
                        resultMap.put("unitPre", "");
                    } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM)) {
                        resultMap.put("unit", "<font color='#ff5050'>%</font>");
                        resultMap.put("unitPre", "");
                    }
                } else {
                    resultMap.put("returnRate", "<font color='#43BF44'>" + score + "</font>");
                    resultMap.put("unit", "<font color='#43BF44'>%</font>");
                    resultMap.put("unitPre", "<font color='#43BF44'>-</font>");
                }
            }
            if (null == rank) {
                resultMap.put("userRank", -1);
            } else {
                resultMap.put("userRank", rank.intValue() + 1);
            }
            resultMap.put("userId", userId);
        }
        Integer totalCount = redisService.kryoZCard(redisKey).intValue();
        Boolean isHaveNextPage = Boolean.FALSE;
        if (totalCount > ((nextPage + 1) * pageCount)) {
            isHaveNextPage = Boolean.TRUE;
        }
        resultMap.put("isHaveNextPage", isHaveNextPage);
        resultMap.put("nextPage", nextPage + 1);

        return resultMap;
    }

    public List<Object> getSportSocialRankList(Integer rankType, Integer playType, Integer nextPage, Integer
            pageCount) {
        String redisKey = RedisConstant.getSportSocialRankKey(rankType, playType);
        List<Long> rankUserIds = redisService.kryoZRevRange(redisKey, new Long(nextPage * pageCount), new Long
                ((nextPage + 1) * pageCount) - 1, Long.class);


        List<Object> rankList = getSportSocialRankList(rankUserIds, redisKey, rankType);
        return rankList;
    }

    public List<Object> getSportSocialRankList(List<Long> rankUserIds, String redisKey, Integer rankType) {
        List<Object> rankList = new ArrayList<>();
        if (null != rankUserIds && rankUserIds.size() > 0) {
            for (Long rankUserId : rankUserIds) {

                Map<String, Object> rankItem = new HashMap<>();
                UserLoginVo userLoginVo = loginService.getUserLoginVo(rankUserId);
                rankItem.put("userName", userLoginVo.getNickName());
                rankItem.put("userImg", userLoginVo.getHeadImgUrl());
                rankItem.put("userId", userLoginVo.getUserId());
                rankItem.put("isSportsVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(),
                        VipMemberConstant.VIP_MEMBER_TYPE_SPORTS));

                Long rank = redisService.kryoZRevRank(redisKey, rankUserId);
                rankItem.put("userRank", rank.intValue() + 1);
                Double score = redisService.kryoZScore(redisKey, rankUserId);
                if (score.intValue() >= 0) {
                    rankItem.put("returnRate", "<font color='#ff5050'>" + score.intValue() + "</font>");
                    if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT)) {
                        rankItem.put("unit", "<font color='#ff5050'>%</font>");
                        rankItem.put("unitPre", "<font color='#ff5050'>+</font>");
                    } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE)) {
                        rankItem.put("unit", "<font color='#ff5050'>连中</font>");
                        rankItem.put("unitPre", "");
                    } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM)) {
                        rankItem.put("unit", "<font color='#ff5050'>%</font>");
                        rankItem.put("unitPre", "");
                    }
                } else {
                    rankItem.put("returnRate", "<font color='#43BF44'>" + score.intValue() + "</font>");
                    rankItem.put("unit", "<font color='#43BF44'>%</font>");
                    rankItem.put("unitPre", "<font color='#43BF44'>-</font>");
                }

                // 是否有新推荐
                Integer newRecommendCount = redisService.kryoZCount(RedisConstant.getUserRecommendMatchsKey(userLoginVo
                        .getUserId()), DateUtil.getCurrentTimestamp().getTime(), Long.MAX_VALUE).intValue();
                if (newRecommendCount > 0) {
                    rankItem.put("newRecommend", "新推荐");
                    rankItem.put("newRecommendNum", newRecommendCount);
                }
                rankList.add(rankItem);
            }
        }
        return rankList;
    }

    @Override
    public List<Object> getSportSocialRankList(Integer rankType, Integer playType, Integer limit) {
        String redisKey = RedisConstant.getSportSocialRankKey(rankType, playType);
        List<Long> rankUserIds = redisService.kryoZRevRange(redisKey, 0L, new Long(limit), Long.class);

        List<Object> rankList = getSportSocialRankList(rankUserIds, redisKey, rankType);

        return rankList;
    }

    @Transactional
    @Override
    public Boolean addUserRecommendAndIndex(UserSportSocialRecommend recommend, IndexMatchRecommend recommendIndex) {
        Boolean res = Boolean.FALSE;
        try {
            Integer tempRes = userSportSocialRecommendDao.insert(recommend);
            if (tempRes > 0) {
                indexMatchRecommendDao.insert(recommendIndex);
                res = Boolean.TRUE;
            }
        } catch (DuplicateKeyException e) {

        }
        return res;
    }

    @Override
    public Integer getUserRecommend(Long userId, Timestamp beginOfToday, Timestamp endOfToday) {
        Integer count = userSportSocialRecommendDao.getUserSportSocialRecommendsByTime(userId, beginOfToday,
                endOfToday);
        count = count == null ? 0 : count;
        return count;
    }

    private void updateSportSocialRank(Long userId, Integer rankType, Integer playType, Integer matchCount, Integer
            userScore) {
        Integer inRank = 0;
        if (matchCount >= 10) { // 上榜条件在这里修改
            inRank = 1;
        }
        SportSocialRank sportSocialRank = new SportSocialRank(userId, rankType, playType,
                matchCount, userScore, null, inRank);
        SportSocialRank sportSocialRankDB = sportSocialRankDao.getUserSportSocialRankByType(rankType, playType, userId,
                Boolean.FALSE);
        if (null == sportSocialRankDB) {
            try {
                sportSocialRankDao.insert(sportSocialRank);
            } catch (DuplicateKeyException e) {
                return;
            }
        } else {
            sportSocialRankDao.update(sportSocialRank);
        }
        // 如果不在榜单从redis 去掉
        if (inRank.equals(0)) {
            String redisKey = RedisConstant.getSportSocialRankKey(rankType, playType);
            redisService.kryoZRem(redisKey, userId);
        }
    }

    // 成为推荐师
    private void becomeWisdomReMaster(Long userId) {
        // 查询这个用户是否满足成为推荐大师的条件
        /* 如何成为智慧预测师？近7天最少推荐10场比赛，整体命中率大于70%且收益率大于100%。结算时间为每天的早上10点。*/
        SportSocialRank sportSocialRank = sportSocialRankDao.getUserSportSocialRankByType(0, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ALL, userId, Boolean.FALSE);

        if (null != sportSocialRank) {
            // 近7天最少推荐10场比赛 收益率大于10%
            if (sportSocialRank.getMatchCount() >= 10 && sportSocialRank.getUserScore() > 120) {
                SportSocialRank sportSocialRankRight = sportSocialRankDao.getUserSportSocialRankByType(1,
                        SportsProgramConstant.FOOTBALL_PLAY_TYPE_ALL, userId, Boolean.FALSE);
                // 整体命中率大于70%
                if (sportSocialRankRight.getUserScore() * 100 / sportSocialRankRight.getMatchCount() > 70) {
                    boolean socialPop = false;
                    UserInfo userInfo = userInfoDao.getUserInfo(userId);
                    if (userInfo.getIsReMaster() != null && userInfo.getIsReMaster() != 1) {
                        socialPop = true;
                    }
                    userInfo.setIsReMaster(1);
                    userInfoDao.update(userInfo);
                    if (socialPop) {
                        String key = RedisConstant.getBecomeSportsMaster(SportsProgramConstant
                                .LOTTERY_LOTTERY_CODE_FOOTBALL);
                        redisService.kryoSAddSet(key, userInfo.getUserId());
                        redisService.expire(key, 446400);
                    }
                }
            }
        }
    }

    // 用户的新推荐缓存
    private void addUserNewRecommendNums(Long userId, String matchId, Timestamp endTime) {
        // 添加到用户推荐的缓存
        redisService.kryoZAddSet(RedisConstant.getUserRecommendMatchsKey(userId), endTime.getTime(), matchId);
    }

    // 根据比分计算推荐结果的返奖钱
    @Override
    public Integer getAwardAmountByScore(DetailMatchInfo detailMatchInfo, Integer playType, String recommend, String
            handicap) {
        String recommend1 = recommend;
        String recommend2 = null;
        if (recommend.contains(CommonConstant.COMMA_SPLIT_STR)) {
            recommend1 = recommend.split(CommonConstant.COMMA_SPLIT_STR)[0];
            recommend2 = recommend.split(CommonConstant.COMMA_SPLIT_STR)[1];
        }
        Integer recommendItem = Integer.valueOf(recommend1.split(CommonConstant.COMMON_COLON_STR)[0]);
        BigDecimal odd = new BigDecimal(recommend1.split(CommonConstant.COMMON_COLON_STR)[1]);

        Integer recommendItem1 = null;
        BigDecimal odd1 = null;
        if (null != recommend2) {
            recommendItem1 = Integer.valueOf(recommend2.split(CommonConstant.COMMON_COLON_STR)[0]);
            odd1 = new BigDecimal(recommend2.split(CommonConstant.COMMON_COLON_STR)[1]);
        }

        BigDecimal hostScore = new BigDecimal(detailMatchInfo.getHostScore());
        BigDecimal awayScore = new BigDecimal(detailMatchInfo.getAwayScore());
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
            Integer result = null;
            if (hostScore.compareTo(awayScore) == 1) {
                result = CommonConstant.FOOTBALL_SPF_ITEM_S;
            }
            if (hostScore.compareTo(awayScore) == -1) {
                result = CommonConstant.FOOTBALL_SPF_ITEM_F;
            }
            if (hostScore.compareTo(awayScore) == 0) {
                result = CommonConstant.FOOTBALL_SPF_ITEM_P;
            }
            // 奖金优化 a*b*100/(a+b)
            if (null != odd1) {
                if (recommendItem.equals(result) || recommendItem1.equals(result)) {
                    return odd.multiply(new BigDecimal(100)).multiply(odd1).divide(odd.add(odd1),
                            2, RoundingMode.CEILING).intValue();
                }
            } else {
                if (recommendItem.equals(result)) {
                    return odd.multiply(new BigDecimal(100)).intValue();
                }
            }


        }
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
            Integer result = null;
            if (StringUtils.isBlank(handicap)) {
                handicap = detailMatchInfo.getRqSpf().get("handicap").toString();
            }
            result = FootballCalculateResultEnum.getEnum(playType).getHitOption(hostScore.toString(), awayScore
                    .toString(), handicap);
            // 奖金优化 a*b*100/(a+b)
            if (null != odd1) {
                if (recommendItem.equals(result) || recommendItem1.equals(result)) {
                    return odd.multiply(new BigDecimal(100)).multiply(odd1).divide(odd.add(odd1),
                            2, RoundingMode.CEILING).intValue();
                }
            } else {
                if (recommendItem.equals(result)) {
                    return odd.multiply(new BigDecimal(100)).intValue();
                }
            }
        }
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
            FootballCalculateResultEnum asiaResult = FootballCalculateResultEnum.getEnum(SportsProgramConstant
                    .FOOTBALL_PLAY_TYPE_ASIA);
            if (asiaResult == null) {
                log.error("FootballCalculateResultEnum is null pls check!!! playType:" + playType);
                return 0;
            }
            if (StringUtils.isBlank(handicap)) {
                handicap = detailMatchInfo.getAsia().get("handicap").toString();
            }

            Double retOdd = hostScore.subtract(awayScore).add(new BigDecimal(handicap)).doubleValue();
            return SportsUtils.getAsiaIncome(retOdd, odd.doubleValue(), recommendItem);

        }
        return 0;
    }

    // 计算用户推荐是否命中
    private Boolean analysisUserRecommendIsRight(DetailMatchInfo detailMatchInfo, UserSportSocialRecommend
            userSportSocialRecommend) {
        if (StringUtils.isBlank(userSportSocialRecommend.getRecommendInfo())) {
            log.error("计算命中，用户推荐为空 recommendId" + userSportSocialRecommend.getRecommendId());
            return Boolean.FALSE;
        }
        // 比赛结果
        String recommend = userSportSocialRecommend.getRecommendInfo();

        Integer awardAmount = getAwardAmountByScore(detailMatchInfo, userSportSocialRecommend.getPlayType(), recommend,
                userSportSocialRecommend.getHandicap());

        Integer isRight = getUserRecommendIsRight(userSportSocialRecommend, detailMatchInfo);

        userSportSocialRecommend.setAwardAmount(awardAmount);

        userSportSocialRecommend.setIsRight(isRight);

        userSportSocialRecommendDao.update(userSportSocialRecommend);
        return Boolean.TRUE;
    }

    @Override
    public Integer getUserRecommendIsRight(UserSportSocialRecommend recommend, DetailMatchInfo matchDetail) {
        Integer result = SportsProgramConstant.RECOMMEND_STATUS_INIT;

        String handicap = recommend.getHandicap();
        String hostScore = matchDetail.getHostScore().toString();
        String awayScore = matchDetail.getAwayScore().toString();
        Map<Integer, String> userRecommendOptionMap = SportsUtils.getUserRecommendOptionMap(recommend
                .getRecommendInfo());

        if (StringUtils.isBlank(handicap)) {
            if (recommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
                handicap = matchDetail.getAsia().get("handicap").toString();
            } else if (recommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                handicap = matchDetail.getRqSpf().get("handicap").toString();
            }
        }
        //1.获取所有用户推荐
        Integer matchRes = SportsUtils.getBetResult(hostScore, awayScore, handicap, 200, recommend.getPlayType());
        if (matchRes.equals(-1)) {
            return result;
        }
        //2.依据赛果和推荐计算对错
        if (userRecommendOptionMap != null && userRecommendOptionMap.containsKey(matchRes)) {
            result = SportsProgramConstant.RECOMMEND_STATUS_WINNING;
        } else {
            result = SportsProgramConstant.RECOMMEND_STATUS_LOST;
            if (recommend.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) && matchRes.equals
                    (CommonConstant
                            .FOOTBALL_RQ_ASIA_ITEM_P)) {
                result = SportsProgramConstant.RECOMMEND_STATUS_GOES;
            }
        }

        return result;
    }

    private void sortList(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                return o2 - o1;
            }
        });
    }

    private List<Map<String, Object>> getSportsRecommendRemuneration(UserInfo userInfo) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ExchangeMall> exchangeMallList = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_RECOMMEND);
        Boolean isSportsVip = vipMemberService.checkUserIsVip(userInfo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_SPORTS);

        for (ExchangeMall exchangeMall : exchangeMallList) {
            Map<String, Object> remunerationItem = new HashMap<>();
            Boolean vipFlag = judgeItemPriceVipFlag(exchangeMall);

            remunerationItem.put("Id", exchangeMall.getItemId());
            remunerationItem.put("name", exchangeMall.getItemName());
            remunerationItem.put("enable", judgeItemPriceEnable(userInfo, exchangeMall, isSportsVip));
            remunerationItem.put("img", vipFlag ? "http://sportsimg.mojieai.com/sports_recommend_price_vip.png" : "");
            remunerationItem.put("vipFlag", vipFlag);
            result.add(remunerationItem);
        }
        return result;
    }

    private Integer judgeItemPriceEnable(UserInfo userInfo, ExchangeMall exchangeMall, Boolean isSportsVip) {
        Integer result = 0;
        if (exchangeMall.getItemName().equals("免费")) {
            return 1;
        }

        if (null != userInfo.getIsReMaster() && userInfo.getIsReMaster().equals(1)) {
            result = 1;

            if (StringUtils.isNotBlank(exchangeMall.getRemark())) {
                Map<String, Object> remarkMap = JSONObject.parseObject(exchangeMall.getRemark(), HashMap.class);
                if (remarkMap != null && remarkMap.containsKey("vipPriceFlag") && Integer.valueOf(remarkMap.get
                        ("vipPriceFlag").toString()).equals(1)) {
                    if (!isSportsVip) {
                        result = 0;
                    }
                }
            }
        }

        return result;
    }

    private Boolean judgeItemPriceVipFlag(ExchangeMall exchangeMall) {
        Boolean result = false;
        Map<String, Object> remarkMap = JSONObject.parseObject(exchangeMall.getRemark(), HashMap.class);
        if (remarkMap != null && remarkMap.containsKey("vipPriceFlag") && Integer.valueOf(remarkMap.get
                ("vipPriceFlag").toString()).equals(1)) {
            result = true;
        }
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (SportSocialService) proxyBean;
    }


    /* 关注比赛方法*/
    @Override
    public Integer followMatch(Long userId, String matchId) {
        Integer followStatus = tranFollowMatch(userId, matchId);
        // 列表数据更新
        MatchInfo matchInfo = matchInfoDao.getMatchInfoByMatchId(Integer.valueOf(matchId), Boolean.FALSE);

        // 取消关注
        if (followStatus.equals(0)) {
            String key = RedisConstant.getUserFollowMatchListKey(SportsProgramConstant.MATCH_TAG_USER_FOLLOW, userId);
            redisService.kryoZRem(key, Integer.valueOf(matchId));
        }

        // 关注
        if (followStatus.equals(1)) {
            matchInfoService.saveTagMatches2TimeLine(matchInfo, Integer.valueOf(SportsProgramConstant
                    .MATCH_TAG_USER_FOLLOW), userId);
        }

        return followStatus;
    }

    @Override
    public Integer checkUserFollowMatch(Long userId, String matchId) {
        String key = RedisConstant.getUserFollowMatchListKey(SportsProgramConstant.MATCH_TAG_USER_FOLLOW, userId);
        Double score = redisService.kryoZScore(key, matchId);
        if (null == score) {
            return 0;
        }
        return 1;
    }

    @Override
    public Map<String, Object> getMJMatchTag() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tagList = new ArrayList<>();
        Map<String, Object> leagueMatchTag = thirdHttpService.getLeagueList();
        if (leagueMatchTag.containsKey("resp")) {
            List<Map<String, Object>> resp = JSONObject.parseObject(leagueMatchTag.get("resp").toString(), ArrayList
                    .class);
            for (Map<String, Object> temp : resp) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("tagId", temp.get("id"));
                tag.put("tagName", temp.get("name"));
                tagList.add(tag);
            }
        }

        result.put("tagList", tagList);
        return result;
    }

    @Override
    public Map<String, Object> getMJLeagueMatchList(String leagueId) {
        Map<String, Object> result = new HashMap<>();

        result.put("leagueProcess", getLeagueProcess(leagueId));
        result.put("integralRank", getIntegralRank(leagueId));
        return result;
    }

    @Override
    public Map<String, Object> getMJLeagueGroupMatch(String groupId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> battleInfos = new ArrayList<>();
        Map<String, Object> thirdGroupMatch = thirdHttpService.getLeagueGroupMatch(groupId);
        List<Map<String, Object>> matchList = (List<Map<String, Object>>) thirdGroupMatch.get("resp");
        if (matchList.size() > 0) {
            for (Map<String, Object> matchInfo : matchList) {
                Map<String, Object> temp = new HashMap<>();
                String score = "VS";
                temp.put("hostName", matchInfo.get("host_name"));
                temp.put("score", score);
                temp.put("awayName", matchInfo.get("away_name"));
                temp.put("hostImg", matchInfo.get("host_team_image"));
                temp.put("awayImg", matchInfo.get("away_team_image"));
                temp.put("matchTime", matchInfo.get("match_time"));
                battleInfos.add(temp);
            }
        }
        result.put("battleInfos", battleInfos);
        return result;
    }

    private List<Map<String, Object>> getIntegralRank(String leagueId) {
        Map<String, Object> integralRank = new HashMap<>();
        Map<String, Object> thirdIntegralRank = thirdHttpService.getIntegralRank(leagueId);
        List<Map<String, Object>> resp = (List<Map<String, Object>>) thirdIntegralRank.get("resp");

//        integralRank.put("", );
        return resp;
    }

    private Map<String, Object> getLeagueProcess(String leagueId) {
        Map<String, Object> leagueProcess = new HashMap<>();
        Map<String, Object> leagueMatchTag = thirdHttpService.getLeagueMatchList(leagueId);
        List<Map<String, Object>> matchInfosMJ = (List<Map<String, Object>>) leagueMatchTag.get("resp");
        List<Map<String, Object>> battleInfos = new ArrayList<>();
        List<Map<String, Object>> seasonList = new ArrayList<>();
        if (matchInfosMJ.size() > 0) {
            List<Map<String, Object>> chooseMatch = (List<Map<String, Object>>) matchInfosMJ.get(0).get("choose_data");
            for (Map<String, Object> matchInfo : chooseMatch) {
                Map<String, Object> battle = new HashMap<>();
                String score = "VS";
                if (matchInfo.containsKey("host_score") && matchInfo.get("host_score") != null) {
                    score = matchInfo.get("host_score") + ":" + matchInfo.get("away_score");
                }
                battle.put("matchTime", matchInfo.get("match_time"));
                battle.put("hostName", matchInfo.get("host_name"));
                battle.put("awayName", matchInfo.get("away_name"));
                battle.put("hostImg", matchInfo.get("host_team_image"));
                battle.put("awayImg", matchInfo.get("away_team_image"));
                battle.put("score", score);
                battleInfos.add(battle);
            }
            List<Map<String, Object>> seasonListMJ = (List<Map<String, Object>>) matchInfosMJ.get(0).get("season_list");
            if (seasonListMJ.size() > 0) {
                for (Map<String, Object> tempSeason : seasonListMJ) {
                    Map<String, Object> season = new HashMap<>();
                    season.put("groupId", tempSeason.get("group_id"));
                    season.put("season", tempSeason.get("season"));
                    season.put("name", tempSeason.get("name"));
                    seasonList.add(season);
                }
            }
        }

        leagueProcess.put("battleInfos", battleInfos);
        leagueProcess.put("seasonList", seasonList);
        return leagueProcess;
    }

    @Transactional
    public Integer tranFollowMatch(Long userId, String matchId) {
        // 关注比赛数据更新
        UserFollowMatches userFollowMatches = userFollowMatchesDao.getUserFollowMatchByUserIdAndUserId(userId, matchId,
                Boolean.TRUE);
        // 如果是第一次点击 为关注
        if (null == userFollowMatches) {
            userFollowMatches = new UserFollowMatches(userId, matchId);
            userFollowMatchesDao.insert(userFollowMatches);
        } else {
            // 如果是第一次点击 为关注
            Integer newFollowStatus = userFollowMatches.getFollowStatus().equals(0) ? 1 : 0;
            userFollowMatches.setFollowStatus(newFollowStatus);
            userFollowMatchesDao.update(userFollowMatches);
        }
        // 返回当前关注状态
        return userFollowMatches.getFollowStatus();
    }

    private Boolean tempCheckMatchEndTime(MatchSchedule matchSchedule) {
        if (matchSchedule == null || !matchSchedule.getIfEnd().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            return Boolean.FALSE;
        }
        Timestamp endTime = matchSchedule.getEndTime();
        if (endTime == null) {
            return Boolean.FALSE;
        }
        long diffMin = DateUtil.getDiffMinutes(endTime, DateUtil.getCurrentTimestamp());
        if (diffMin <= 15) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
