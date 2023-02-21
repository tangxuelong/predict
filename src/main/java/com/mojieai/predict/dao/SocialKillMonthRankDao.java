package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialKillMonthRankDao {

    void insert(SocialRank socialRank);

    List<SocialRank> getSocialKillMonthRank(Long gameId, String monthId);

    SocialRank getSocialKillMonthRankByUserId(Long gameId, String monthId, Long userId, Boolean isLock);

    int updateSocialKillMonthRank(long gameId, String monthId, Long userId, Integer userScore);

    List<Long> getSocialKillMonthTop(long gameId, String weekId, int count);
}
