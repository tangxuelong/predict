package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.Program;
import com.mojieai.predict.entity.po.UserProgram;

import java.util.Map;

public interface UserProgramService {

    Map<String, Object> getUserProgram(long gameId, String lastPeriodId, Long userId);

    Map<String, Object> cashPurchaseProgram(Long userId, String programId, Integer channelId, Integer bankId, String
            clientIp, Integer clientId);

    Map<String, Object> wisdomCoinPurchaseProgram(Long userId, String programId, Integer channelId);

    Boolean callBackMakeProgramEffective(String userProgramId, String exchangeFlowId);

    Boolean updateUserProgramPayed(String programId, String userProgramId);

    void refundWisdomCoin();

    UserProgram produceUserProgram(Long userId, Program program, Long payAmount);

    Boolean updateUserSubscribeInfoAfterPayed(Long userId, Program program, String userProgramId);

    void programRefundMonitor();
}
