package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PayUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class UserBuyRecommendServiceImpl implements UserBuyRecommendService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserBuyRecommendIdSequenceDao userBuyRecommendIdSequenceDao;
    @Autowired
    private UserBuyRecommendDao userBuyRecommendDao;
    @Autowired
    private MissionDao missionDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private PayService payService;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private MarqueeService marqueeService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private UserInfoDao userInfoDao;

    private UserBuyRecommendService self;

    @Override
    public Map<String, Object> getUserPurchaseSportRecommend(Long userId, Integer lotteryCode, String lastIndex) {
        Map<String, Object> res = new HashMap<>();
        Integer pageSize = 10;
        boolean hasNext = false;
        //1.获取用户购买的推荐
        List<UserBuyRecommend> userPurchaseRecommends = userBuyRecommendDao.getUserPurchaseSportRecommend
                (userId, lotteryCode, lastIndex, pageSize + 1);
        List<Map<String, Object>> datas = new ArrayList<>();
        if (userPurchaseRecommends != null && userPurchaseRecommends.size() > 0) {
            lastIndex = userPurchaseRecommends.get(userPurchaseRecommends.size() - 1).getFootballLogId();
            if (userPurchaseRecommends.size() > pageSize) {
                hasNext = true;
                lastIndex = userPurchaseRecommends.get(pageSize - 1).getFootballLogId();
            }

            //2.请求列表中所有赛事的信息
            Integer count = 0;
            StringBuilder matchIds = new StringBuilder();
            for (UserBuyRecommend userPurchaseRecommend : userPurchaseRecommends) {
                matchIds.append(userPurchaseRecommend.getMatchId());
                if (count < userPurchaseRecommends.size() - 1) {
                    matchIds.append(CommonConstant.COMMA_SPLIT_STR);
                }
                count++;
            }
            //3.依据赛事id获取所有比赛信息
            Map<String, DetailMatchInfo> detailMatchInfos = thirdHttpService.getMatchMapByMatchIds(matchIds.toString());

            for (UserBuyRecommend userPurchaseRecommend : userPurchaseRecommends) {
                DetailMatchInfo detailMatchInfo = detailMatchInfos.get(userPurchaseRecommend.getMatchId());
                Map tempMap = packageUserPurchaseRecommend(userPurchaseRecommend, detailMatchInfo);
                if (tempMap != null && !tempMap.isEmpty()) {
                    datas.add(tempMap);
                }
            }
        }
        res.put("datas", datas);
        res.put("hasNext", hasNext);
        res.put("lastIndex", lastIndex);
        return res;
    }

    private Map<String, Object> packageUserPurchaseRecommend(UserBuyRecommend userPurchaseRecommend,
                                                             DetailMatchInfo detailMatchInfo) {
        Map<String, Object> res = new HashMap<>();

        String teamName = detailMatchInfo.getHostName() + "  VS  " + detailMatchInfo.getAwayName();
        String matchDesc = detailMatchInfo.getMatchName() + " " + detailMatchInfo.getMatchDate() + " " + detailMatchInfo
                .getMatchTime();
        Integer programStatus = SportsProgramConstant.RECOMMEND_STATUS_INIT;
        if (userPurchaseRecommend.getAwardStatus() != null) {
            programStatus = userPurchaseRecommend.getAwardStatus();
        }

        //依据比赛状态
        String btnMsg = "未开赛";
        boolean teamNameDescWeakColor = false;
        Integer matchStatus = detailMatchInfo.getMatchStatus();
        if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            btnMsg = "";
            teamNameDescWeakColor = true;
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            btnMsg = "<font color='#ff5050'>比赛中</font>";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
            btnMsg = "<font color='#999999'>比赛取消</font>";
            teamNameDescWeakColor = true;
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
            btnMsg = "<font color='#999999'>比赛延期</font>";
        }

        if (teamNameDescWeakColor) {
            teamName = "<font color='#999999'>" + teamName + "</font>";
        }

        res.put("matchDesc", matchDesc);
        res.put("matchStatus", matchStatus);
        res.put("recommendId", userPurchaseRecommend.getProgramId());
        res.put("teamName", teamName);
        res.put("playName", SportsUtils.getPlayTypeCn(userPurchaseRecommend.getLotteryCode(), userPurchaseRecommend
                .getPlayType()));

        String payAmount = "";
        if (userPurchaseRecommend.getPayAmount() != null) {
            payAmount = CommonUtil.convertFen2Yuan(userPurchaseRecommend.getPayAmount()).toString() + CommonConstant
                    .WISDOM_COIN_PAY_NAME;
        }

        res.put("payAmount", payAmount);
        res.put("programStatus", programStatus);
        res.put("recommendId", userPurchaseRecommend.getProgramId());

        UserSportSocialRecommend userSportSocialRecommend = userSportSocialRecommendDao.getSportSocialRecommendById
                (Long.parseLong(userPurchaseRecommend.getProgramId()), userPurchaseRecommend.getProgramId(), Boolean
                        .FALSE);

        // 个人推荐添加title 4.6.2
        if (null != userSportSocialRecommend) {
            userSportSocialRecommendService.userRecommendTitleLock(userSportSocialRecommend);
            res.put("recommendTitle", userSportSocialRecommend.getRecommendTitle());

            // 添加标签 4.6.4 单选 分析
            res.putAll(userSportSocialRecommend.remark2marks());

        } else {
            res.put("recommendTitle", "");
        }

        res.put("payAmount", userPurchaseRecommend.getPayAmount());
        res.put("btnMsg", btnMsg);
        res.put("tags", SportsUtils.getMatchTags(userPurchaseRecommend.getPlayType(), detailMatchInfo.getTag()));
        return res;
    }

    @Override
    public Boolean checkUserPurchaseFootballProgramStatus(Long userId, String programId) {
        String userKey = RedisConstant.getUserPurchaseFootballProgramKey(userId, programId);
        Long purchaseUserId = redisService.kryoGet(userKey, Long.class);
        if (purchaseUserId != null && userId.equals(purchaseUserId)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public UserBuyRecommend initUserBuyRecommend(Long userId, Long payAmount, UserSportSocialRecommend
            recommend) {
        //1.查询用户是否已经购买
        UserBuyRecommend userProgramLog = userBuyRecommendDao.getUserBuyRecommendByUniqueKey
                (userId, recommend.getRecommendId(), false);
        if (checkUserPurchaseFootballProgramStatus(userId, recommend.getRecommendId())) {
            return userProgramLog;
        }

        if (userProgramLog != null && userProgramLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return userProgramLog;
        }
        //2.初始化用户购买记录
        userProgramLog = new UserBuyRecommend();
        String footballLogId = CommonUtil.generateStrId(userId, "SPORTPROGRAM", userBuyRecommendIdSequenceDao);
        Long withdrawAmount = SportsUtils.getWithdrawAmountByDivided(payAmount);
        userProgramLog.initUserBuyRecommend(footballLogId, recommend.getRecommendId(), userId, recommend
                .getPrice(), payAmount, withdrawAmount, recommend.getLotteryCode(), recommend.getMatchId(), recommend
                .getPlayType());
        try {
            userBuyRecommendDao.insert(userProgramLog);
        } catch (DuplicateKeyException e) {
            userProgramLog = userBuyRecommendDao.getUserBuyRecommendByUniqueKey(userId, recommend.getRecommendId(),
                    false);
        }

        return userProgramLog;
    }

    @Override
    public Boolean updateUserBuyRecommendAfterPayed(Long userId, String sportSocialRecommendId, Boolean couponFlag,
                                                    Boolean ifCardUnlock) {
        Boolean res = Boolean.FALSE;
        //1.更新用户购买db
        Boolean updateRes = self.updateFootballProgramPayed(userId, sportSocialRecommendId, couponFlag, ifCardUnlock);
        if (updateRes) {
            userSocialTaskAwardService.recordSocialTask(userId, GoldTask.TASK_TYPE_PURCHASE_RECOMMEND, "127.0.0.1",
                    CommonConstant.CLIENT_TYPE_ANDRIOD);
            //2.刷新各种缓存
            saveUserPurchaseProgram2Redis(sportSocialRecommendId, userId);
            //3.添加跑马灯
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
            UserBuyRecommend recommendLog = userBuyRecommendDao.getUserBuyRecommendByUniqueKey
                    (userId, sportSocialRecommendId, false);
            String payAmount = "";
            if (recommendLog.getPayAmount() != null) {
                payAmount = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(recommendLog.getPayAmount())
                        .toString());
            }
            String marqueeTitle = CommonUtil.getPurchaseRecommendMarqueeTitle(userLoginVo.getNickName(), payAmount);
            marqueeService.saveContent2Marquee(marqueeTitle, CommonUtil.getSportSocialRecommendDetailPushUrl
                    (sportSocialRecommendId));
            res = Boolean.TRUE;
        }
        return res;
    }

    private void saveUserPurchaseProgram2Redis(String programId, Long userId) {
        String userKey = RedisConstant.getUserPurchaseFootballProgramKey(userId, programId);
        redisService.kryoSetEx(userKey, 604800, userId);
    }

    @Transactional
    @Override
    public Boolean updateFootballProgramPayed(Long userId, String sportSocialRecommendId, Boolean couponFlag, Boolean ifCardUnlock) {
        Boolean res = Boolean.FALSE;
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByUniqueKey(userId,
                sportSocialRecommendId, true);
        if (programLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_NO) || (ifCardUnlock && Objects.equals
                (CommonConstant.PROGRAM_BUY_STATUS_PAYED, programLog.getPayStatus()))) {
            Integer updateRes = 0;
            if (!ifCardUnlock) {
                updateRes = userBuyRecommendDao.updatePayStatus(userId, programLog.getFootballLogId(),
                        CommonConstant.PROGRAM_IS_PAY_YES, CommonConstant.PROGRAM_IS_PAY_NO, couponFlag);
                if (programLog.getProgramAmount() == null || programLog.getProgramAmount() == 0) {
                    return updateRes > 0;
                }
            }
            if (updateRes > 0 || ifCardUnlock) {
                Long userPrefix = CommonUtil.getUserIdSuffix(sportSocialRecommendId);
                UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById
                        (userPrefix, sportSocialRecommendId, true);
                //3.售出推荐加1
                Integer saleCount = recommend.getSaleCount() == null ? 1 : (recommend.getSaleCount() + 1);
                Integer couponSaleCount = recommend.getCouponSaleCount();
                if (couponFlag) {
                    couponSaleCount += 1;
                }
                userSportSocialRecommendDao.updateSaleCount(userPrefix, recommend.getRecommendId(), saleCount,
                        couponSaleCount);

                //未使用免单券才涉及提现
                if (!couponFlag && !ifCardUnlock) {
                    Integer lotteryCode = recommend.getLotteryCode();
                    String matchId = recommend.getMatchId();
                    Mission mission = new Mission(programLog.getFootballLogId(), Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                            Mission.MISSION_STATUS_INTI, DateUtil.getCurrentTimestamp(), "", lotteryCode + ":" + matchId);
                    missionDao.insert(mission);
                }
                res = Boolean.TRUE;
            }
        }
        return res;
    }

    @Override
    public Boolean footballMatchEndUpdateWithdrawStatus(Mission mission) {
        Boolean res = Boolean.FALSE;
        String programId = mission.getKeyInfo();
        Long userId = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
        //1.check是否已经更新
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userId, programId, false);
        if (programLog == null) {
            return res;
        }
        //2.如果记录已经被转移到账户直接返回
        if (programLog.getWithdrawStatus().equals(SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_FINISH)) {
            return Boolean.TRUE;
        }
        //3.将记录置位（更新用户记录为已操作，并更新mission status 为等待提现到账户）
        res = self.updateWithdrawStatusAndMission(programLog.getUserId(), mission);
        return res;
    }

    @Override
    public Boolean footballMatchCancelUpdateWithdrawStatus(Mission mission) {
        Boolean res = Boolean.FALSE;
        String programId = mission.getKeyInfo();
        Long userId = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
        //1.check是否已经更新
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userId, programId, false);
        if (programLog == null) {
            return res;
        }
        //2.如果记录已经取消就返回
        if (programLog.getWithdrawStatus().equals(SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_QUIT)) {
            return Boolean.TRUE;
        }
        //3.将记录置位（更新用户记录为已取消，并更新mission status 为等待退款到账户）
        res = self.cancelWithdrawStatusAndMission(programLog.getUserId(), mission);
        return res;
    }

    @Override
    public Boolean callBackMakeUserFootballRecommendEffective(String userFootballLogId, String exchangeFlowId) {
        Boolean res = Boolean.FALSE;
        //1.check流水
        Long userIdSuffix = Long.valueOf(exchangeFlowId.substring(exchangeFlowId.length() - 2, exchangeFlowId.length
                ()));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, userIdSuffix,
                false);
        if (userAccountFlow == null || !userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_FINISH)) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + exchangeFlowId + " 异常." + "流水状态为:" + status);
            return false;
        }
        //2.验证订阅流水是否已经置位
        UserBuyRecommend footballProgramLog = userBuyRecommendDao.getUserBuyRecommendByPk(userAccountFlow.getUserId()
                , userFootballLogId, false);
        if (footballProgramLog == null) {
            log.error("异常 购买足球预测回调 userFootballLogId:" + userFootballLogId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.校验流水金额支付前后是否一样
        if (!footballProgramLog.getPayAmount().equals(userAccountFlow.getPayAmount())) {
            log.error("订阅订单金额不一致.userFootballLogId:" + userFootballLogId + " 金额为" + footballProgramLog.getPayAmount()
                    + " userAccountFlow:" + exchangeFlowId + " 金额为:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (footballProgramLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return true;
        }
        //4.更新用户购买方案状态
        res = updateUserBuyRecommendAfterPayed(footballProgramLog.getUserId(), footballProgramLog.getProgramId(),
                false, false);
        return res;
    }

    @Transactional
    @Override
    public Boolean updateWithdrawStatusAndMission(Long userId, Mission mission) {
        Boolean res = Boolean.FALSE;
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userId, mission
                .getKeyInfo(), true);
        Integer updateRes = userBuyRecommendDao.updateWithdrawStatus(programLog.getUserId(), programLog
                .getFootballLogId(), SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_FINISH, SportsProgramConstant
                .PROGRAM_LOG_WITHDRAW_STATUS_WAIT);
        if (updateRes > 0) {
            Integer missionRes = missionDao.updateMissionStatus(mission.getMissionId(), Mission
                    .MISSION_STATUS_WITHDRAW_CASH_WAITE, Mission.MISSION_STATUS_INTI);
            if (missionRes > 0) {
                res = Boolean.TRUE;
            }
        }
        return res;
    }

    @Transactional
    @Override
    public Boolean cancelWithdrawStatusAndMission(Long userId, Mission mission) {
        Boolean res = Boolean.FALSE;
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userId, mission
                .getKeyInfo(), true);
        Integer updateRes = userBuyRecommendDao.updateWithdrawStatus(programLog.getUserId(), programLog
                .getFootballLogId(), SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_QUIT, SportsProgramConstant
                .PROGRAM_LOG_WITHDRAW_STATUS_WAIT);
        if (updateRes > 0) {
            Integer missionRes = missionDao.updateMissionStatus(mission.getMissionId(), Mission
                    .MISSION_STATUS_WITHDRAW_WAIT_REFUND, Mission.MISSION_STATUS_INTI);
            if (missionRes > 0) {
                res = Boolean.TRUE;
            }
        }
        return res;
    }

    @Override
    public Boolean transferAccount2UserByMission(Mission mission) {
        Boolean res = Boolean.FALSE;
        if (mission == null || mission.getKeyInfo() == null || !mission.getMissionType().equals(Mission
                .MISSION_TYPE_FOOTBALL_WITHDRAW) || !mission.getStatus().equals(Mission
                .MISSION_STATUS_WITHDRAW_CASH_WAITE)) {
            return res;
        }
        //1.获取log
        Long userSuffix = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userSuffix, mission
                .getKeyInfo(), false);

        if (programLog == null || !programLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES) || !programLog
                .getWithdrawStatus().equals(SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_FINISH) || programLog
                .getWithdrawAmount() == null) {
            return res;
        }
        Long withdrawAmount = programLog.getWithdrawAmount();
        //2.check分成价格
        Long calculateAmount = SportsUtils.getWithdrawAmountByDivided(programLog.getPayAmount());
        if (!calculateAmount.equals(withdrawAmount)) {
            log.error(mission.getMissionId() + "提现金额异常。计算用户分成" + calculateAmount + " 方案中提现金额：" + withdrawAmount);
            return res;
        }
        //3.给用户现金账户充值
        String payDesc = "足球推单收入";
        Long userPrefix = CommonUtil.getUserIdSuffix(programLog.getProgramId());
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(userPrefix,
                programLog.getProgramId(), false);
        Map<String, Object> payInfo = payService.fillAccount(recommend.getUserId(), "WITHDRAW" + programLog
                        .getFootballLogId(), withdrawAmount, CommonConstant.ACCOUNT_TYPE_BALANCE, null, withdrawAmount,
                payDesc, "", null);

        if (payInfo == null) {
            log.error("给用户现金账户添加提现金额流水失败.missionId:" + mission.getMissionId() + " lotteryCode:matchId" + mission
                    .getClassId());
            return res;
        }
        //4.充值成功之后更新mission
        Integer status = Integer.valueOf(payInfo.get("payStatus").toString());
        if (status.equals(ResultConstant.REPEAT_CODE) || status.equals(ResultConstant.PAY_SUCCESS_CODE)) {
            Integer updateRes = missionDao.updateMissionStatus(mission.getMissionId(), Mission
                    .MISSION_STATUS_WITHDRAW_CASH_FINISH, Mission.MISSION_STATUS_WITHDRAW_CASH_WAITE);
            if (updateRes > 0) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public Boolean canceledMatchRefund2User(Mission mission) {
        Boolean res = Boolean.FALSE;
        if (mission == null || mission.getKeyInfo() == null || !mission.getMissionType().equals(Mission
                .MISSION_TYPE_FOOTBALL_WITHDRAW) || !mission.getStatus().equals(Mission
                .MISSION_STATUS_WITHDRAW_WAIT_REFUND)) {
            return res;
        }

        //1.获取购买log
        Long userSuffix = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
        UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userSuffix, mission
                .getKeyInfo(), false);

//        if (programLog.getWithdrawStatus().equals(SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_QUIT)) {
//            log.error("足彩方案退款：" + JSONObject.toJSONString(mission));
//            return false;
//        }

        //2.给用户现金账户充值
        Long refundAmount = programLog.getPayAmount();
        String payDesc = "足球赛事延期退款";

        Map<String, Object> payInfo = payService.fillAccount(programLog.getUserId(), "REFUND" + programLog
                        .getFootballLogId(), refundAmount, CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, null, refundAmount,
                payDesc, "", null);

        //3.充值成功之后更新mission
        Integer status = Integer.valueOf(payInfo.get("payStatus").toString());
        if (status.equals(ResultConstant.REPEAT_CODE) || status.equals(ResultConstant.PAY_SUCCESS_CODE)) {
            Integer updateRes = missionDao.updateMissionStatus(mission.getMissionId(), Mission
                    .MISSION_STATUS_WITHDRAW_REFUND_FINISH, Mission.MISSION_STATUS_WITHDRAW_WAIT_REFUND);
            if (updateRes > 0) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public Boolean updatePurchaseRecommendAwardStatus(Mission mission) {
        Boolean res = false;
        String footballProgramId = mission.getKeyInfo();
        Long userId = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
        try {
            //1.check是否已经更新
            UserBuyRecommend programLog = userBuyRecommendDao.getUserBuyRecommendByPk(userId, footballProgramId, false);
            if (programLog == null) {
                return false;
            }
            Long programUserId = CommonUtil.getUserIdSuffix(programLog.getProgramId());
            UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(programUserId,
                    programLog.getProgramId(), false);
            //2.获取比赛
            Map<String, DetailMatchInfo> map = thirdHttpService.getMatchMapByMatchIds(recommend.getMatchId());
            Integer awardStatus = sportSocialService.getUserRecommendIsRight(recommend, map.get(recommend.getMatchId
                    ()));
            //
            userBuyRecommendDao.updateUserRecommendAwardStatus(userId, footballProgramId, awardStatus);
            res = true;
        } catch (Exception e) {
            log.error("计算购买中奖异常", e);
        }
        return res;
    }

    @Override
    public Boolean checkUserPurchaseTaskStatus(Long userId, Integer taskTime, Timestamp taskDate) {
        Integer count = userBuyRecommendDao.getUserPurchaseRecommendByDate(userId, 200, DateUtil.getBeginOfOneDay
                (taskDate), DateUtil.getEndOfOneDay(taskDate));
        if (count != null && count >= taskTime) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean checkUserByProgramIsRobot(Long userId, String footballLogId) {
        if (userId == null || StringUtils.isBlank(footballLogId)) {
            return Boolean.FALSE;
        }
        UserBuyRecommend buyRecommend = userBuyRecommendDao.getUserBuyRecommendByPk(userId, footballLogId, false);
        if (buyRecommend == null) {
            return Boolean.FALSE;
        }
        UserSportSocialRecommend program = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                .getUserIdSuffix(buyRecommend.getProgramId()), buyRecommend.getProgramId(), false);
        if (program == null) {
            return Boolean.FALSE;
        }
        UserInfo userInfo = userInfoDao.getUserInfo(program.getUserId());
        if (userInfo != null && userInfo.getChannelType().equals("robot")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> couponPurchaseRecommend(Long userId, String couponId, String recommendId) {
        Map<String, Object> result = new HashMap<>();

        Integer code = ResultConstant.ERROR;
        if (!userCouponService.checkCouponIsEnable(userId, couponId)) {
            result.put("code", code);
            result.put("msg", "优惠券不可用");
            return result;
        }
        Long userIdPrix = CommonUtil.getUserIdSuffix(recommendId);
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(userIdPrix,
                recommendId, false);
        if (recommend == null) {
            result.put("code", PayConstant.PAY_CHECK_ERROR_CODE);
            result.put("msg", "推荐不存在");
            return result;
        }
        if (DateUtil.compareDate(recommend.getEndTime(), new Date())) {
            result.put("code", PayConstant.PAY_CHECK_ERROR_CODE);
            result.put("msg", "方案已过期");
            return result;
        }

        //2.check用户是否已经购买
        if (checkUserPurchaseFootballProgramStatus(userId, recommendId)) {
            result.put("code", PayConstant.PAY_CHECK_ERROR_CODE);
            result.put("msg", "已购买该方案");
            return result;
        }

        UserBuyRecommend userBuyRecommend = initUserBuyRecommend(userId, null, recommend);

        String payDesc = "优惠券兑换智慧师推单";
        //1.创建优惠券支付流水
        Map<String, Object> payMap = payService.payCreateFlow(userId, couponId, userBuyRecommend.getProgramAmount(),
                CommonConstant.PAY_TYPE_COUPON, null, 0l, payDesc, null, null, null, CommonConstant
                        .PAY_OPERATE_TYPE_DEC, null);

        Map<String, Object> payInfo = PayUtil.analysisCashPayMap(payMap);
        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        //2.优惠券消费
        String flowId = String.valueOf(payInfo.get("flowId"));
        Map<String, Object> consumeCouponRes = userCouponService.consumeCoupon(userId, couponId, flowId, null);
        if (consumeCouponRes == null || consumeCouponRes.get("status").toString().equals(ResultConstant
                .COUPON_DISTRIBUTE_FAIL_STATUS)) {
            result.put("code", ResultConstant.ERROR);
            result.put("msg", "优惠券支付失败，请重试");
            return result;
        }

        // 3.支付成功 更新用户购买足球方案
        Boolean updateRes = updateUserBuyRecommendAfterPayed(userId, recommendId, true, false);

        if (updateRes) {
            // 业务处理成功
            payService.handledFlow(flowId);
            result.put("code", 0);
            result.put("msg", "购买成功");
        } else {
            log.error("已扣优惠券方案购买失败:" + JSONObject.toJSONString(payInfo));
            result.put("code", -1);
            result.put("msg", "兑换失败");
        }
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserBuyRecommendService) proxyBean;
    }
}
