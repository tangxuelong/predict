package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.cache.SignRewardCache;
import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.VipGoldMallInfo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class VipMemberServiceImpl implements VipMemberService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private VipMemberDao vipMemberDao;
    @Autowired
    private VipOperateFollowDao vipOperateFollowDao;
    @Autowired
    private VipIdSequenceDao vipIdSequenceDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private VipOperateFollowService vipOperateFollowService;
    @Autowired
    private UserSignService userSignService;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private PayService payService;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private CouponConfigDao couponConfigDao;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private MissionDao missionDao;

    private VipMemberService self;

    @Override
    public boolean checkUserIsVip(Long userId, Integer vipType) {
        if (userId == null) {
            return false;
        }
        VipMember userVip = getUserVipMemberRedisAndDb(userId, vipType);
        if (userVip == null) {
            return false;
        }

        if (userVip.getStatus().equals(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE) && DateUtil.compareDate(new Date()
                , userVip.getEndTime())) {
            return true;
        }
        return false;
    }

    @Override
    public VipMember getUserVipMemberRedisAndDb(Long userId, Integer vipType) {
        if (userId == null) {
            return null;
        }
        String vipKey = RedisConstant.getUserVipRedisKey(userId, vipType);
        VipMember userVip = redisService.kryoGet(vipKey, VipMember.class);
        if (userVip == null) {
            userVip = vipMemberDao.getVipMemberByUserId(userId, vipType);
            if (userVip != null) {
                int expireTime = TrendUtil.getExprieSecond(userVip.getEndTime(), 8640);
                redisService.kryoSetEx(vipKey, expireTime, userVip);
            }
        }
        return userVip;
    }

    @Override
    public Map<String, Object> getUserCenterShowInfo(Long userId, Integer versionCode, Integer clientType) {
        Map<String, Object> result = new HashMap<>();
        boolean isVip = false;
        boolean signFlag = false;
        //1.????????????????????????
        Integer signGoldCoin = SignRewardCache.getSignReward(SignRewardCache.SIGN_TYPE_COMMON, SignRewardCache
                .REWARD_SIGN_TYPE_GOLD_COIN, 1);
        String signBtnMsg = "????????????";
//        String signBtnMsg = "?????????" + signGoldCoin + CommonConstant.GOLD_COIN_MONETARY_UNIT;
        VipGoldMallInfo vipInfo = new VipGoldMallInfo("??????VIP??????", null, "", "vipPriceList");
        VipGoldMallInfo goldCoin = new VipGoldMallInfo("????????????", null, "", "goldCoinMall");
        VipGoldMallInfo wisdomCoin = new VipGoldMallInfo("?????????", null, "", "wisdomCoinMall");
        VipGoldMallInfo cash = new VipGoldMallInfo("??????", null, "", "cashCoinMall");
        if (userId != null) {
            //1.??????vip??????
            String descAd = "";
            VipMember userVip = getUserVipMemberRedisAndDb(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
            if (userVip != null && userVip.getStatus().equals(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE) && DateUtil
                    .compareDate(new Date(), userVip.getEndTime())) {
                isVip = true;
                descAd = DateUtil.formatTime(userVip.getEndTime(), "yyyy-MM-dd") + "??????";
                vipInfo.setTitle("????????????");
            }
            if (versionCode >= CommonConstant.VERSION_CODE_4_4) {
                descAd = "??????????????????";
            }
            vipInfo.setDescAd(descAd);
            //2.??????????????????
            Long goldCoinBalance = 0L;
            Long wisdomCoinBalance = 0L;
            UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_GOLD_COIN,
                    false);
            if (userAccount != null) {
                goldCoinBalance = userAccount.getAccountBalance();
            }
            UserAccount userAccount1 = userAccountDao.getUserAccountBalance(userId, CommonConstant
                    .PAY_TYPE_WISDOM_COIN, false);
            if (userAccount1 != null) {
                wisdomCoinBalance = userAccount1.getAccountBalance();
            }

            goldCoin.setDescAd(goldCoinBalance + CommonConstant.GOLD_COIN_MONETARY_UNIT);
            wisdomCoin.setDescAd(CommonUtil.convertFen2Yuan(wisdomCoinBalance) + CommonConstant
                    .GOLD_WISDOM_COIN_MONETARY_UNIT);
            //3.????????????????????????
            signFlag = userSignService.checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), CommonUtil
                    .getUserSignTypeByVersion(clientType, versionCode));
            if (signFlag) {
                signBtnMsg = "?????????";
            }

            //4.?????????????????????
            UserAccount userCashAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_BALANCE,
                    false);
            Long balance = userCashAccount == null ? 0 : userCashAccount.getAccountBalance();
            cash.setDescAd("?????????:" + CommonUtil.convertFen2Yuan(balance));
        }
        Integer withdrawSwitch = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.AUTO_WITHDRAW_SWITCH, 0);

        result.put("codeBookMsg", "?????????????????????????????????");
        result.put("goldCoinMsg", "");
        result.put("isVip", isVip);
        result.put("isSportsVip", checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS));
        result.put("signFlag", signFlag);
        result.put("vipPriceListBtn", vipInfo);
        result.put("goldCoinMallBtn", goldCoin);
        result.put("wisdomCoinMallBtn", wisdomCoin);
        result.put("cashMallBtn", cash);
        result.put("signBtnMsg", signBtnMsg);
        result.put("withdrawSwitch", withdrawSwitch);
        result.put("aboveGoldCoinMallImg", "http://sportsimg.mojieai.com/above_gold_coin_mall.png");
        return result;
    }

    @Override
    public Map<String, Object> cashPurchaseVip(Long userId, Integer payChannelId, String money, Integer dateCount,
                                               String clientIp, Integer clientId, Integer priceId, Integer sourceType,
                                               Integer activityStatus, Integer vipType, Integer bankId, String wxCode) {
        Map<String, Object> result = new HashMap<>();
        long moneyFen = CommonUtil.convertYuan2Fen(money).longValue();
        String moneyShow = moneyFen + CommonConstant.CASH_MONETARY_UNIT_FEN;
        try {
            //1.?????????????????????vip??????
            VipMember vipMember = vipMemberDao.getVipMemberByUserId(userId, vipType);
            if (vipMember == null) {
                boolean reTry = false;
                vipMember = new VipMember(VipMemberConstant.VIP_MEMBER_STATUS_DISENABLE, userId, generateVipId
                        (userId), vipType);
                try {
                    vipMemberDao.insert(vipMember);
                } catch (DuplicateKeyException e) {
                    reTry = true;
                }
                if (reTry) {
                    vipMember = vipMemberDao.getVipMemberByUserId(userId, vipType);
                }
            }
            //2.??????vip????????????
            VipOperateFollow vipOperateFollow = new VipOperateFollow();
            String vipFollowCode = vipOperateFollowService.generateVipOperateId(userId);
            vipOperateFollow.toUnpaidInstance(userId, vipFollowCode, dateCount, moneyShow, vipMember.getVipId(),
                    sourceType, vipType);
            vipOperateFollowDao.insert(vipOperateFollow);
            //3.????????????
            String payDesc = "????????????VIP";
            moneyFen = PayUtil.randomDiscountPrice(moneyFen, payChannelId);
            Map<String, Object> payMap = payService.payCreateFlow(userId, vipOperateFollow.getVipOperateCode(),
                    moneyFen, 1, payChannelId, moneyFen, payDesc, clientIp, clientId,
                    VipMemberConstant.VIP_PURCHASE_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, bankId, wxCode);
            //4.apply pay??????ios??????id
            String iosMallGoodId = "";
            if (PayChannelInfoCache.getChannelInfo(payChannelId).getChannelName().equals(CommonConstant
                    .APPLY_PAY_NAME)) {
                VipPrice vipPrice = VipPriceCache.getVipPriceById(priceId, clientId);
                iosMallGoodId = vipPrice.getIosMallId();
            }
            if (payMap != null) {
                //????????????
                takeParkActivity(userId, activityStatus, vipType);

                Integer vipOpeRes = vipOperateFollowDao.updateVipOpreateFollowIsPay(vipOperateFollow
                        .getVipOperateCode(), VipMemberConstant.VIP_IS_PAIED_NO, payMap.get("flowId").toString());
                if (vipOpeRes > 0) {
                    result.put("iosMallGoodId", iosMallGoodId);
                    result.put("flowId", payMap.get("flowId"));
                    result.putAll((Map<? extends String, ?>) payMap.get("payForToken"));
                    result.put("msg", "????????????");
                }
            }
        } catch (Exception e) {
            result.put("msg", "????????????");
            log.error("??????????????????", e);
        }
        return result;
    }

    private Boolean takeParkActivity(Long userId, Integer activityStatus, Integer vipType) {
        Boolean res = false;
        if (activityStatus != 1) {
            return res;
        }
        try {
            Integer activityId = 201803001;
            if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                activityId = 201806004;
            }
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
            if (activityService.checkActivityIsEnabled(activityId) && (activityUserInfo == null || (activityUserInfo
                    .getTotalTimes() != null && activityUserInfo.getTotalTimes() == 0))) {
                if (activityUserInfo == null) {
                    activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp
                            (), DateUtil.getCurrentTimestamp());
                    activityUserInfoDao.insert(activityUserInfo);
                }
                res = true;
            }
        } catch (Exception e) {
            res = false;
        }
        return res;
    }

    @Override
    public Map<String, Object> goldCoinPurchaseVip(Long userId, Long goldCoin, Long dateCount, String goldFlowId,
                                                   Integer sourceType, Integer vipType) {
        return noncashPurchaseVip(userId, goldCoin, dateCount, goldFlowId, sourceType, VipMemberConstant
                .VIP_FOLLOW_OPERATE_TYPE_GOLD_PURCHASE, vipType);
    }

    @Override
    public Map<String, Object> wxjsapiPurchaseVip(Long userId, Integer vipPriceId, Integer sourceType, Integer
            activityStatus, Integer vipType, Long vipPrice, Integer dateCount, String wxCode) {
        if (vipType == null) {
            vipType = VipMemberConstant.VIP_MEMBER_TYPE_DIGIT;
        }
        return cashPurchaseVip(userId, CommonConstant.WX_PAY_CHANNEL_WX_JSAPI, CommonUtil.convertFen2Yuan(vipPrice)
                        .toString(), dateCount, "127.0.0.1", CommonConstant.CLIENT_TYPE_ANDRIOD, vipPriceId,
                sourceType, activityStatus, vipType, null, wxCode);
    }

    private Map<String, Object> noncashPurchaseVip(Long userId, Long exchangeAmount, Long dateCount, String goldFlowId,
                                                   Integer sourceType, Integer operateType, Integer vipType) {
        String amountStr = VipUtils.getAmountStrByOperateType(exchangeAmount, operateType);
        //1.??????????????????  todo

        //2.????????????VIP??????
        VipMember vipMember = vipMemberDao.getVipMemberByUserId(userId, vipType);
        if (vipMember == null) {
            vipMember = new VipMember(VipMemberConstant.VIP_MEMBER_STATUS_DISENABLE, userId, generateVipId(userId), vipType);
            try {
                vipMemberDao.insert(vipMember);
            } catch (DuplicateKeyException e) {
            }
        }
        vipMember.setStatus(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE);
        //3.??????vip????????????
        VipOperateFollow vipFollow = new VipOperateFollow();
        vipFollow.toPayedInstance(vipOperateFollowService.generateVipOperateId(userId), userId, vipMember.getVipId(),
                dateCount, goldFlowId, amountStr, operateType, sourceType, vipType);

        //4.????????????????????????
        Map<String, Object> result = new HashMap<>();
        try {
            self.makeVipEffectiveInsertFollow(userId, dateCount, vipFollow);
            if (StringUtils.isNotBlank(goldFlowId)) {
                UserAccountFlow flow = userAccountFlowDao.getUserFlowByShardType(goldFlowId, CommonUtil
                        .getUserIdSuffix(userId + ""), false);
                flow.setPayId(vipFollow.getVipOperateCode());
                if (flow.getStatus().equals(1)) {
                    flow.setStatus(2);
                }
                userAccountFlowDao.update(flow);
            }
            //6.???????????????
            if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                VipMember vipMember1 = vipMemberDao.getVipMemberByUserId(userId, vipType);
                Integer endDate = Integer.valueOf(DateUtil.formatTime(vipMember1.getEndTime(), "yyyyMMdd"));
                distributeCoupon2UserByMissionVip(userId, dateCount.intValue(), endDate, goldFlowId);
            }

            updateUserVipRedis(userId, vipType);
            result.put("code", ResultConstant.SUCCESS);
            result.put("msg", "?????????" + dateCount + "?????????");
        } catch (Exception e) {
            result.put("code", ResultConstant.ERROR);
            result.put("msg", "????????????");
            log.error("?????????????????????????????????", e);
        }
        return result;
    }

    @Override
    public Map<String, Object> adminGiftVip(Long userId, Long dateCount, Integer sourceType, Integer vipType) {
        return noncashPurchaseVip(userId, 0L, dateCount, null, sourceType, VipMemberConstant
                .VIP_FOLLOW_OPERATE_TYPE_ACTIVITY_SEND, vipType);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseVip(Long userId, Integer payChannelId, Long money, Integer numbers,
                                                     Integer sourceType, Integer activityStatus, Integer vipType) {
        Integer activityId = 201803001;
        if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            activityId = 201806004;
        }
        if (activityStatus == 1) {
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
            if (activityService.checkActivityIsEnabled(activityId) && (activityUserInfo == null || (activityUserInfo
                    .getTotalTimes() != null && activityUserInfo.getTotalTimes() == 0))) {
                if (activityUserInfo == null) {
                    activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp
                            (), DateUtil.getCurrentTimestamp());
                    activityUserInfoDao.insert(activityUserInfo);
                }
            }
        }
        // 1.??????
        Map payInfo = payService.payCreateFlow(userId, null, money, CommonConstant.ACCOUNT_TYPE_WISDOM_COIN,
                payChannelId, money, "???????????????vip", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
        // 2.????????????
        if (Integer.valueOf(payInfo.get("payStatus").toString()) == ResultConstant.PAY_FAILED_CODE) {
            Map<String, Object> result = new HashMap<>();
            result.put("flag", ResultConstant.PAY_FAILED_CODE);
            result.put("msg", payInfo.get("payMsg"));
        }
        // 4 ??????????????????VIP????????????
        try {
            // ?????????
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(201803001, userId);
            Boolean insert = Boolean.FALSE;
            if (null == activityUserInfo) {
                activityUserInfo = new ActivityUserInfo(201803001, userId, 0, null, DateUtil.getCurrentTimestamp
                        (), DateUtil.getCurrentTimestamp());
                insert = Boolean.TRUE;
            }
            if (sourceType.equals(13)) {
                if (activityUserInfo.getTotalTimes() == 5) {
                    activityUserInfo.setTotalTimes(7);
                } else {
                    activityUserInfo.setTotalTimes(2);
                }
                if (insert) {
                    activityUserInfoDao.insert(activityUserInfo);
                } else {
                    activityUserInfoDao.update(activityUserInfo);
                }

            }
            // ??????
            log.info("sourceType" + sourceType.toString());
            if (sourceType.equals(23)) {
                log.info("sourceType23" + sourceType.toString());
                if (activityUserInfo.getTotalTimes() == 2) {
                    activityUserInfo.setTotalTimes(7);
                } else {
                    activityUserInfo.setTotalTimes(5);
                }
                if (insert) {
                    activityUserInfoDao.insert(activityUserInfo);
                } else {
                    activityUserInfoDao.update(activityUserInfo);
                }
                // ????????????55??????????????????
                ActivityUserInfo activityUserInfoSportsVip = activityUserInfoDao.getUserTotalTimes(201806004, userId);
                if (null == activityUserInfoSportsVip) {
                    activityUserInfoSportsVip = new ActivityUserInfo(201806004, userId, 2, null, DateUtil
                            .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                    activityUserInfoDao.insert(activityUserInfoSportsVip);
                } else {
                    activityUserInfoSportsVip.setTotalTimes(2);
                    activityUserInfoDao.update(activityUserInfoSportsVip);
                }
            }
        } catch (Exception e) {
            log.error("wisdomCoinPurchaseVip error", e);
        }

        // 3.???????????? ????????????
        return noncashPurchaseVip(userId, money, Long.valueOf(numbers), payInfo.get("flowId").toString(), sourceType,
                VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_WISDOM_PURCHASE, vipType);
    }

    @Override
    public void distributeCoupon2UserByMissionVip(Long userId, Integer dateCount, Integer endDate, String flowId) {
        if (dateCount == null || dateCount == 0) {
            return;
        }
        Integer activityId = 201806003;
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        if (activityService.checkActivityIsEnabled(activityId) && activityInfo != null && StringUtils
                .isNotBlank(activityInfo.getRemark())) {
            Timestamp endTime = DateUtil.formatString(endDate + "", "yyyyMMdd");

            Map<String, Object> couponConfigIdsMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
            String couponConfIds = couponConfigIdsMap.get("couponConfIds").toString();
            if (StringUtils.isNotBlank(couponConfIds)) {
                String[] couponConfIdArr = couponConfIds.split(CommonConstant.COMMA_SPLIT_STR);
                if (couponConfIdArr.length > 0) {
                    for (String couponConfId : couponConfIdArr) {
                        for (int i = dateCount; i > 0; i--) {
                            String beginTime = DateUtil.formatTime(DateUtil.getIntervalDays(endTime, -i), "yyyyMMdd");
                            String keyInfo = userId + ":" + activityId + ":" + couponConfId + ":" + beginTime;
                            Map<String, Object> remarkMap = new HashMap<>();

                            remarkMap.put("userId", userId);
                            remarkMap.put("couponConfigId", couponConfId);
                            remarkMap.put("flowId", flowId);
                            remarkMap.put("beginTime", beginTime);
                            String remark = JSONObject.toJSONString(remarkMap);
                            Mission mission = missionDao.getMissionByKeyInfo(keyInfo, Mission
                                    .MISSION_TYPE_DISTRIBUTE_COUPON);
                            if (mission == null) {
                                try {
                                    mission = new Mission(keyInfo, Mission.MISSION_TYPE_DISTRIBUTE_COUPON, Mission
                                            .MISSION_STATUS_INTI, DateUtil.getCurrentTimestamp(), remark, null);
                                    missionDao.insert(mission);
                                } catch (Exception e) {
                                    log.error("???????????????", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Boolean callBackMakeVipEffective(String vipFollowId, String exchangeFlowId) {

        if (StringUtils.isBlank(vipFollowId) || StringUtils.isBlank(exchangeFlowId)) {
            log.error("callBackMakeVipEffective ??????????????????vipFollowId-exchangeFlowId:" + vipFollowId + "--" +
                    exchangeFlowId);
            return false;
        }
        Long preFix = Long.parseLong(exchangeFlowId.substring(exchangeFlowId.length() - 2));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, preFix, Boolean
                .FALSE);
        //1.????????????????????????????????????????????????????????????
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("??????id:" + exchangeFlowId + " ??????." + "???????????????:" + status);
            return false;
        }
        //2.??????vip????????????????????????
        VipOperateFollow vipFollow = vipOperateFollowDao.getVipFollowByFollowIdForUpdate(vipFollowId, false);
        if (vipFollow == null) {
            log.error("??????vip???????????? vipFollowId:" + vipFollowId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.??????????????????????????????????????????
        if (!userAccountFlow.getChannel().equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID) && !vipFollow
                .getTransactionAmount().equals(userAccountFlow.getPayAmount() + CommonConstant.CASH_MONETARY_UNIT_FEN)) {
            log.error("vip?????????????????????.vipFollowId:" + vipFollowId + " ?????????" + vipFollow.getTransactionAmount() + " " +
                    "userAccountFlow:" + exchangeFlowId + " ?????????:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (vipFollow.getIsPay() != null && vipFollow.getIsPay().equals(VipMemberConstant.VIP_IS_PAIED_YES)) {
            return true;
        }
        //2.??????????????????
        self.makeVipEffective(vipFollow.getUserId(), vipFollow.getVipType(), vipFollowId, exchangeFlowId, vipFollow
                .getTransactionDays());
        //3.??????redis??????vip??????
        try {
            if (StringUtils.isNotBlank(vipFollow.getOperateDesc())) {
                Map<String, Object> remark = JSONObject.parseObject(vipFollow.getOperateDesc(), HashMap.class);
                // 4 ??????????????????VIP????????????
                if (remark.containsKey("sourceType") && remark.get("sourceType").toString().equals("13")) {
                    Integer activityId = 201803001;
                    if (vipFollow.getVipType() != null && vipFollow.getVipType().equals(VipMemberConstant
                            .VIP_MEMBER_TYPE_SPORTS)) {
                        activityId = 201806004;
                    }
                    ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId,
                            userAccountFlow.getUserId());
                    if (activityUserInfo.getTotalTimes() == 5) {
                        activityUserInfo.setTotalTimes(7);
                    } else {
                        activityUserInfo.setTotalTimes(2);
                    }
                    activityUserInfoDao.update(activityUserInfo);
                }
                // 5 ????????????????????????VIP????????????
                if (remark.containsKey("sourceType") && remark.get("sourceType").toString().equals("23")) {
                    Integer activityId = 201803001;
                    if (vipFollow.getVipType() != null && vipFollow.getVipType().equals(VipMemberConstant
                            .VIP_MEMBER_TYPE_SPORTS)) {
                        activityId = 201806004;
                    }
                    ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId,
                            userAccountFlow.getUserId());
                    if (activityUserInfo.getTotalTimes() == 2) {
                        activityUserInfo.setTotalTimes(7);
                    } else {
                        activityUserInfo.setTotalTimes(5);
                    }
                    activityUserInfoDao.update(activityUserInfo);
                    // ????????????55??????????????????
                    ActivityUserInfo activityUserInfoSportsVip = activityUserInfoDao.getUserTotalTimes(201806004,
                            userAccountFlow.getUserId());

                    if (null == activityUserInfoSportsVip) {
                        activityUserInfoSportsVip = new ActivityUserInfo(201806004, userAccountFlow.getUserId(), 2,
                                null,
                                DateUtil.getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                        activityUserInfoDao.insert(activityUserInfoSportsVip);
                    } else {
                        activityUserInfoSportsVip.setTotalTimes(2);
                        activityUserInfoDao.update(activityUserInfoSportsVip);
                    }
                }
            }
            if (vipFollow.getVipType().equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                VipMember vipMember = vipMemberDao.getVipMemberByUserId(vipFollow.getUserId(), vipFollow.getVipType());
                Integer endDate = Integer.valueOf(DateUtil.formatTime(vipMember.getEndTime(), "yyyyMMdd"));
                distributeCoupon2UserByMissionVip(vipFollow.getUserId(), vipFollow.getTransactionDays(), endDate
                        , userAccountFlow.getFlowId());
            }

        } catch (Exception e) {
            log.error("vip desc parse ex", e);
        }
        return updateUserVipRedis(vipFollow.getUserId(), vipFollow.getVipType());
    }

    @Override
    public boolean updateUserVipRedis(Long userId, Integer vipType) {
        VipMember vipMember = vipMemberDao.getVipMemberByUserId(userId, vipType);
        String key = RedisConstant.getUserVipRedisKey(userId, vipType);
        redisService.del(key);
        int expireTime = TrendUtil.getExprieSecond(vipMember.getEndTime(), 0);
        return redisService.kryoSetEx(key, expireTime, vipMember);
    }

    @Transactional
    @Override
    public void makeVipEffective(Long userId, Integer vipType, String vipOperateCode, String exchangeFlowId, Integer
            dateCount) {
        try {
            VipMember vipMember = vipMemberDao.getVipByUserIdForUpdate(userId, vipType, true);
            //1.???????????????
            Integer followRes = vipOperateFollowDao.updateVipOpreateFollowIsPay(vipOperateCode, VipMemberConstant
                    .VIP_IS_PAIED_YES, exchangeFlowId);
            if (followRes > 0) {
                //2.??????????????????
                Timestamp baseTime = DateUtil.getCurrentTimestamp();
                Timestamp beginTime = vipMember.getBeginTime();
                Timestamp endTime = vipMember.getEndTime();
                if (endTime == null || DateUtil.compareDate(endTime, baseTime)) {
                    beginTime = baseTime;
                }
                if (endTime != null && !DateUtil.compareDate(endTime, baseTime)) {
                    baseTime = endTime;
                }
                endTime = DateUtil.getEndOfOneDay(DateUtil.getIntervalDays(baseTime, dateCount));
                vipMemberDao.updateUserVipStatus(vipMember.getUserId(), vipType, VipMemberConstant
                        .VIP_MEMBER_STATUS_ENABLE, beginTime, endTime);
            }

        } catch (Exception e) {
            throw new BusinessException("????????????????????????vip??????", e);
        }
    }

    @Transactional
    @Override
    public void makeVipEffectiveInsertFollow(Long userId, Long dateCount, VipOperateFollow vipFollow) {
        try {
            VipMember vipMember = vipMemberDao.getVipByUserIdForUpdate(userId, vipFollow.getVipType(), true);

            Timestamp baseTime = DateUtil.getCurrentTimestamp();
            Timestamp beginTime = vipMember.getBeginTime();
            Timestamp endTime = vipMember.getEndTime();
            //???????????????????????????????????????????????????
            if (endTime == null || DateUtil.compareDate(endTime, baseTime)) {
                beginTime = baseTime;
            }
            //?????????????????????
            if (endTime != null && DateUtil.compareDate(baseTime, endTime)) {
                baseTime = endTime;
            }
            endTime = DateUtil.getEndOfOneDay(DateUtil.getIntervalDays(baseTime, dateCount));
            vipMemberDao.updateUserVipStatus(vipMember.getUserId(), vipFollow.getVipType(), VipMemberConstant
                    .VIP_MEMBER_STATUS_ENABLE, beginTime, endTime);
            vipFollow.setPayTime(DateUtil.getCurrentTimestamp());
            vipOperateFollowDao.insert(vipFollow);
        } catch (Exception e) {
            throw new BusinessException("????????????????????????vip??????", e);
        }
    }

    @Override
    public String generateVipId(Long userId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        String userIdStr = userId + "";
        long seq = vipIdSequenceDao.getVipIdSequence();
        String vipId = Long.parseLong(timePrefix) + "VIP" + CommonUtil.formatSequence(seq) + userIdStr.substring
                (userIdStr.length() - 2);
        return vipId;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (VipMemberService) proxyBean;
    }
}
