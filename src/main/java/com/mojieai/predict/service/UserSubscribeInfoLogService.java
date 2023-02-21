package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.SubscribeProgram;
import com.mojieai.predict.entity.po.UserSubscribeLog;

import java.util.Map;

public interface UserSubscribeInfoLogService {

    Boolean updateUserSubscribeLogPayed(Long userId, Integer predictType, long gameId, String subScribeId);

    Boolean callBackMakeSubscribeEffective(String userSubscribeLogId, String exchangeFlowId);

    UserSubscribeLog produceUserSubscribeLog(Long userId, SubscribeProgram program, long payAmount);

    Boolean updateUserSubscribeInfoAfterPayed(Long userId, SubscribeProgram program, String subscribeLogId);

    Map<String,Object> givePredictStateNum2User(Long userId, SubscribeProgram program);
}
