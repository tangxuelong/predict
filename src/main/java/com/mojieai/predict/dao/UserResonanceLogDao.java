package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserResonanceLog;

@TableShard(tableName = ConfigConstant.USER_RESONANCE_LOG_TABLE_NAME, shardType = ConfigConstant
        .USER_RESONANCE_LOG_SHARD_TYPE, shardBy = ConfigConstant.USER_RESONANCE_LOG_SHARD_BY)
public interface UserResonanceLogDao {

    UserResonanceLog getUserResonanceLogByPk(Long userId, String resonanceLogId);

    UserResonanceLog getUserResonanceLogByUnique(Long userId, long gameId, Integer startPeriod, Integer lastPeriod,
                                                 Long payAmount);

    UserResonanceLog getRepeatUserResonanceLog(Long userId, Long gameId, Long itemPrice, Integer beginPeriod);

    Integer updateUserPayStatus(String resonanceLogId, Integer setPayStatus, Long userId);

    Integer updateUserResonanceLogStatus(Long userId, String resonanceLogId, Integer setStatus, Integer originStatus);

    Integer insert(UserResonanceLog userResonanceLog);
}
