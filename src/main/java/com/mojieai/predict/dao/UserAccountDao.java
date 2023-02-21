package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserAccount;

@TableShard(tableName = ConfigConstant.USER_ACCOUNT_TABLE_NAME, shardType = ConfigConstant.USER_SHARD_TYPE,
        shardBy = ConfigConstant.USER_SHARD_BY)
public interface UserAccountDao {
    UserAccount getUserAccountBalance(Long userId, Integer accountType, Boolean isLock);

    Integer update(UserAccount userAccount);

    Integer updateUserBalance(Long userId, Integer accountType, Long setBalance, Long oldBalance);

    void insert(UserAccount userAccount);
}
