package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSocialIntegral;

@TableShard(tableName = ConfigConstant.USER_SOCIAL_INTEGRAL_TABLE_NAME, shardType = ConfigConstant
        .USER_SOCIAL_INTEGRAL_SHARD_TYPE, shardBy = ConfigConstant.USER_SOCIAL_INTEGRAL_SHARD_BY)
public interface UserSocialIntegralDao {

    UserSocialIntegral getUserSocialIntegralByUserId(long gameId, Long userId, Boolean isLock);

    Integer insert(UserSocialIntegral userSocialIntegral);

    Integer updateUserScore(long gameId, Long userScore, Long userId);
}
