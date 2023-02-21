package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.User;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@TableShard(tableName = ConfigConstant.USER_TABLE_NAME, shardType = ConfigConstant.USER_SHARD_TYPE,
        shardBy = ConfigConstant.USER_SHARD_BY)
public interface UserDao {
    User getUserByUserId(Long userId, Boolean isLock);

    User getUserByMobileFromOtter(String mobile);

    int update(User user);

    void insert(User user);
}
