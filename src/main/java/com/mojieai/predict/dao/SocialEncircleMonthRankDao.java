package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRank;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialEncircleMonthRankDao {
    void insert(SocialRank socialRank);

    List<SocialRank> getSocialEncircleMonthRank(Long gameId, String monthId);

    SocialRank getSocialEncircleMonthRankByUserId(Long gameId, String monthId, Long userId, Boolean isLock);

    int updateSocialEncircleMonthRank(long gameId, String monthId, Long userId, Integer userScore);

    List<Long> getSocialEncircleMonthTop(Long gameId, String monthId, int count);
}
