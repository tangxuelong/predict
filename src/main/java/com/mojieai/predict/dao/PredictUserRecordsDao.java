package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PredictUserRecords;

import java.util.List;

public interface PredictUserRecordsDao {
    int insert(PredictUserRecords predictUserRecords);

    int updateNumStr(String recordId, String strNum, Integer isAward);

    List<PredictUserRecords> getAwardPredictRecords(long gameId, String periodId);

    List<PredictUserRecords> getUnAwardPredictRecords(long gameId, String periodId);
}
