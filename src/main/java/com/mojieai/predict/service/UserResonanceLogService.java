package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.UserResonanceLog;

public interface UserResonanceLogService {

    UserResonanceLog produceUserResonanceLog(Long userId, ExchangeMall exchangeMall, Long payAmount);

    Boolean updateUserResonanceInfoAfterPayed(Long userId, long gameId, String resonanceLogId);

    Boolean callBackMakeResonanceEffective(String resonanceLogId, String exchangeFlowId);

    Boolean updateUserResonanceLogPayed(Long userId, long gameId, String resonanceLogId);
}
