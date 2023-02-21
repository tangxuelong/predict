package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialEncircleWeekRankDao {
    void insert(SocialRank socialRank);

    List<SocialRank> getSocialEncircleWeekRank(Long gameId, String weekId);

    SocialRank getSocialEncircleWeekRankByUserId(Long gameId, String weekId, Long userId, Boolean isLock);

    int updateSocialEncircleWeekRank(long gameId, String weekId, Long userId, Integer userScore);

    List<Long> getSocialEncircleWeekTop(Long gameId, String weekId, int count);
}
