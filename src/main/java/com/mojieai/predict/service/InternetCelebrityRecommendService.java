package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserCoupon;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;

import java.util.Map;

public interface InternetCelebrityRecommendService {

    Map<String, Object> celebrityAddRecommend(Long userId, Integer matchId, Integer goodsPriceId, String
            recommendInfo, String reason, String rewardDesc, String visitorIp, Integer clientType, Integer
                                                      programType, String originPrice,Integer index,String tips);

    Map<String, Object> getInternetCelebrityInfo(Long celebrityUserId, Long userId);

    void addLikeCount(String recommendId);

    Map<String, Object> getAddRecommendBaseInfo();

    Map<String, Object> getAllInternetCelebrities();

    Map<String, Object> getAllUsableInternetCelebrities(Long userId);

    String unlockCelebrityRecommendByCard(String goodsId, Long userId);

    Boolean unlockRecommend(UserCoupon userCoupon, UserSportSocialRecommend recommend);

    Map<String, Object> buyCelebrityRecommendCard(Long userId, Long couponConfigId, Integer activityId, Integer
            payChannelId, String clientIp, Integer clientId, Integer bankId);

    Boolean celebrityRecommendPayCallBack(String vipFollowId, String exchangeFlowId);
}
