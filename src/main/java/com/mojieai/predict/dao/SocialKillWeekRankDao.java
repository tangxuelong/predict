package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialKillWeekRankDao {

    void insert(SocialRank socialRank);

    List<SocialRank> getSocialKillWeekRank(Long gameId, String periodId);

    SocialRank getSocialKillWeekRankByUserId(Long gameId, String weekId, Long userId, Boolean isLock);

    int updateSocialKillWeekRank(long gameId, String periodId, Long userId, Integer userScore);

    List<Long> getSocialKillWeekTop(Long gameId, String weekId, int count);
}
