package com.mojieai.predict.service;

public interface UserSubscribeInfoService {

    Boolean checkUserSubscribePredict(Long userId, Integer subscribeProgramId);

    Integer rebuildUserSubscribeInfoRedis(Long userId, long gameId, Integer predictType, Integer programType);

    Integer checkUserFirstBuyStatus(long gameId, Long userId, Integer programType);

    void subscribeRefundWisdom2User();

    Integer checkIsRefund(String partMissionId);
}
