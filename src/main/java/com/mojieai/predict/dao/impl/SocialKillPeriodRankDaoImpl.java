package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialKillPeriodRankDao;
import com.mojieai.predict.entity.po.SocialRank;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialKillPeriodRankDaoImpl extends BaseDao implements SocialKillPeriodRankDao {
    @Override
    public void insert(SocialRank socialRank) {
        sqlSessionTemplate.insert("SocialKillPeriodRank.insert", socialRank);
    }

    @Override
    public List<SocialRank> getSocialKillPeriodRank(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialKillPeriodRank.getSocialKillPeriodRank", params);
    }

    @Override
    public List<Long> getSocialKillPeriodUserId(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialKillPeriodRank.getSocialKillPeriodUserId", params);
    }

    @Override
    public int updateSocialKillPeriodRank(long gameId, String periodId, Long userId, Integer userScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("SocialKillPeriodRank.updateSocialKillPeriodRank", params);
    }

    @Override
    public SocialRank getSocialKillPeriodRankByUserId(Long gameId, String periodId, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialKillPeriodRank.getSocialKillPeriodRankByUserId", params);
    }


}
