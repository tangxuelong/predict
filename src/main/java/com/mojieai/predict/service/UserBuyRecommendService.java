package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.entity.po.UserBuyRecommend;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;

import java.sql.Timestamp;
import java.util.Map;

public interface UserBuyRecommendService {

    Map<String, Object> getUserPurchaseSportRecommend(Long userId, Integer lotteryCode, String lastIndex);

    Boolean checkUserPurchaseFootballProgramStatus(Long userId, String programId);

    UserBuyRecommend initUserBuyRecommend(Long userId, Long payAmount, UserSportSocialRecommend recommend);

    Boolean updateUserBuyRecommendAfterPayed(Long userId, String goodsId, Boolean couponFlag, Boolean ifCardUnlock);

    Boolean updateFootballProgramPayed(Long userId, String sportSocialRecommendId, Boolean couponFlag, Boolean ifCardUnlock);

    Boolean footballMatchEndUpdateWithdrawStatus(Mission mission);

    Boolean footballMatchCancelUpdateWithdrawStatus(Mission mission);

    Boolean callBackMakeUserFootballRecommendEffective(String userFootballLogId, String exchangeFlowId);

    Boolean updateWithdrawStatusAndMission(Long userId, Mission mission);

    Boolean cancelWithdrawStatusAndMission(Long userId, Mission mission);

    Boolean transferAccount2UserByMission(Mission mission);

    Boolean canceledMatchRefund2User(Mission mission);

    Boolean updatePurchaseRecommendAwardStatus(Mission mission);

    Boolean checkUserPurchaseTaskStatus(Long userId, Integer taskTime, Timestamp taskDate);

    Boolean checkUserByProgramIsRobot(Long userId, String payId);

    Map<String, Object> couponPurchaseRecommend(Long userId, String couponId, String recommendId);
}
