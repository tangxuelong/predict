package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.SocialIntegralLog;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_SOCIAL_INTEGRAL_LOG_TABLE_NAME, shardType = ConfigConstant
        .USER_SOCIAL_INTEGRAL_LOG_SHARD_TYPE, shardBy = ConfigConstant.USER_SOCIAL_INTEGRAL_LOG_SHARD_BY)
public interface SocialIntegralLogDao {

    SocialIntegralLog getSocialIntegralLogByPk(Long userId, Integer socialType, Long socialCode);

    List<String> getSomePeriodIntervalPeriodId(Long userId, long gameId, String lastPeriodId, int pageSize);

    List<SocialIntegralLog> getUserIntegralBySectionPeriodId(Long userId, long gameId, String maxPeriodId, String
            minPeriodId);

    Integer updateIntegralLogDistribute(Long userId, Integer isDistribute, Integer socialType, Long socialCode);

    Integer insert(SocialIntegralLog socialIntegralLog);
}
