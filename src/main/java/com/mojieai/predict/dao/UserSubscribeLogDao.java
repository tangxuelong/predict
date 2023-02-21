package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSubscribeLog;

@TableShard(tableName = ConfigConstant.USER_SUBSCRIBE_LOG_TABLE_NAME, shardType = ConfigConstant
        .USER_SUBSCRIBE_LOG_SHARD_TYPE, shardBy = ConfigConstant.USER_SUBSCRIBE_LOG_SHARD_BY)
public interface UserSubscribeLogDao {

    UserSubscribeLog getUserSubscribeLogByPk(String subscribeId, Long userId);

    UserSubscribeLog getUserSubScribeLogByUniqueKey(Long userId, Integer programId, Long programAmount, Integer
            beginPeriod, Integer endPeriod);

    UserSubscribeLog getRepeatUserSubscribeLog(Long userId, Integer programId, Long amount, Integer beginPeriod);

    Integer insert(UserSubscribeLog userSubscribeLog);

    Integer updateUserSubscribeLogStatus(String subscribeId, Long userId, Integer setStatus, Integer originStatus);

    Integer updateUserSubscribeLogPayStatus(String subScribeId, Long userId, Integer setPayStatus, Integer
            originPayStatus);
}
