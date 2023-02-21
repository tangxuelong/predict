package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserSocialTaskAward;

import java.util.Map;

public interface UserSocialTaskAwardService {
    String generateTaskId(Long userId);

    void doUserSocialTask(Long gameId, String periodId, Long userId, Integer taskType, String clientIp, Integer
            clientId);

    void taskAwardCompensateTimer();

    boolean checkUserFinishTask(long gameId, String periodId, Long userId, Integer taskType);

    UserSocialTaskAward initUserSocialTask(Long gameId, String periodId, Long userId, Integer taskType);

    Map<String, Object> getEarnGoldCoinTaskList(Long userId, Integer versionCode, Integer clientType);

    Boolean recordSocialTask(Long userId, String taskType, String clientIp, Integer clientId);
}
