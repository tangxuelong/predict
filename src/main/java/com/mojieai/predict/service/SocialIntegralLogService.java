package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.GamePeriod;

import java.util.Map;

public interface SocialIntegralLogService {

    /* 派发奖励*/
    void distributeIntegralTiming();

    boolean distributeUserIntegral(GamePeriod lastOpenPeriod);

    Boolean distributeIntegral2User(Long userId, long gameId, String periodId, Integer socialType, Long socialCode, Long
            score, String name);

    Boolean updateUserIntegralAndSetLog(long gameId, Long userId, Integer socialType, Long socialCode, Long score);

    Map getUserIntegralLogInfo(Long gameId, String lastPeriodId, Long userId);

    boolean customDistributeUserIntegral(Long userId, long score, long gameId, String periodId, String name);

    void newUserDistributeIntegral(Long userId);
}
