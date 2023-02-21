package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.MobileUser;

@TableShard(tableName = ConfigConstant.MOBILE_USER_TABLE_NAME, shardType = ConfigConstant.MOBILE_USER_SHARD_TYPE,
        shardBy = ConfigConstant.MOBILE_USER_SHARD_BY)
public interface MobileUserDao {
    void insert(MobileUser mobileUser);

    Long getUserIdByMobile(String mobile);
}
