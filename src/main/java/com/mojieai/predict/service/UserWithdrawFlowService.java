package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.entity.po.UserWithdrawFlow;
import com.mojieai.predict.entity.vo.ResultVo;

import java.util.List;
import java.util.Map;

public interface UserWithdrawFlowService {

    Map<String, Object> getUserWithdrawOrder(Long userId, Integer page);

    Map<String, Object> getUserWithdrawDetail(String withdrawOrderId, Long userId);

    Map<String, Object> getApplyWithdrawIndex(Long userId);

    List<Map<String, Object>> adminGetAllWaitConfirmWithdrawOrder();

    Long getUserCurrentMonthMaxWithDrawMoney(Long userId);

    Map<String, Object> createWithdrawOrder(Long userId, Integer bankId, Long withdrawAmount, Integer clientId,
                                            String clientIp, String passwrod);

    ResultVo createWithdrawOrderAndDescAccount(Long userId, UserWithdrawFlow userWithdrawFlow, UserAccountFlow userAccountFlow);

    ResultVo threePartyWithdrawSuccessHandler(String withdrawFlowId);

    ResultVo threePartyWithdrawErrorHandler(String withdrawFlowId, String errorMsg);

    ResultVo withdrawErrorHandler(Long userId, String withdrawFlowId, String errorMsg, Boolean missionFlag);

    ResultVo withdrawSuccessHandler(Long userId, String withdrawFlowId);

    ResultVo adminManualConfirmLargeWithdrawOrder(String withdrawFlowId, Integer orderStatus, String failReason);

    ResultVo confirmLargeWithdrawOrderAndAddMission(Long userId, String withdrawFlowId, Integer orderStatus, String
            remark);

    void thirdPartyPaymentTiming();

    void monitorWithdrawBalanceTiming();
}
