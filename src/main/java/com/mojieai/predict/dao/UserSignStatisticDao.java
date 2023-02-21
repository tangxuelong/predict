package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSign;
import com.mojieai.predict.entity.po.UserSignStatistic;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_SIGN_STATISTIC_TABLE_NAME, shardType = ConfigConstant
        .USER_SIGN_STATISTIC_SHARD_TYPE, shardBy = ConfigConstant.USER_SIGN_STATISTIC_SHARD_BY)
public interface UserSignStatisticDao {

    UserSignStatistic getUserSignStatisticByUserId(Long userId, Integer signType);

    Integer updateUserStatistic(UserSignStatistic userSignStatistic);

    Integer insert(UserSignStatistic userSignStatistic);
}
