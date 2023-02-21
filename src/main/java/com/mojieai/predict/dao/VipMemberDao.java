package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.VipMember;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@TableShard(tableName = ConfigConstant.VIP_MEMBER_TABLE_NAME, shardType = ConfigConstant.VIP_MEMBER_SHARD_TYPE,
        shardBy = ConfigConstant.VIP_MEMBER_SHARD_BY)
public interface VipMemberDao {
    VipMember getVipMemberByUserId(Long userId, Integer vipType);

    VipMember getVipByUserIdForUpdate(Long userId, Integer vipType, Boolean isLock);

    Integer updateUserVipStatus(Long userId, Integer vipType, Integer status, Timestamp beginTime, Timestamp endTime);

    Integer insert(VipMember vipMember);

    //OLAP 查找用户
    List<VipMember> getVipMemberByExpireDate(Timestamp date);
}
