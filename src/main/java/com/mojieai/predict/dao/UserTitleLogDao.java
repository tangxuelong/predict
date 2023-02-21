package com.mojieai.predict.dao;


import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserTitleLog;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_TITLE_LOG_TABLE_NAME, shardType = ConfigConstant.USER_TITLE_LOG_SHARD_TYPE,
        shardBy = ConfigConstant.USER_TITLE_LOG_SHARD_BY)
public interface UserTitleLogDao {

    UserTitleLog getUserTitleLogByDistributeId(long gameId, Long userId, Integer titleId, String dateStr);

    List<UserTitleLog> getAllNeedDistributeTitle(Integer count);

    Integer insert(UserTitleLog userTitleLog);

    Integer updateUserTitleLogDistributeStatus(Long userId, String userTitleLogId, Integer isDistribute);
}
