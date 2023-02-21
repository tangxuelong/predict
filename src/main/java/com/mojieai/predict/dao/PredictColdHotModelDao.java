package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PredictColdHotModel;

public interface PredictColdHotModelDao {

    PredictColdHotModel getColdHotModelByPk(long gameId, String periodId, Integer periodCount, Integer numType);

    Integer insert(PredictColdHotModel predictColdHotModel);
}
