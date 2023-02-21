package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserCouponFlowDao;
import com.mojieai.predict.entity.po.UserCouponFlow;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserCouponFlowDaoImpl extends BaseDao implements UserCouponFlowDao {


    @Override
    public UserCouponFlow getUserCouponFlowById(Long userId, String couponFlowId) {
        Map<String, Object> param = new HashMap<>();
        param.put("couponFlowId", couponFlowId);
        param.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserCouponFlow.getUserCouponFlowById", param);
    }

    @Override
    public UserCouponFlow getUserCouponFlowByUniqueKey(Long userId, String exchangeId, Long couponId) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("exchangeId", exchangeId);
        param.put("couponId", couponId);
        return sqlSessionTemplate.selectOne("UserCouponFlow.getUserCouponFlowByUniqueKey", param);
    }

    @Override
    public Integer insert(UserCouponFlow userCouponFlow) {
        return sqlSessionTemplate.insert("UserCouponFlow.insert", userCouponFlow);
    }
}
