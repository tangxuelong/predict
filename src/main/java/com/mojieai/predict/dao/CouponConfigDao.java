package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.CouponConfig;

public interface CouponConfigDao {

    CouponConfig getCouponConfigById(Long couponConfigId);

    Integer insert(CouponConfig couponConfig);
}
