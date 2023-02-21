package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialKillPeriodRankDao {

    void insert(SocialRank socialRank);

    List<SocialRank> getSocialKillPeriodRank(Long gameId, String periodId);

    List<Long> getSocialKillPeriodUserId(Long gameId, String periodId);

    int updateSocialKillPeriodRank(long gameId, String periodId, Long userId, Integer userScore);

    SocialRank getSocialKillPeriodRankByUserId(Long gameId, String periodId, Long userId, Boolean isLock);
}
