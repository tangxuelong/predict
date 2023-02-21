package com.mojieai.predict.service;

import com.mojieai.predict.entity.dto.Result;

import java.util.Map;

public interface UserAccountService {

    /**
     * @param userId
     * @param accountType 账户类型
     * @param payAmount   分
     * @return
     */
    boolean checkUserBalance(Long userId, Integer accountType, Long payAmount);

    Map<String, Object> getUserWithdrawBalanceCenter(Long userId);

    Result fillWisdom2UserAccount(Long userId, Long wisdomAmount, Long amount, Integer exchangeType,String mobileOperate);
}
