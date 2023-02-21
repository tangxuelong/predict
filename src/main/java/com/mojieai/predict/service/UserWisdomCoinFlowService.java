package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserAccountFlow;

import java.sql.Timestamp;
import java.util.Map;

public interface UserWisdomCoinFlowService {

    Map<String, Object> getUserWisdomCoinFlows(Long userId, Integer page);

    Map getWisdomPriceList(Long userId, Integer clientId, Integer versionCode);

    Map<String, Object> getWxApiWisdomPriceList(Long userId, String wxCode);

    Long getOfflineAmount(Timestamp beginTime, Timestamp endTime);

    Map<String, Object> cashPurchaseWisdomCoin(Long userId, Integer payChannelId, Long amount, Long wisdomCount, String
            clientIp, Integer clientId, Integer goodsId, Integer versionCode, Integer bankId);

    Map<String, Object> cashPurchaseWisdomCoin(Long userId, Integer payChannelId, Long amount, Long wisdomCount, String
            clientIp, Integer clientId, Integer goodsId, Integer versionCode, Integer bankId, String wxCode);

    Map<String, Object> wxApiPurchaseWisdomCoin(Long userId, Long payAmount, Long wisdomCount, Integer itemId, String
            wxCode);

    Boolean callBackAddAccount(String businessFlowId, String exchangeFlowId);

    Boolean updateWisdomFlowAndAddAccount(String flowId, Long userId, Long amount, UserAccountFlow userAccountFlow);
}
