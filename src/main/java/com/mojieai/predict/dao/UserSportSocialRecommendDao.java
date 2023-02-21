package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;

import java.sql.Timestamp;
import java.util.List;

@TableShard(tableName = ConfigConstant.USER_SPORT_SOCIAL_RECOMMEND_TABLE_NAME, shardType = ConfigConstant
        .USER_SPORT_SOCIAL_RECOMMEND_SHARD_TYPE, shardBy = ConfigConstant.USER_SPORT_SOCIAL_RECOMMEND_SHARD_BY)
public interface UserSportSocialRecommendDao {
    UserSportSocialRecommend getSportSocialRecommendById(Long userIdPrefix, String recommendId, Boolean isLock);

    List<UserSportSocialRecommend> getSportSocialRecommendByMatchIdAndPlayType(Long userIdPrefix, String matchId, Integer playType);

    List<UserSportSocialRecommend> getSportSocialRecommendByMatchId(Long userIdPrefix, String matchId);

    List<UserSportSocialRecommend> getUserSportSocialRecommendByDate(Long userId, Timestamp beginTime, Timestamp
            endTime);

    List<UserSportSocialRecommend> getUserSportSocialRecommends(Long userId);

    List<UserSportSocialRecommend> getUserRecentRecommend(Long userId, Integer count);

    Integer getUserSportSocialRecommendsByTime(Long userId, Timestamp begin, Timestamp end);

    List<UserSportSocialRecommend> getUserCanPurchaseRecommend(Long userId, Integer playType);

    List<UserSportSocialRecommend> getUserSportRecommendsBySize(Long userId, String lastIndex, Integer count);

    Integer insert(UserSportSocialRecommend userSportSocialRecommend);

    void update(UserSportSocialRecommend userSportSocialRecommend);

    Integer updateSaleCount(Long userIdPrefix, String recommendId, Integer saleCount, Integer couponSaleCount);

    List<UserSportSocialRecommend> getSportSocialRecommendByUserIdMatchId(Long userId, String matchId);

    Integer getUserRecommendCount(Long userId, Timestamp beginOfOneDay, Timestamp endOfOneDay);
}
