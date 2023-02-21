package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.BetOrder;

import java.util.List;

public interface BetOrderDao {
    BetOrder getBetOrderByOrderId(Integer orderId, Boolean isLock);

    List<BetOrder> getBetOrdersByUserId(Long userId);

    List<BetOrder> getBetOrdersByMatchId(String matchId);

    void insert(BetOrder betOrder);

    void update(BetOrder betOrder);
}
