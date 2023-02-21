package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.SocialUserFollowInfo;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_FOLLOW_INFO_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_FOLLOW_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_FOLLOW_SHARD_BY)
public interface SocialUserFollowInfoDao {

    SocialUserFollowInfo getUserFollowInfo(Long userId, Integer followType);

    void insert(SocialUserFollowInfo socialUserFollowInfo);

    void update(SocialUserFollowInfo socialUserFollowInfo);
}
