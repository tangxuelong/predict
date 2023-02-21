package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserResonanceInfo;

@TableShard(tableName = ConfigConstant.USER_RESONANCE_INFO_TABLE_NAME, shardType = ConfigConstant
        .USER_RESONANCE_INFO_SHARD_TYPE, shardBy = ConfigConstant.USER_RESONANCE_INFO_SHARD_BY)
public interface UserResonanceInfoDao {

    UserResonanceInfo getUserResonanceInfo(Long userId, long gameId, boolean isLock);

    Integer updateLastPeriod(Long userId, long gameId, Integer setPeriod, Integer originPeriod);

    Integer insert(UserResonanceInfo userResonanceInfo);
}
