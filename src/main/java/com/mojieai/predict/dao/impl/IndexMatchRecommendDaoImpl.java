package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.IndexMatchRecommendDao;
import com.mojieai.predict.entity.po.IndexMatchRecommend;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IndexMatchRecommendDaoImpl extends BaseDao implements IndexMatchRecommendDao {
    @Override
    public List<IndexMatchRecommend> getRecommendUserByMatchId(String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectList("IndexMatchRecommend.getRecommendUserByMatchId", params);
    }

    @Override
    public List<IndexMatchRecommend> getAllWaitCalculateRecommend() {
        return slaveSqlSessionTemplate.selectList("IndexMatchRecommend.getAllWaitCalculateRecommend");
    }

    @Override
    public List<IndexMatchRecommend> getRecommendIds(String matchId, String lastIndex, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        params.put("lastIndex", lastIndex);
        params.put("count", count);
        return sqlSessionTemplate.selectList("IndexMatchRecommend.getRecommendIds", params);
    }

    @Override
    public List<IndexMatchRecommend> slaveGetIndexMatchByTime(Timestamp beginTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", beginTime);
        return slaveSqlSessionTemplate.selectList("IndexMatchRecommend.slaveGetIndexMatchByTime", params);
    }

    @Override
    public Integer getMatchPredictCount(String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectOne("IndexMatchRecommend.getMatchPredictCount", params);
    }

    @Override
    public List<Long> slaveGetAllRecommendUserId() {
        return slaveSqlSessionTemplate.selectList("IndexMatchRecommend.slaveGetAllRecommendUserId");
    }

    @Override
    public void update(IndexMatchRecommend indexMatchRecommend) {
        sqlSessionTemplate.update("IndexMatchRecommend.update", indexMatchRecommend);
    }

    @Override
    public void insert(IndexMatchRecommend indexMatchRecommend) {
        sqlSessionTemplate.insert("IndexMatchRecommend.insert", indexMatchRecommend);
    }
}
