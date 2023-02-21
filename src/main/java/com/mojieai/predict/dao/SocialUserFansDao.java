package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialUserFans;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_FANS_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_FOLLOW_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_FOLLOW_SHARD_BY)
public interface SocialUserFansDao {
    PaginationList<SocialUserFans> getUserFansListByPage(Long userId, Integer fansType, Integer page, Integer pageSize);

    SocialUserFans getUserFans(Long userId, Long fansUserId, Integer fansType);

    List<Long> getUserFansUserId(Long userId, Integer fansType);

    void insert(SocialUserFans socialUserFans);

    void update(SocialUserFans socialUserFans);

}
