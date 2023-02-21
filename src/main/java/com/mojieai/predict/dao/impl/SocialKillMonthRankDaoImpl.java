package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialKillMonthRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialKillMonthRankDaoImpl extends BaseDao implements SocialKillMonthRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialKillMonthRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialKillMonthRank(Long gameId, String monthId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        return sqlSessionTemplate.selectList("SocialKillMonthRank.getSocialKillMonthRank", params);
    }

    @Override
    public SocialRank getSocialKillMonthRankByUserId(Long gameId, String monthId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialKillMonthRank.getSocialKillMonthRankByUserId", params);
    }

    @Override
    public int updateSocialKillMonthRank(long gameId, String monthId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialKillMonthRank.updateSocialKillMonthRank", params);
    }

    @Override
    public List<Long> getSocialKillMonthTop(long gameId, String weekId, int count) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("weekId", weekId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("SocialKillMonthRank.getSocialKillMonthTop", params);
    }
}
