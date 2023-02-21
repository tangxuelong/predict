package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.BetOrderDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.BetOrder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BetOrderDaoImpl extends BaseDao implements BetOrderDao {


    @Override
    public BetOrder getBetOrderByOrderId(Integer orderId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("BetOrder.getBetOrderByOrderId", params);
    }

    @Override
    public List<BetOrder> getBetOrdersByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("BetOrder.getBetOrdersByUserId", params);
    }

    @Override
    public List<BetOrder> getBetOrdersByMatchId(String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectList("BetOrder.getBetOrdersByMatchId", params);
    }

    @Override
    public void insert(BetOrder betOrder) {
        sqlSessionTemplate.insert("BetOrder.insert", betOrder);
    }

    @Override
    public void update(BetOrder betOrder) {
        sqlSessionTemplate.update("BetOrder.update", betOrder);
    }
}
