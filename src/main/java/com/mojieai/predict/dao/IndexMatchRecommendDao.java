package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.IndexMatchRecommend;

import java.sql.Timestamp;
import java.util.List;

public interface IndexMatchRecommendDao {
    List<IndexMatchRecommend> getRecommendUserByMatchId(String matchId);

    List<IndexMatchRecommend> getAllWaitCalculateRecommend();

    List<IndexMatchRecommend> getRecommendIds(String matchId, String lastIndex, Integer count);

    List<IndexMatchRecommend> slaveGetIndexMatchByTime(Timestamp beginTime);

    Integer getMatchPredictCount(String matchId);

    List<Long> slaveGetAllRecommendUserId();

    void update(IndexMatchRecommend indexMatchRecommend);

    void insert(IndexMatchRecommend indexMatchRecommend);
}
