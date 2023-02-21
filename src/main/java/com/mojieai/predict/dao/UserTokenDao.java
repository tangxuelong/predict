package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserToken;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@TableShard(tableName = ConfigConstant.USER_TOKEN_TABLE_NAME, shardType = ConfigConstant.USER_TOKEN_SHARD_TYPE,
        shardBy = ConfigConstant.USER_TOKEN_SHARD_BY)
public interface UserTokenDao {
    UserToken getTokenByUserIdByShardType(Long userId, String tokenSuffix);

    Long getUserIdByToken(String token);

    UserToken getUserTokenByToken(String token);

    int updateExpireTime(Long userId, String token, Timestamp oldExpireTime, Timestamp newExpireTime);

    void insert(UserToken userToken);
}
