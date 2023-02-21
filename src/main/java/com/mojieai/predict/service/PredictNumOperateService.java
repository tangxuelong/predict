package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.PredictSchedule;

import java.util.Map;

public interface PredictNumOperateService {
    Integer getPredictNumsCount(long gameId, String periodId);

    Map<String, Object> getOperatePredictNumRule(long gameId, String periodId);
}
