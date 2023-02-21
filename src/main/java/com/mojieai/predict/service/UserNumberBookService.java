package com.mojieai.predict.service;

import java.util.Map;

public interface UserNumberBookService {

    String generateNumId(Long userId);

    Map<String, Object> getUserNumbers(long gameId, Long userId, String lastNumId);

    Map<String, Object> saveUserNumber(long gameId, String periodId, Long userId, String nums, Integer numType);

    Map<String,Object> deleteUserNum(Long userId, String numId);

    void calculateAward2NumBook(Long gameId, String periodId);
}
