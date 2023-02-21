package com.mojieai.predict.enums.predict;

import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.service.predict.AbstractPredictDb;
import com.mojieai.predict.service.predict.AbstractPredictView;

import java.util.List;
import java.util.Map;

public interface PickNumPredict {

    /*** view ***/
    Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls);

    Map<String, Object> getPredictInfo(AbstractPredictView predictView, Long userId);

    /*** db ***/
    Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId);

    Integer getColdHotStateNum();

    Integer getStrType();

    String getPredictName();

    Integer getNumCount();

    Integer getNumType();

    String getTitleText(String gameEn);

    Integer getPeriodShowCount();

    String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId);

    String generateNewByNumModel(Long gameId, String periodId, String numModel);
}
