package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserBuyRecommend;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@TableShard(tableName = ConfigConstant.USER_BUY_RECOMMEND_TABLE_NAME, shardType = ConfigConstant
        .USER_BUY_RECOMMEND_SHARD_TYPE, shardBy = ConfigConstant.USER_BUY_RECOMMEND_SHARD_BY)
public interface UserBuyRecommendDao {

    UserBuyRecommend getUserBuyRecommendByPk(Long userId, String footballProgramLogId, boolean isLock);

    UserBuyRecommend getUserBuyRecommendByUniqueKey(Long userId, String programId, boolean isLock);

    List<UserBuyRecommend> getUserPurchaseSportRecommend(Long userId, Integer lotteryCode, String lastIndex, Integer
            count);

    Integer getUserPurchaseRecommendByDate(Long userId, Integer lotteryCode, Timestamp beginTime, Timestamp endTime);

    Integer updatePayStatus(Long userId, String footballLogId, Integer setPayStatus, Integer oldPayStatus, Boolean
            couponFlag);

    Integer updateWithdrawStatus(Long userId, String footballLogId, Integer setStatus, Integer oldStatus);

    Integer insert(UserBuyRecommend userFootballProgramLog);


    Integer updateUserRecommendAwardStatus(Long userPrefix, String footballLogId, Integer awardStatus);

    // #######    otter    ############
    Map<String, Object> getCouponAmountAndCountFromOtter(Timestamp begin, Timestamp end);

    List<Map<String,Object>> getNotCouponOrderFromOtter(Timestamp begin, Timestamp end);
}
