package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.PredictSchedule;

import java.util.Map;

public interface PredictNumService {

    Map<String, Object> getPredictIndexInfo(String userIdUniqueStr, String deviceIdUniqueStr, long gameId, String
            versionCode);

    Map<String, Object> getPredictHistoryList(long gameId);

    Map<String, Object> getPredictNums(String userIdUniqueStr, String deviceIdUniqueStr, long gameId);

    void generatePredictNums(Task task, PredictSchedule dirtyPredictSchedule);

    void updateHistoryPredict(Task task, PredictSchedule dirtyPredictSchedule);

    void checkPredictNumRedis();

    Boolean clearTimes(long gameId, String mobile, String type);

    void upatePeriodId(long gameId, String oldPeriodId, String newPeriod);

    Boolean updateHistoryPredictBonus(Task task, PredictSchedule dirtyPredictSchedule);

    void rebuildHistory(long gameId, String periodId);

    Boolean rebuild100History(Long gameId);

    void updateUserPredictMaxNums(long gameId, String periodId, Long userId, Integer addNums);

    Integer getUserPredictMaxNums(long gameId, String periodId, Long userId);

}
