package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.ThirdUser;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@TableShard(tableName = ConfigConstant.THIRD_USER_TABLE_NAME, shardType = ConfigConstant.THIRD_USER_SHARD_TYPE,
        shardBy = ConfigConstant.THIRD_USER_SHARD_BY)
public interface ThirdUserDao {
    Long getUserIdByThird(String oauthId, Integer oauthType);

    void insert(ThirdUser thirdUser);
}
