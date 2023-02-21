package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.VipMember;
import com.mojieai.predict.entity.po.VipOperateFollow;

import java.util.Map;

public interface VipMemberService {

    boolean checkUserIsVip(Long userId, Integer vipType);

    VipMember getUserVipMemberRedisAndDb(Long userId, Integer vipType);

    Map<String, Object> getUserCenterShowInfo(Long userId, Integer versionCode, Integer clientType);

    Map<String, Object> cashPurchaseVip(Long userId, Integer payChannelId, String money, Integer dateCount, String
            clientIp, Integer clientId, Integer priceId, Integer sourceType, Integer activityStatus, Integer vipType,
                                        Integer bankId, String wxCode);

    Map<String, Object> goldCoinPurchaseVip(Long userId, Long goldCoin, Long dateCount, String goldFollowId,
                                            Integer sourceType, Integer vipType);

    Map<String, Object> wxjsapiPurchaseVip(Long userId, Integer vipPriceId, Integer sourceType, Integer
            activityStatus, Integer vipType, Long vipPrice, Integer dateCount, String wxCode);

    Map<String, Object> adminGiftVip(Long userId, Long dateCount, Integer sourceType, Integer vipType);

    Map<String, Object> wisdomCoinPurchaseVip(Long userId, Integer payChannelId, Long money, Integer numbers, Integer
            sourceType, Integer activityStatus, Integer vipType);

    void makeVipEffective(Long userId, Integer vipType, String vipOperateCode, String exchangeFlowId, Integer
            dateCount);

    void makeVipEffectiveInsertFollow(Long userId, Long dateCount, VipOperateFollow vipFollow);

    Boolean callBackMakeVipEffective(String vipOperateFollowId, String exchangeFlowId);

    boolean updateUserVipRedis(Long userId, Integer vipType);

    String generateVipId(Long userId);

    void distributeCoupon2UserByMissionVip(Long userId, Integer dateCount, Integer endDate, String flowId);
}
