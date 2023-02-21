package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.entity.po.PredictRedBall;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PredictRedBallDaoImpl extends BaseDao implements PredictRedBallDao {
    @Override
    public int insert(PredictRedBall predictRedBall) {
        return sqlSessionTemplate.insert("PredictRedBall.insert", predictRedBall);
    }

    @Override
    public int updateNumStrByGameIdPeriodId(long gameId, String periodId, String numStr, int strType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("numStr", numStr);
        params.put("strType", strType);
        return sqlSessionTemplate.update("PredictRedBall.updateNumStrByGameIdPeriodId", params);
    }

    @Override
    public PredictRedBall getPredictRedBall(long gameId, String periodId, int strType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("strType", strType);
        return sqlSessionTemplate.selectOne("PredictRedBall.getPredictRedBall", params);
    }

    @Override
    public PredictRedBall getLatestPredictRedBall(long gameId, Integer strType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("strType", strType);
        return sqlSessionTemplate.selectOne("PredictRedBall.getLatestPredictRedBall", params);
    }

    @Override
    public List<PredictRedBall> getPredictRedBalls(long gameId, int strType, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("strType", strType);
        params.put("count", count);
        return sqlSessionTemplate.selectList("PredictRedBall.getPredictRedBalls", params);
    }

    @Override
    public List<PredictRedBall> getAllPredictRedBall(long gameId, int strType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("strType", strType);
        return sqlSessionTemplate.selectList("PredictRedBall.getAllPredictRedBall", params);
    }
}
