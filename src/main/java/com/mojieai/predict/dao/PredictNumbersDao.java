package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PredictNumbers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PredictNumbersDao {
    int insert(PredictNumbers predictNumbers);

    List<PredictNumbers> getPredictNumsByGameId(long gameId);

    BigDecimal getLastHistoryAwardSum(long gameId, String periodId);

    PredictNumbers getPredictNums(long gameId, String periodId);

    String getPredictNumAwardLevel(long gameId, String periodId);

    String getHistoryAwardLevelSum(long gameId, String periodId);

    void updatePredictNums(PredictNumbers predictNumbers);

    Map<String, String> getAllAwardLevelStr(long gameId, String periodId);

    int updateHistoryAwardSum(long gameId, String periodId, long historyAwardSum);

    int updatePredictNumAwardLevel(long gameId, String periodId, String awardLevel, String historyAwardLevelSum);

    void updatePeriodId(long gameId, String oldPeriodId, String newPeriodId);

    List<Map<String,Object>> getPredictNumsPartInfo(long gameId, Integer count);
}
