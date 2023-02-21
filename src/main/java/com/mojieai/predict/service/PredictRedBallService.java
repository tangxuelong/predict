package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictSchedule;

import java.util.List;
import java.util.Map;

public interface PredictRedBallService {

    void generateRedTwentyNums(Task task, PredictSchedule dirtyPredictSchedule);

    Map<String, Object> getRedTwentyNumsByGameId(long gameId);

    Map<String, Object> getKillThreeCodeByGameId(long gameId);

    int saveKillThreeCode(long gameId, String periodId, String killNum);

    List<Map<String, Object>> refreshRedKillThreeList(long gameId);

    List<Map<String, Object>> getRedKillThreeList(long gameId, GamePeriod lastOpenPeriod, GamePeriod
            openPeriodNextPeriod, GamePeriod currentPeriod);

    int savePredictKillCodeAndBonusKillCode(GamePeriod predictPeriod, GamePeriod gamePeriod, String
            predictKillNum);

    void predictRedBallTiming();
}
