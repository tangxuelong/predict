package com.mojieai.predict.service;

import java.util.Map;

public interface BindBankCardService {

    Map<String, Object> getBindBankCardDetail(Long userId);

    Map<String, Object> userBindBankCard(Long userId, String userName, String idCard, String mobile, String bankNo);

    Map<String, Object> getUserBankList(Long userId);

    Map<String, Object> getUserBankCardDetail(Long userId, Integer bankId);
}
