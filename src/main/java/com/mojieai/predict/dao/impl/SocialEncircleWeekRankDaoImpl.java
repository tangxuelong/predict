package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialEncircleWeekRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialEncircleWeekRankDaoImpl extends BaseDao implements SocialEncircleWeekRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialEncircleWeekRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialEncircleWeekRank(Long gameId, String weekId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        return sqlSessionTemplate.selectList("SocialEncircleWeekRank.getSocialEncircleWeekRank", params);
    }

    @Override
    public SocialRank getSocialEncircleWeekRankByUserId(Long gameId, String weekId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialEncircleWeekRank.getSocialEncircleWeekRankByUserId", params);
    }

    @Override
    public int updateSocialEncircleWeekRank(long gameId, String weekId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialEncircleWeekRank.updateSocialEncircleWeekRank", params);
    }

    @Override
    public List<Long> getSocialEncircleWeekTop(Long gameId, String weekId, int count) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("SocialEncircleWeekRank.getSocialEncircleWeekTop", params);
    }
}
