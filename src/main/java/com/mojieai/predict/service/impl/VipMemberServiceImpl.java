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
        //1.获取签到加的金币
        Integer signGoldCoin = SignRewardCache.getSignReward(SignRewardCache.SIGN_TYPE_COMMON, SignRewardCache
                .REWARD_SIGN_TYPE_GOLD_COIN, 1);
        String signBtnMsg = "签到有礼";
//        String signBtnMsg = "签到＋" + signGoldCoin + CommonConstant.GOLD_COIN_MONETARY_UNIT;
        VipGoldMallInfo vipInfo = new VipGoldMallInfo("成为VIP会员", null, "", "vipPriceList");
        VipGoldMallInfo goldCoin = new VipGoldMallInfo("金币商城", null, "", "goldCoinMall");
        VipGoldMallInfo wisdomCoin = new VipGoldMallInfo("智慧币", null, "", "wisdomCoinMall");
        VipGoldMallInfo cash = new VipGoldMallInfo("提现", null, "", "cashCoinMall");
        if (userId != null) {
            //1.获取vip信息
            String descAd = "";
            VipMember userVip = getUserVipMemberRedisAndDb(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
            if (userVip != null && userVip.getStatus().equals(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE) && DateUtil
                    .compareDate(new Date(), userVip.getEndTime())) {
                isVip = true;
                descAd = DateUtil.formatTime(userVip.getEndTime(), "yyyy-MM-dd") + "过期";
                vipInfo.setTitle("会员中心");
            }
            if (versionCode >= CommonConstant.VERSION_CODE_4_4) {
                descAd = "数字彩和足彩";
            }
            vipInfo.setDescAd(descAd);
            //2.获取金豆信息
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
            //3.判断是否已经签到
            signFlag = userSignService.checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), CommonUtil
                    .getUserSignTypeByVersion(clientType, versionCode));
            if (signFlag) {
                signBtnMsg = "已签到";
            }

            //4.获取可提现金额
            UserAccount userCashAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_BALANCE,
                    false);
            Long balance = userCashAccount == null ? 0 : userCashAccount.getAccountBalance();
            cash.setDescAd("可提现:" + CommonUtil.convertFen2Yuan(balance));
        }
        Integer withdrawSwitch = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.AUTO_WITHDRAW_SWITCH, 0);

        result.put("codeBookMsg", "新增开奖，支持云端存储");
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
            //1.检验用户是否有vip信息
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
            //2.创建vip订单流水
            VipOperateFollow vipOperateFollow = new VipOperateFollow();
            String vipFollowCode = vipOperateFollowService.generateVipOperateId(userId);
            vipOperateFollow.toUnpaidInstance(userId, vipFollowCode, dateCount, moneyShow, vipMember.getVipId(),
                    sourceType, vipType);
            vipOperateFollowDao.insert(vipOperateFollow);
            //3.通知支付
            String payDesc = "现金购买VIP";
            moneyFen = PayUtil.randomDiscountPrice(moneyFen, payChannelId);
            Map<String, Object> payMap = payService.payCreateFlow(userId, vipOperateFollow.getVipOperateCode(),
                    moneyFen, 1, payChannelId, moneyFen, payDesc, clientIp, clientId,
                    VipMemberConstant.VIP_PURCHASE_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, bankId, wxCode);
            //4.apply pay获取ios商城id
            String iosMallGoodId = "";
            if (PayChannelInfoCache.getChannelInfo(payChannelId).getChannelName().equals(CommonConstant
                    .APPLY_PAY_NAME)) {
                VipPrice vipPrice = VipPriceCache.getVipPriceById(priceId, clientId);
                iosMallGoodId = vipPrice.getIosMallId();
            }
            if (payMap != null) {
                //参加活动
                takeParkActivity(userId, activityStatus, vipType);

                Integer vipOpeRes = vipOperateFollowDao.updateVipOpreateFollowIsPay(vipOperateFollow
                        .getVipOperateCode(), VipMemberConstant.VIP_IS_PAIED_NO, payMap.get("flowId").toString());
                if (vipOpeRes > 0) {
                    result.put("iosMallGoodId", iosMallGoodId);
                    result.put("flowId", payMap.get("flowId"));
                    result.putAll((Map<? extends String, ?>) payMap.get("payForToken"));
                    result.put("msg", "购买成功");
                }
            }
        } catch (Exception e) {
            result.put("msg", "购买失败");
            log.error("购买会员异常", e);
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
        //1.校验金豆流水  todo

        //2.直接创建VIP订单
        VipMember vipMember = vipMemberDao.getVipMemberByUserId(userId, vipType);
        if (vipMember == null) {
            vipMember = new VipMember(VipMemberConstant.VIP_MEMBER_STATUS_DISENABLE, userId, generateVipId(userId), vipType);
            try {
                vipMemberDao.insert(vipMember);
            } catch (DuplicateKeyException e) {
            }
        }
        vipMember.setStatus(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE);
        //3.创建vip操作流水
        VipOperateFollow vipFollow = new VipOperateFollow();
        vipFollow.toPayedInstance(vipOperateFollowService.generateVipOperateId(userId), userId, vipMember.getVipId(),
                dateCount, goldFlowId, amountStr, operateType, sourceType, vipType);

        //4.事务更新会员时间
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
            //6.赠送免单券
            if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                VipMember vipMember1 = vipMemberDao.getVipMemberByUserId(userId, vipType);
                Integer endDate = Integer.valueOf(DateUtil.formatTime(vipMember1.getEndTime(), "yyyyMMdd"));
                distributeCoupon2UserByMissionVip(userId, dateCount.intValue(), endDate, goldFlowId);
            }

            updateUserVipRedis(userId, vipType);
            result.put("code", ResultConstant.SUCCESS);
            result.put("msg", "已开通" + dateCount + "天会员");
        } catch (Exception e) {
            result.put("code", ResultConstant.ERROR);
            result.put("msg", "兑换失败");
            log.error("金币兑换会员时发生异常", e);
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
        // 1.支付
        Map payInfo = payService.payCreateFlow(userId, null, money, CommonConstant.ACCOUNT_TYPE_WISDOM_COIN,
                payChannelId, money, "智慧币购买vip", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
        // 2.支付失败
        if (Integer.valueOf(payInfo.get("payStatus").toString()) == ResultConstant.PAY_FAILED_CODE) {
            Map<String, Object> result = new HashMap<>();
            result.put("flag", ResultConstant.PAY_FAILED_CODE);
            result.put("msg", payInfo.get("payMsg"));
        }
        // 4 新手活动购买VIP处理状态
        try {
            // 数字彩
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
            // 足彩
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
                // 足彩首月55活动标志更新
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

        // 3.支付成功 发送奖励
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
                                    log.error("派发优惠券", e);
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
            log.error("callBackMakeVipEffective 必要参数为空vipFollowId-exchangeFlowId:" + vipFollowId + "--" +
                    exchangeFlowId);
            return false;
        }
        Long preFix = Long.parseLong(exchangeFlowId.substring(exchangeFlowId.length() - 2));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, preFix, Boolean
                .FALSE);
        //1.检验交易流水状态。是否已经支付或者未付款
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + exchangeFlowId + " 异常." + "流水状态为:" + status);
            return false;
        }
        //2.验证vip流水是否已经置位
        VipOperateFollow vipFollow = vipOperateFollowDao.getVipFollowByFollowIdForUpdate(vipFollowId, false);
        if (vipFollow == null) {
            log.error("异常vip购买回调 vipFollowId:" + vipFollowId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.校验流水金额支付前后是否一样
        if (!userAccountFlow.getChannel().equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID) && !vipFollow
                .getTransactionAmount().equals(userAccountFlow.getPayAmount() + CommonConstant.CASH_MONETARY_UNIT_FEN)) {
            log.error("vip订单金额不一致.vipFollowId:" + vipFollowId + " 金额为" + vipFollow.getTransactionAmount() + " " +
                    "userAccountFlow:" + exchangeFlowId + " 金额为:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (vipFollow.getIsPay() != null && vipFollow.getIsPay().equals(VipMemberConstant.VIP_IS_PAIED_YES)) {
            return true;
        }
        //2.更新会员信息
        self.makeVipEffective(vipFollow.getUserId(), vipFollow.getVipType(), vipFollowId, exchangeFlowId, vipFollow
                .getTransactionDays());
        //3.更新redis用户vip信息
        try {
            if (StringUtils.isNotBlank(vipFollow.getOperateDesc())) {
                Map<String, Object> remark = JSONObject.parseObject(vipFollow.getOperateDesc(), HashMap.class);
                // 4 新手活动购买VIP处理状态
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
                // 5 新手活动购买足彩VIP处理状态
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
                    // 足彩首月55活动标志更新
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
            //1.先更新流水
            Integer followRes = vipOperateFollowDao.updateVipOpreateFollowIsPay(vipOperateCode, VipMemberConstant
                    .VIP_IS_PAIED_YES, exchangeFlowId);
            if (followRes > 0) {
                //2.更新会员日期
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
            throw new BusinessException("直接购买立即生效vip异常", e);
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
            //如果从来没有兑换过，或者已经过期了
            if (endTime == null || DateUtil.compareDate(endTime, baseTime)) {
                beginTime = baseTime;
            }
            //如果现在是会员
            if (endTime != null && DateUtil.compareDate(baseTime, endTime)) {
                baseTime = endTime;
            }
            endTime = DateUtil.getEndOfOneDay(DateUtil.getIntervalDays(baseTime, dateCount));
            vipMemberDao.updateUserVipStatus(vipMember.getUserId(), vipFollow.getVipType(), VipMemberConstant
                    .VIP_MEMBER_STATUS_ENABLE, beginTime, endTime);
            vipFollow.setPayTime(DateUtil.getCurrentTimestamp());
            vipOperateFollowDao.insert(vipFollow);
        } catch (Exception e) {
            throw new BusinessException("直接购买立即生效vip异常", e);
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
