package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserTitle;

import java.sql.Timestamp;
import java.util.List;


@TableShard(tableName = ConfigConstant.USER_TITLE_TABLE_NAME, shardType = ConfigConstant.USER_TITLE_SHARD_TYPE,
        shardBy = ConfigConstant.USER_TITLE_SHARD_BY)
public interface UserTitleDao {

    UserTitle getUserTitleByUserIdAndTitleId(long gameId, Long userId, Integer titleId, boolean isLock);

    List<UserTitle> getUserAllTitle(Long userId);

    Integer insert(UserTitle userTitle);

    Integer updateUserTitleAviable(long gameId, Long userId, Integer titleId, Integer count, Timestamp endTime);
}
