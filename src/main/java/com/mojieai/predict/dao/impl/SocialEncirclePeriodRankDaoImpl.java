package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialEncirclePeriodRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialEncirclePeriodRankDaoImpl extends BaseDao implements SocialEncirclePeriodRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialEncirclePeriodRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialEncirclePeriodRank(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialEncirclePeriodRank.getSocialEncirclePeriodRank", params);
    }

    @Override
    public List<Long> getSocialEncirclePeriodUserId(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialEncirclePeriodRank.getSocialEncirclePeriodUserId", params);
    }

    @Override
    public SocialRank getSocialEncirclePeriodRankByUserId(Long gameId, String periodId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialEncirclePeriodRank.getSocialEncirclePeriodRankByUserId",
                params);
    }

    @Override
    public int updateSocialEncirclePeriodRank(long gameId, String periodId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialEncirclePeriodRank.updateSocialEncirclePeriodRank", params);
    }
}
