package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.CouponConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.UserCouponService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class UserCouponServiceImpl implements UserCouponService, BeanSelfAware {
    private Logger log = LogConstant.commonLog;

    @Autowired
    private UserCouponFlowDao userCouponFlowDao;
    @Autowired
    private UserCouponFlowIdSeqDao userCouponFlowIdSeqDao;
    @Autowired
    private UserCouponIdSequenceDao userCouponIdSequenceDao;
    @Autowired
    private UserCouponDao userCouponDao;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private MissionDao missionDao;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private CouponConfigDao couponConfigDao;

    private UserCouponService self;

    @Override
    public Map<String, Object> getCouponActivityIndexInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        Integer activityId = 201806001;
        Integer couponStatus = 0;
        String imgUrl = "http://sportsimg.mojieai.com/index_coupon_icon.png";
        if (!activityService.checkActivityIsEnabled(activityId)) {
            result.put("couponImg", imgUrl);
            result.put("couponStatus", couponStatus);
            result.put("couponAd", "");
            return result;
        }
        String activityAd = "送你88元礼包：足彩预测免单+1";
        if (!activityService.checkUserTakepartActivity(userId, activityId, -1)) {
            couponStatus = 1;
        }

        result.put("couponImg", imgUrl);
        result.put("couponStatus", couponStatus);
        result.put("couponAd", activityAd);
        return result;
    }

    @Override
    public List<UserCoupon> getUserCouponCount(Long userId, Integer couponAccessType) {
        List<UserCoupon> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }
        result = userCouponDao.getUserUseAbleCouponByAccessType(userId, couponAccessType);
        return result;
    }

    @Override
    public Map<String, Object> distributeCoupon2UserByConfig(Long userId, String exchangeId, Timestamp beginTime,
                                                             CouponConfig couponConfig) {
        Map<String, Object> result = new HashMap<>();
        //1.获取用户是否已经派发过该优惠券
        UserCouponFlow userCouponFlow = userCouponFlowDao.getUserCouponFlowByUniqueKey(userId, exchangeId, couponConfig
                .getCouponId());
        if (userCouponFlow != null) {
            result.put("status", ResultConstant.COUPON_DISTRIBUTE_SUCCESS_STATUS);
            result.put("couponId", userCouponFlow.getCouponId());
            result.put("handleId", userCouponFlow.getExchangeId());
            return result;
        }
        String status = ResultConstant.COUPON_DISTRIBUTE_FAIL_STATUS;
        //2.开始派发
        userCouponFlow = new UserCouponFlow(CommonUtil.generateStrId(userId, "COUPONFLOW", userCouponFlowIdSeqDao),
                userId, exchangeId, null);
        UserCoupon userCoupon = new UserCoupon(userId, couponConfig.getCouponType(), couponConfig.getCouponId(),
                couponConfig.getCouponName(), couponConfig.getCouponDesc(), beginTime, couponConfig.getValidDay());
        if (self.generateFlowAndDistributeCoupon(userCouponFlow, userCoupon)) {
            status = ResultConstant.COUPON_DISTRIBUTE_SUCCESS_STATUS;
        }
        result.put("status", status);
        result.put("couponId", userCoupon.getCouponId());
        result.put("handleId", userCouponFlow.getExchangeId());
        return result;
    }

    @Override
    public Map<String, Object> goldCoinExchangeCoupon(Long userId, String flowId) {
        Map<String, Object> result = new HashMap<>();
        Integer activityId = 201806002;
        String msg = "";
        Integer code = ResultConstant.PAY_FAILED_CODE;

        //获取兑换活动配置信息
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        if (activityInfo != null && StringUtils.isNotBlank(activityInfo.getRemark())) {
            Map<String, Object> couponMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
            String couponIds = couponMap.get("couponConfIds").toString();
            if (StringUtils.isNotBlank(couponIds)) {
                String[] couponIdArr = couponIds.split(CommonConstant.COMMA_SPLIT_STR);
                for (String couponId : couponIdArr) {
                    String exchangeId = activityId + ":" + DateUtil.getCurrentDay();
                    Mission mission = missionDao.getMissionByKeyInfo(exchangeId, Mission
                            .MISSION_TYPE_DISTRIBUTE_COUPON);
                    if (mission == null) {
                        Map<String, Object> remarkMap = new HashMap<>();

                        remarkMap.put("userId", userId);
                        remarkMap.put("couponConfigId", couponId);
                        remarkMap.put("flowId", flowId);
                        String remark = JSONObject.toJSONString(remarkMap);
                        mission = new Mission(exchangeId, Mission.MISSION_TYPE_DISTRIBUTE_COUPON, Mission
                                .MISSION_STATUS_INTI, DateUtil.getCurrentTimestamp(), remark, null);
                        missionDao.insert(mission);
                    }
                    CouponConfig couponConfig = couponConfigDao.getCouponConfigById(Long.valueOf(couponId));
                    Map<String, Object> distributeRes = distributeCoupon2UserByConfig(userId, exchangeId, DateUtil
                            .getCurrentTimestamp(), couponConfig);
                    if (distributeRes != null && distributeRes.get("status").equals(ResultConstant
                            .COUPON_DISTRIBUTE_SUCCESS_STATUS)) {
                        missionDao.updateMissionStatus(mission.getMissionId(), Mission
                                .MISSION_STATUS_COUPON_DISTRIBUTE, Mission.MISSION_STATUS_INTI);
                        code = ResultConstant.SUCCESS;
                        msg = "兑换成功";
                    }
                }
            }
        }
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    private void updateMissionAfterDistributeSuccess(Long missionId) {
        missionDao.updateMissionStatus(missionId, Mission.MISSION_STATUS_COUPON_DISTRIBUTE, Mission.MISSION_STATUS_INTI);
    }

    @Override
    public Map<String, Object> consumeCoupon(Long userId, String userCouponId, String exchangeId, Long amount) {
        Map<String, Object> result = new HashMap<>();
        String status = "FAIL";
        //1.check优惠券是否合法
        if (!checkCouponIsEnable(userId, userCouponId)) {
            result.put("status", status);
            result.put("isRepeat", true);
            return result;
        }
        //2.
        UserCouponFlow userCouponFlow = new UserCouponFlow(CommonUtil.generateStrId(userId, "COUPONFLOW",
                userCouponFlowIdSeqDao), userId, exchangeId, userCouponId);
        if (self.consumeCouponAndGenerateFlow(userCouponFlow, userCouponId)) {
            status = "SUCCESS";
        }

        result.put("status", status);
        result.put("isRepeat", false);
        return result;
    }

    @Override
    public Boolean checkCouponIsEnable(Long userId, String userCouponId) {
        UserCoupon userCoupon = userCouponDao.getUserCouponByUserIdAndCouponId(userId, userCouponId);
        if (userCoupon == null || userCoupon.getBeginTime() == null || userCoupon.getEndTime() == null) {
            return Boolean.FALSE;
        }
        if (!userCoupon.getUseStatus().equals(CouponConstant.COUPON_USE_STATUS_USED) && DateUtil.compareDate
                (userCoupon.getBeginTime(), DateUtil.getCurrentTimestamp()) && DateUtil.compareDate(DateUtil
                .getCurrentTimestamp(), userCoupon.getEndTime())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transactional
    @Override
    public Boolean generateFlowAndDistributeCoupon(UserCouponFlow userCouponFlow, UserCoupon userCoupon) {
        Boolean result = Boolean.FALSE;
        try {
            String userCouponId = CommonUtil.generateStrId(userCoupon.getUserId(), "COUPON", userCouponIdSequenceDao);
            userCoupon.setCouponId(userCouponId);
            userCouponFlow.setCouponId(userCouponId);
            if (userCouponFlowDao.insert(userCouponFlow) > 0) {
                if (userCouponDao.insert(userCoupon) > 0) {
                    result = Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            log.error("派发优惠券异常，请查看。。。可能不要处理，前期只是用来观察。couponFlowId:" + userCouponFlow.getCouponFlowId(), e);
            throw new BusinessException("generateFlowAndDistributeCoupon error", e);
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean consumeCouponAndGenerateFlow(UserCouponFlow userCouponFlow, String userCouponId) {
        Boolean result = Boolean.FALSE;
        try {
            if (userCouponFlowDao.insert(userCouponFlow) > 0) {
                Integer updateRes = userCouponDao.updateCouponUseStatus(userCouponFlow.getUserId(), userCouponId,
                        CouponConstant.COUPON_USE_STATUS_USED, CouponConstant.COUPON_USE_STATUS_ENABLE);
                if (updateRes > 0) {
                    result = Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            log.error("消费优惠券异常。userCouponId:" + userCouponId, e);
        }
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserCouponService) proxyBean;
    }
}
