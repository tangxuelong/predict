package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.CouponConfig;
import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.entity.po.UserCoupon;
import com.mojieai.predict.entity.po.UserCouponFlow;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface UserCouponService {

    Map<String, Object> getCouponActivityIndexInfo(Long userId);

    List<UserCoupon> getUserCouponCount(Long userId, Integer couponAccessType);

    Map<String, Object> distributeCoupon2UserByConfig(Long userId, String exchangeId, Timestamp beginTime, CouponConfig
            couponConfig);

    Map<String, Object> goldCoinExchangeCoupon(Long userId, String flowId);

    Map<String, Object> consumeCoupon(Long userId, String userCouponId, String exchangeId, Long amount);

    Boolean checkCouponIsEnable(Long userId, String userCouponId);

    Boolean generateFlowAndDistributeCoupon(UserCouponFlow userCouponFlow, UserCoupon userCoupon);

    Boolean consumeCouponAndGenerateFlow(UserCouponFlow userCouponFlow, String userCouponId);
}
