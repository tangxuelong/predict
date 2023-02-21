package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialEncirclePeriodRankDao {
    void insert(SocialRank socialRank);

    List<SocialRank> getSocialEncirclePeriodRank(Long gameId, String periodId);

    List<Long> getSocialEncirclePeriodUserId(Long gameId, String periodId);

    int updateSocialEncirclePeriodRank(long gameId, String periodId, Long userId, Integer userScore);

    SocialRank getSocialEncirclePeriodRankByUserId(Long gameId, String periodId, Long userId, Boolean isLock);
}
