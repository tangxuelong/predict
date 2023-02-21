package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PredictRedBall;

import java.util.List;

public interface PredictRedBallDao {
    int insert(PredictRedBall predictRedBall);

    int updateNumStrByGameIdPeriodId(long gameId, String periodId, String strNum, int strType);

    PredictRedBall getPredictRedBall(long gameId, String periodId, int strType);

    PredictRedBall getLatestPredictRedBall(long gameId, Integer strType);

    List<PredictRedBall> getPredictRedBalls(long gameId, int strType, Integer count);

    List<PredictRedBall> getAllPredictRedBall(long gameId, int strType);
}
