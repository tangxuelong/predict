package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PredictNumbersOperate;

import java.util.List;
import java.util.Map;

public interface PredictNumbersOperateDao {
    int insert(PredictNumbersOperate predictNumbers);

    List<PredictNumbersOperate> getPredictNumsByGameId(long gameId);

    Map getPredictNumsByGameIdAndPeriodId(long gameId, String periodId);

    PredictNumbersOperate getPredictNumPoByGameIdAndPeriodId(long gameId, String periodId);

    void updatePredictNumsOperate(PredictNumbersOperate predictNumbers);

    int saveOperatePredictNums(long gameId, String periodId, byte[] operateNums);

    int updateStatus(long gameId, String periodId, int status);

    int saveRuleStr(long gameId, String periodId, String ruleStr);

    List<PredictNumbersOperate> getPredictNumsByCondition(Long gameId, String minPeriodId, String maxPeriodId, String
            manualFlag);
}
