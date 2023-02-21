package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.CouponConfigDao;
import com.mojieai.predict.entity.po.CouponConfig;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CouponConfigDaoImpl extends BaseDao implements CouponConfigDao {

    @Override
    public CouponConfig getCouponConfigById(Long couponConfigId) {
        Map<String, Object> param = new HashMap<>();
        param.put("couponId", couponConfigId);
        return sqlSessionTemplate.selectOne("CouponConfig.getCouponConfigById", param);
    }

    @Override
    public Integer insert(CouponConfig couponConfig) {
        return sqlSessionTemplate.insert("CouponConfig.insert", couponConfig);
    }
}
