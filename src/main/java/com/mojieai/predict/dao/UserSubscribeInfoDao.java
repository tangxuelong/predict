package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSubscribeInfo;

@TableShard(tableName = ConfigConstant.USER_SUBSCRIBE_INFO_TABLE_NAME, shardType = ConfigConstant
        .USER_SUBSCRIBE_INFO_SHARD_TYPE, shardBy = ConfigConstant.USER_SUBSCRIBE_INFO_SHARD_BY)
public interface UserSubscribeInfoDao {

    UserSubscribeInfo getUserSubscribeInfoByPk(Long userId, Integer predictType, long gameId, boolean isLock);

    Integer getUserSubscribeProgramCount(long gameId, Long userId, Integer programType);

    Integer insert(UserSubscribeInfo userSubscribeInfo);

    int updatePeriodIdByPk(Long userId, Integer predictType, long gameId, Integer setPeriodId, Integer originPeriodId);
}
