package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserSign;
import com.mojieai.predict.entity.po.UserSignStatistic;

import java.util.Map;

public interface UserSignService {

    Long generateSignId(Long userId);

    Map<String, Object> dailySigned(Long userId, String clientIp, Integer clientId);

    Map<String, Object> cycleSigned(Long userId, String visitorIp, Integer clientType, Integer versionCode);

    boolean checkUserSign(Long userId, String signDate, Integer signType);

    Integer updateSignRewardStatus(Long signCode, Long userId);

    void signRewardTimingCompensate();

    Map<String, Object> addSignInfoAndStatistic(UserSign userSign, UserSignStatistic userSignStatistic, boolean
            userStatisExistFlag);

    Map<String, Object> getUserSignPop(Long userId, Integer manual, String deviceId, Integer signType);
}
