package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserBankCard;

import java.util.List;

public interface UserBankCardDao {

    UserBankCard getUserBankCardById(Long userId, Integer bankId);

    UserBankCard getUserBankCardByBankNo(Long userId, String bankCard);

    Integer getUserBankCardCount(Long userId);

    List<UserBankCard> getUserAllBankCard(Long userId, Integer cardType);

    int updateBankCard(Long userId, Integer bankId, String realName, int status);

    Integer updateBankCardStatus(Long userId, Integer bankId, int status);

    Integer insert(UserBankCard userBankCard);
}
