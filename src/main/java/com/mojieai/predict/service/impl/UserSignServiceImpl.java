package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.SignRewardCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ActivityInfoDao;
import com.mojieai.predict.dao.SignIdSequenceDao;
import com.mojieai.predict.dao.UserSignDao;
import com.mojieai.predict.dao.UserSignStatisticDao;
import com.mojieai.predict.entity.bo.SignRewardImg;
import com.mojieai.predict.entity.bo.UserSignResult;
import com.mojieai.predict.entity.po.ActivityInfo;
import com.mojieai.predict.entity.po.UserSign;
import com.mojieai.predict.entity.po.UserSignReward;
import com.mojieai.predict.entity.po.UserSignStatistic;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserSignService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserSignServiceImpl implements UserSignService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserSignDao userSignDao;
    @Autowired
    private SignIdSequenceDao signIdSequenceDao;
    @Autowired
    private UserSignStatisticDao userSignStatisticDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PayService payService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityInfoDao activityInfoDao;

    private UserSignService self;

    @Override
    public Map<String, Object> dailySigned(Long userId, String clientIp, Integer clientId) {
        Map<String, Object> result = new HashMap<>();
        //1.签到
        UserSignResult userSignResult = userSign(userId, CommonConstant.USER_SIGN_TYPE_DAILY);
        if (userSignResult.getCode().equals(ResultConstant.REPEAT)) {
            result.put("msg", userSignResult.getMsg());
            result.put("signFlag", true);
            return result;
        }

        if (userSignResult.getCode().equals(ResultConstant.SUCCESS)) {
            Map<String, Object> rewardRes = distributeSignReward(userSignResult.getUserSign(), clientIp, clientId);
            result.put("msg", rewardRes.get("msg"));
            result.put("signFlag", rewardRes.get("signFlag"));
        }

        return result;
    }

    @Override
    public Map<String, Object> cycleSigned(Long userId, String visitorIp, Integer clientType, Integer versionCode) {
        Map<String, Object> result = new HashMap<>();
        Integer signType = CommonConstant.USER_SIGN_TYPE_CYCLE;
        Integer activityId = 201809002;
        Boolean activityFlag = activityService.checkActivityIsEnabled(activityId);
        if (activityFlag) {
            signType = CommonConstant.USER_SIGN_TYPE_CYCLE_ACTIVITY;
        }

        //1.记录签到
        UserSignResult userSignResult = userSign(userId, signType);
        if (userSignResult.getCode().equals(ResultConstant.ERROR)) {
            result.put("msg", userSignResult.getMsg());
            result.put("signFlag", false);
            result.put("lastRewardImg", "");
            return result;
        }
        //2.派发奖励
        if (userSignResult.getCode().equals(ResultConstant.SUCCESS) || userSignResult.getCode().equals(ResultConstant
                .REPEAT)) {
            Map<String, Object> rewardRes = distributeCycleSignReward(userSignResult.getUserSign(), visitorIp,
                    clientType, versionCode, signType, activityId);
            result.put("msg", rewardRes.get("msg"));
            result.put("signFlag", rewardRes.get("signFlag"));
            result.put("lastRewardImg", rewardRes.get("lastRewardImg"));
        }
        return result;
    }

    private void saveUserSign2Redis(Long userId, String signDate, Integer signType) {
        try {
            String userSignKey = RedisConstant.getUserSignKey(userId, signDate, signType);
            int expireTime = TrendUtil.getExprieSecond(DateUtil.getEndOfOneDay(DateUtil.formatString(signDate,
                    "yyyyMMdd")), 0);
            if (expireTime > 10) {
                redisService.kryoSetEx(userSignKey, expireTime, signDate);
            }
        } catch (Exception e) {
            log.warn("刷新签到缓存失败");
        }
    }

    @Override
    public Long generateSignId(Long userId) {
        String userIdStr = userId + "";
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = signIdSequenceDao.getSignIdSequence();
        String userIdBak = userIdStr.substring(userIdStr.length() - 2);
        return Long.parseLong(timePrefix + CommonUtil.formatSequence(seq) + userIdBak);

    }

    //signDate 20171101
    @Override
    public boolean checkUserSign(Long userId, String signDate, Integer signType) {
        if (userId == null) {
            return false;
        }
        String userSignKey = RedisConstant.getUserSignKey(userId, signDate, signType);
        String date = redisService.kryoGet(userSignKey, String.class);
        if (StringUtils.isNotBlank(date)) {
            return true;
        }
        UserSign userSign = userSignDao.getUserSignByUserIdAndDate(userId, signDate, signType);
        if (userSign != null) {
            saveUserSign2Redis(userId, signDate, signType);
            return true;
        }
        return false;
    }

    @Override
    public Integer updateSignRewardStatus(Long signCode, Long userId) {
        return userSignDao.updateSignRewardStatusBySignCode(userId, signCode, CommonConstant.SIGN_IF_REWARD_YES);
    }

    @Transactional
    @Override
    public Map<String, Object> addSignInfoAndStatistic(UserSign userSign, UserSignStatistic userSignStatistic, boolean
            userStatisExistFlag) {
        Map<String, Object> res = new HashMap<>();
        res.put("signFlag", false);
        try {
            userSignDao.insert(userSign);
            if (!userStatisExistFlag) {
                userSignStatisticDao.insert(userSignStatistic);
            } else {
                userSignStatisticDao.updateUserStatistic(userSignStatistic);
            }
            res.put("signFlag", true);
            res.put("msg", "签到成功");
        } catch (DuplicateKeyException e) {
            res.put("signFlag", true);
            res.put("msg", "已经签到成功");
        }
        return res;
    }

    @Override
    public Map<String, Object> getUserSignPop(Long userId, Integer manual, String deviceId, Integer signType) {
        Map<String, Object> res = new HashMap<>();

        Integer popStatus = getPopStatus(userId, manual, deviceId, signType);
        Integer signStatus = checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), signType) ? 1 : 0;

        res.put("popStatus", popStatus);
        res.put("popContent", getUserSignPopContent(userId, signStatus, signType));
        res.put("signStatus", signStatus);
        return res;
    }

    private Integer getPopStatus(Long userId, Integer manual, String deviceId, Integer signType) {
        Integer result = 1;//0不弹 1弹
        //1.如果人工请求一定弹
        if (manual == 1) {
            return result;
        }
        //3.用户未登录不弹
        if (userId == null) {
            return 0;
        }
        //2.用户已经签到就不在弹
        if (checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), signType) && manual == 0) {
            return 0;
        }
        return 1;
    }

    private Map<String, Object> getUserSignPopContent(Long userId, Integer signStatus, Integer signType) {
        Map<String, Object> result = new HashMap<>();

        String jumpUrl = "";
        String btnMsg = "一键签到";
        if (signStatus == 1) {
            btnMsg = "已签到";
            jumpUrl = ActivityIniCache.getActivityIniValue(ActivityIniConstant.USER_SIGN_BTN_JUMP_URL);
        }

        result.put("taskAd", "每周一凌晨0点刷新");
        result.put("rewardTask", getUserSignRewardInfos(userId, signStatus, signType));
        result.put("jumpUrl", jumpUrl);
        result.put("btnMsg", btnMsg);
        return result;
    }

    private List<Map<String, Object>> getUserSignRewardInfos(Long userId, Integer signStatus, Integer signType) {
        List<Map<String, Object>> result = new ArrayList<>();
        Date currentDate = new Date();
        String beginDate = DateUtil.formatDate(DateUtil.getBeginDayOfWeek(currentDate), "yyyyMMdd");
        String endDate = DateUtil.formatDate(DateUtil.getEndDayOfWeek(currentDate), "yyyyMMdd");
        Integer signCount = 0;
        if (userId != null) {
            signCount = userSignDao.getUserSignCountByIntervalDate(userId, signType, beginDate, endDate);
        }

        List<UserSignReward> signReward = SignRewardCache.getSignReward(signType);
        for (UserSignReward tempSignReward : signReward) {
            Map<String, Object> tempMap = convertUserSignReward2Map(tempSignReward, signCount, signStatus);
            if (tempMap != null && !tempMap.isEmpty()) {
                result.add(tempMap);
            }
        }
        return result;
    }

    private Map<String, Object> convertUserSignReward2Map(UserSignReward signReward, Integer signCount, Integer
            signStatus) {
        if (signReward == null) {
            return null;
        }
        Map<String, Object> res = new HashMap<>();
        //1.判断当前奖励
        Integer futureSignCount = signCount;
        if (signStatus == 0) {
            futureSignCount += 1;
        }

        Integer current = 0;
        if (futureSignCount == 0 && signReward.getSignCount() == 1) {
            current = 1;
        } else if (futureSignCount.equals(signReward.getSignCount())) {
            current = 1;
        }
        //2.判断奖励是否已获得
        Integer status = 0;
        if (signCount >= signReward.getSignCount()) {
            status = 1;
        }

        SignRewardImg signRewardImg = getSignRewardImg(signReward.getSignType(), 201809002);

        res.put("date", signReward.getSignCount() + "天");
        res.put("img", signRewardImg.getUserSignRewardImg(signReward.getRewardType(), signReward.getSignReward()));
        res.put("title", getSignRewardMsg(signReward.getRewardType(), signReward.getSignReward()));
        res.put("status", status);//0未完成1: 已完成
        res.put("current", current);
        return res;
    }

    private String getSignRewardMsg(Integer rewardType, Integer signReward) {
        String res = "";
        if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_GOLD_COIN)) {
            res = signReward + CommonConstant.GOLD_COIN_MONETARY_UNIT;
        } else if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_WISDOM)) {
            res = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(signReward).toString()) + "元现金";
        } else if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_VIP)) {
            res = signReward + "天VIP";
        }
        return res;
    }

    @Override
    public void signRewardTimingCompensate() {
        List<UserSign> neeRewardSign = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<UserSign> temp = userSignDao.getAllNeedRewardSign(Long.valueOf(i));
            if (temp != null && temp.size() > 0) {
                neeRewardSign.addAll(temp);
            }
        }
        //todo 定时通知给金币
        //通知派发积分 todo
        Integer goldCoin = SignRewardCache.getSignReward(SignRewardCache.SIGN_TYPE_COMMON, SignRewardCache
                .REWARD_SIGN_TYPE_GOLD_COIN, 1);

    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserSignService) proxyBean;
    }

    public UserSignResult userSign(Long userId, Integer signType) {
        UserSignResult result = new UserSignResult();
        boolean userStatisExistFlag = true;
        Integer continueCount = 0;
        Integer maxCountinueSignCount;
        int totalSignCount;
        //1.查询用是不是已经签到
        if (checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), signType)) {
            UserSign userSign = userSignDao.getUserSignByUserIdAndDate(userId, DateUtil.formatDate(new Date(),
                    "yyyyMMdd"), signType);
            result.setCode(ResultConstant.REPEAT);
            result.setMsg("您今天已经签到成功");
            result.setUserSign(userSign);
            return result;
        }
        //2.查询用户是不是已经有统计了
        UserSignStatistic userSignStatistic = userSignStatisticDao.getUserSignStatisticByUserId(userId, signType);
        //3.组装用户签到统计
        if (userSignStatistic == null) {
            userSignStatistic = new UserSignStatistic();
            userSignStatistic.setUserId(userId);
            continueCount = 1;
            maxCountinueSignCount = 1;
            totalSignCount = 1;
            userStatisExistFlag = false;
        } else {
            continueCount = userSignStatistic.getContinueSignCount() == null ? 1 : userSignStatistic
                    .getContinueSignCount();
            maxCountinueSignCount = userSignStatistic.getMaxCountinueSignCount() == null ? 1 : userSignStatistic
                    .getMaxCountinueSignCount();
            //判断是否连续上了
            if (DateUtil.getDiffSeconds(DateUtil.getBeginOfToday(), DateUtil.getBeginOfOneDay(userSignStatistic
                    .getLastSignTime())) <= 86400) {
                continueCount = continueCount + 1;
            } else {
                continueCount = 1;
            }
            if (continueCount > maxCountinueSignCount) {
                maxCountinueSignCount = continueCount;
            }
            totalSignCount = userSignStatistic.getTotalSignCount() == null ? 1 : userSignStatistic.getTotalSignCount();
        }
        userSignStatistic.setContinueSignCount(continueCount);
        userSignStatistic.setLastSignTime(DateUtil.getCurrentTimestamp());
        userSignStatistic.setMaxCountinueSignCount(maxCountinueSignCount);
        userSignStatistic.setTotalSignCount(totalSignCount);
        userSignStatistic.setSignType(signType);
        //组装用户签到
        UserSign userSign = new UserSign();
        userSign.setSignCode(generateSignId(userId));
        userSign.setUserId(userId);
        userSign.setSignDate(DateUtil.formatDate(new Date(), "yyyyMMdd"));
        userSign.setIfReward(SignRewardCache.IF_REWARD_NO);
        userSign.setSignType(signType);

        Map<String, Object> res = self.addSignInfoAndStatistic(userSign, userSignStatistic, userStatisExistFlag);
        if (res == null || res.get("signFlag").equals(false)) {
            result.setMsg(res.get("msg").toString());
            result.setCode(ResultConstant.ERROR);
            return result;
        }
        result.setCode(ResultConstant.SUCCESS);
        result.setMsg(res.get("msg").toString());
        result.setUserSign(userSign);
        return result;
    }

    private Map<String, Object> distributeSignReward(UserSign userSign, String clientIp, Integer clientId) {
        Map<String, Object> result = new HashMap<>();
        Integer goldCoin = SignRewardCache.getSignReward(SignRewardCache.SIGN_TYPE_COMMON, SignRewardCache
                .REWARD_SIGN_TYPE_GOLD_COIN, 1);
        Long amount = goldCoin * 1L;
        String msg = "签到失败";
        boolean signFlag = false;
        Map<String, Object> signRes = payService.fillAccount(userSign.getUserId(), userSign.getSignCode() + "",
                amount, CommonConstant.PAY_TYPE_GOLD_COIN, null, amount, "签到", clientIp, clientId);
        if (signRes != null) {
            Integer payStatus = Integer.valueOf(signRes.get("payStatus").toString());
            if (payStatus.equals(ResultConstant.REPEAT_CODE) || payStatus.equals(ResultConstant.PAY_SUCCESS_CODE)) {
                //3.更新奖励状态
                updateSignRewardStatus(userSign.getSignCode(), userSign.getUserId());
                //4.刷新缓存
                saveUserSign2Redis(userSign.getUserId(), userSign.getSignDate(), userSign.getSignType());
                msg = "金币+" + goldCoin;
                signFlag = true;
            }
        }
        result.put("msg", msg);
        result.put("signFlag", signFlag);
        return result;
    }

    // todo 重构签到奖励派发 策略模式或者工厂
    private Map<String, Object> distributeCycleSignReward(UserSign userSign, String clientIp, Integer clientId,
                                                          Integer versionCode, Integer signType, Integer activityId) {
        Map<String, Object> result = new HashMap<>();
        if (userSign == null) {
            result.put("msg", "签到失败");
            result.put("signFlag", false);
            return result;
        }

        //1.check本周用户签到了几次
        Date currentDate = new Date();
        String beginDate = DateUtil.formatDate(DateUtil.getBeginDayOfWeek(currentDate), "yyyyMMdd");
        String endDate = DateUtil.formatDate(DateUtil.getIntervalDate(currentDate, -1), "yyyyMMdd");
        Integer userSignCount = userSignDao.getUserSignCountByIntervalDate(userSign.getUserId(), signType, beginDate,
                endDate);
        userSignCount = userSignCount == null ? 0 : userSignCount;
        Integer currentDay = Integer.valueOf(DateUtil.formatDate(currentDate, "yyyyMMdd"));
        Integer signDay = currentDay - Integer.valueOf(beginDate) + 1;
        Integer signCount = signDay;
        if ((userSignCount + 1) < signDay) {
            signCount = userSignCount + 1;
        }

        //2.获取本次应获得的奖励
        UserSignReward userSignReward = SignRewardCache.getSignReward(signType, signCount);
        if (userSignReward == null) {
            log.error("签到奖励不存在.签到次数：" + String.valueOf(signCount));
            result.put("msg", "签到失败");
            result.put("signFlag", false);
            return result;
        }
        //3.派发
        String msg = "签到失败";
        boolean distributeFlag = false;
        boolean signFlag = false;
        Long amount = Long.valueOf(userSignReward.getSignReward());
        if (userSignReward.getRewardType().equals(SignRewardCache.REWARD_SIGN_TYPE_GOLD_COIN)) {
            Map<String, Object> rewardRes = payService.fillAccount(userSign.getUserId(), userSign.getSignCode() + "",
                    amount, CommonConstant.PAY_TYPE_GOLD_COIN, null, amount, "签到", clientIp, clientId);
            if (rewardRes != null) {
                Integer payStatus = Integer.valueOf(rewardRes.get("payStatus").toString());
                if (payStatus.equals(ResultConstant.REPEAT_CODE) || payStatus.equals(ResultConstant.PAY_SUCCESS_CODE)) {
                    distributeFlag = true;
                    msg = "恭喜获得" + amount + CommonConstant.GOLD_COIN_MONETARY_UNIT;
                }
            }
        } else if (userSignReward.getRewardType().equals(SignRewardCache.REWARD_SIGN_TYPE_WISDOM)) {
            Map<String, Object> rewardRes = payService.fillAccount(userSign.getUserId(), userSign.getSignCode() + "",
                    amount, CommonConstant.PAY_TYPE_WISDOM_COIN, null, amount, "签到", clientIp, clientId);
            if (rewardRes != null) {
                Integer payStatus = Integer.valueOf(rewardRes.get("payStatus").toString());
                if (payStatus.equals(ResultConstant.REPEAT_CODE) || payStatus.equals(ResultConstant.PAY_SUCCESS_CODE)) {
                    distributeFlag = true;
                    msg = "恭喜获得" + CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(amount).toString()) +
                            CommonConstant.GOLD_WISDOM_COIN_MONETARY_UNIT;
                }
            }
        } else if (userSignReward.getRewardType().equals(SignRewardCache.REWARD_SIGN_TYPE_VIP)) {
            Map<String, Object> rewardRes = vipMemberService.adminGiftVip(userSign.getUserId(), amount,
                    VipMemberConstant.VIP_SOURCE_TYPE_SIGN_TASK, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);// TODO ：
            msg = "恭喜获得";
            if (versionCode >= CommonConstant.VERSION_CODE_4_4) {
                Map<String, Object> rewardRes1 = vipMemberService.adminGiftVip(userSign.getUserId(), amount,
                        VipMemberConstant.VIP_SOURCE_TYPE_SIGN_TASK, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS);// TODO ：
                if (rewardRes1 != null && Integer.valueOf(rewardRes1.get("code").toString()).equals(ResultConstant
                        .SUCCESS)) {
                    msg = msg + "\n" + amount + "天足彩会员";
                }
            }

            // 18/5/31  这里未来要支持会员类型可配置
            if (rewardRes != null && Integer.valueOf(rewardRes.get("code").toString()).equals(ResultConstant.SUCCESS)) {
                distributeFlag = true;
                msg = msg + "\n" + amount + "天数字彩会员";
            }
        }

        if (distributeFlag) {
            //3.更新奖励状态
            updateSignRewardStatus(userSign.getSignCode(), userSign.getUserId());
            //4.刷新缓存
            saveUserSign2Redis(userSign.getUserId(), userSign.getSignDate(), signType);
            signFlag = true;
        }

        UserSignReward reward = SignRewardCache.getSignReward(signType, userSignCount + 1);
        String lastRewardImg = "";
        if (reward != null) {
            SignRewardImg signRewardImg = getSignRewardImg(signType, activityId);
            lastRewardImg = signRewardImg.getUserSignRewardImg(reward.getRewardType(), reward.getSignReward());
        }

        result.put("msg", msg);
        result.put("signFlag", signFlag);
        result.put("lastRewardImg", lastRewardImg);
        return result;
    }

    private SignRewardImg getSignRewardImg(Integer signType, Integer activityId) {
        SignRewardImg signRewardImg = new SignRewardImg();
        if (signType.equals(CommonConstant.USER_SIGN_TYPE_CYCLE_ACTIVITY)) {
            ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
            String domain = CommonUtil.getWebDomain();
            if (StringUtils.isNotBlank(activityInfo.getRemark())) {
                Map<String, String> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
                signRewardImg.setMoreGoldCoinImg(domain + CommonUtil.getValueFromMap(remarkMap, "moreGoldCoinImg"));
                signRewardImg.setOneGoldCoinImg(domain + CommonUtil.getValueFromMap(remarkMap, "oneGoldCoinImg"));
                signRewardImg.setRedPacketImg(domain + CommonUtil.getValueFromMap(remarkMap, "redImg"));
                signRewardImg.setVipImg(domain + CommonUtil.getValueFromMap(remarkMap, "vipImg"));
                signRewardImg.setMoreRedPacketImg(domain + CommonUtil.getValueFromMap(remarkMap, "moreRedImg"));
            }
        }
        return signRewardImg;
    }
}
