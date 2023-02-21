package com.mojieai.predict.dao.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserCouponDao;
import com.mojieai.predict.entity.po.UserCoupon;
import com.yeepay.shade.com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserCouponDaoImpl extends BaseDao implements UserCouponDao {
    protected Logger log = LogConstant.commonLog;

    @Override
    public UserCoupon getUserCouponByUserIdAndCouponId(Long userId, String userCouponId) {
        return getUserCouponByUserIdAndCouponId(userId, userCouponId, Boolean.FALSE);
    }

    @Override
    public List<UserCoupon> getUserCouponByUserId(Long userId) {

        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        return sqlSessionTemplate.selectList("UserCoupon.getUserCouponByUserId", param);
    }

    @Override
    public List<UserCoupon> getUserUseAbleCouponByAccessType(Long userId, Integer couponAccessType) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("accessType", couponAccessType);
        return sqlSessionTemplate.selectList("UserCoupon.getUserUseAbleCouponByAccessType", param);
    }

    @Override
    public Integer insert(UserCoupon userCoupon) {
        return sqlSessionTemplate.insert("UserCoupon.insert", userCoupon);
    }

    @Override
    public Integer updateCouponUseStatus(Long userId, String userCouponId, Integer setUseStatus, Integer oldStatus) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("couponId", userCouponId);
        param.put("setUseStatus", setUseStatus);
        param.put("oldStatus", oldStatus);
        return sqlSessionTemplate.update("UserCoupon.updateCouponUseStatus", param);
    }

    @Override
    public List<UserCoupon> getCouponHasAvailableTimesByType(Long userId, Integer accessType, Integer useStatus) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("accessType", accessType);
        params.put("useStatus", useStatus);
        return sqlSessionTemplate.selectList("UserCoupon.getCouponHasAvailableTimesByType", params);
    }

    @Override
    public int updateCouponAvailableTimes(Long userId, String couponId, Integer setAvailableTimes, Integer
            oldAvailableTimes, Integer oldUseStatus, Integer setUseStatus) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("couponId", couponId);
        params.put("setAvailableTimes", setAvailableTimes);
        params.put("oldAvailableTimes", oldAvailableTimes);
        params.put("oldUseStatus", oldUseStatus);
        params.put("setUseStatus", setUseStatus);
        return sqlSessionTemplate.update("UserCoupon.updateCouponAvailableTimes", params);
    }

    @Override
    public UserCoupon getUserCouponByUserIdAndCouponId(Long userId, String userCouponId, Boolean ifLock) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("couponId", userCouponId);
        param.put("ifLock", ifLock);
        return sqlSessionTemplate.selectOne("UserCoupon.getUserCouponByUserIdAndCouponId", param);
    }
}
