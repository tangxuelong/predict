package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserCoupon;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_COUPON_TABLE_NAME, shardType = ConfigConstant.USER_COUPON_SHARD_TYPE,
        shardBy = ConfigConstant.USER_COUPON_SHARD_BY)
public interface UserCouponDao {

    UserCoupon getUserCouponByUserIdAndCouponId(Long userId, String userCouponId);

    List<UserCoupon> getUserCouponByUserId(Long userId);

    List<UserCoupon> getUserUseAbleCouponByAccessType(Long userId, Integer couponAccessType);

    Integer insert(UserCoupon userCoupon);

    Integer updateCouponUseStatus(Long userId, String userCouponId, Integer setUseStatus, Integer oldStatus);

    List<UserCoupon> getCouponHasAvailableTimesByType(Long userId, Integer accessType, Integer useStatus);

    int updateCouponAvailableTimes(Long userId, String couponId, Integer setAvailableTimes, Integer oldAvailableTimes,
                                   Integer setUseStatus, Integer oldUseStatus);

    UserCoupon getUserCouponByUserIdAndCouponId(Long userId, String userCouponId, Boolean ifLock);
}
