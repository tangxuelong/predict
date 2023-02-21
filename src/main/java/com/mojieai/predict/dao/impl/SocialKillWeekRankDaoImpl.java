package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialKillWeekRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialKillWeekRankDaoImpl extends BaseDao implements SocialKillWeekRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialKillWeekRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialKillWeekRank(Long gameId, String weekId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        return sqlSessionTemplate.selectList("SocialKillWeekRank.getSocialKillWeekRank", params);
    }

    @Override
    public SocialRank getSocialKillWeekRankByUserId(Long gameId, String weekId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialKillWeekRank.getSocialKillWeekRankByUserId", params);
    }

    @Override
    public int updateSocialKillWeekRank(long gameId, String weekId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialKillWeekRank.updateSocialKillWeekRank", params);
    }

    @Override
    public List<Long> getSocialKillWeekTop(Long gameId, String weekId, int count) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("SocialKillWeekRank.getSocialKillWeekTop", params);
    }
}
