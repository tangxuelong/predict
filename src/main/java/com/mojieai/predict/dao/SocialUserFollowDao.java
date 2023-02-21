package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialUserFollow;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_FOLLOW_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_FOLLOW_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_FOLLOW_SHARD_BY)
public interface SocialUserFollowDao {

    Integer getUserFollowCount(Long userId, Integer followType);

    PaginationList<SocialUserFollow> getFollowUserListByPage(Long userId, Integer followType, Integer page, Integer
            pageSize);

    List<SocialUserFollow> getFollowUserIdList(Long userId, Integer followType, Integer count, Long lastFollowUserId);

    SocialUserFollow getFollowUser(Long userId, Long followUserId, Integer followType, Boolean isLock);

    void insert(SocialUserFollow socialUserFollow);

    void update(SocialUserFollow socialUserFollow);
}
