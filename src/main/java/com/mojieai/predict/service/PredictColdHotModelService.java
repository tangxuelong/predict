package com.mojieai.predict.service;

public interface PredictColdHotModelService {

    String getColdHotModel(long gameId, String periodId, Integer periodCount, Integer numType);
}
