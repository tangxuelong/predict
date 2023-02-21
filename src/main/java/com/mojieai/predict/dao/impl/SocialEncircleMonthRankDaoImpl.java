package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialEncircleMonthRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialEncircleMonthRankDaoImpl extends BaseDao implements SocialEncircleMonthRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialEncircleMonthRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialEncircleMonthRank(Long gameId, String monthId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        return sqlSessionTemplate.selectList("SocialEncircleMonthRank.getSocialEncircleMonthRank", params);
    }

    @Override
    public SocialRank getSocialEncircleMonthRankByUserId(Long gameId, String monthId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialEncircleMonthRank.getSocialEncircleMonthRankByUserId", params);
    }

    @Override
    public int updateSocialEncircleMonthRank(long gameId, String monthId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("monthId", monthId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialEncircleMonthRank.updateSocialEncircleMonthRank", params);
    }

    @Override
    public List<Long> getSocialEncircleMonthTop(Long gameId, String monthId, int count) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("monthId", monthId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("SocialEncircleMonthRank.getSocialEncircleMonthTop", params);
    }
}
