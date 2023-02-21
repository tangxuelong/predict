package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSocialRecord;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_RECORD_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_RECORD_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_RECORD_SHARD_BY)
public interface UserSocialRecordDao {
    UserSocialRecord getUserSocialRecordByUserIdAndPeriodIdAndType(long gameId, Long userId, String periodId, Integer
            recordType);

    UserSocialRecord getLatestUserSocialRecord(long gameId, Long userId, Integer recordType);

    List<UserSocialRecord> getAllUserRecordByPeriodId(long gameId, Long userId, String periodId, Integer socialType);

    List<UserSocialRecord> getUserSocialRecordByCondition(long gameId, String periodId, Long userId, Integer
            recordType);

    String getLatestUserSocialRecordBySocialType(long gameId, Long userId, Integer socialType);

    int insert(UserSocialRecord userSocialRecord);
}
