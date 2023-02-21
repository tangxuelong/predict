package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.UserFollowMatches;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_FOLLOW_MATCHES_TABLE_NAME, shardType = ConfigConstant
        .USER_FOLLOW_MATCHES_SHARD_TYPE, shardBy = ConfigConstant.USER_FOLLOW_MATCHES_SHARD_BY)
public interface UserFollowMatchesDao {
    List<UserFollowMatches> getUserFollowMatchesByUserId(Long userId);

    UserFollowMatches getUserFollowMatchByUserIdAndUserId(Long userId, String matchId, Boolean isLock);

    void update(UserFollowMatches userFollowMatches);

    void insert(UserFollowMatches userFollowMatches);
}
