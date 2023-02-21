package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialKillCode;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_KILL_CODE_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_KILL_CODE_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_KILL_CODE_SHARD_BY)
public interface SocialKillCodeDao {
    Integer insert(SocialKillCode socialKillCode);

    PaginationList<SocialKillCode> getKillNumsByEncircleIdByPage(long gameId, String periodId, Long encircleId, Long
            userId, Integer page, Integer isDistribute);

    SocialKillCode getKillNumsByEncircleIdAndUserId(long gameId, String periodId, Long encircleId, Long userId);

    List<SocialKillCode> getKillNumsByCondition(long gameId, String periodId, Long encircleId, Long userId);

    List<SocialKillCode> getKillNumsByPeriodId(long gameId, String periodId);

    SocialKillCode getKillNumsByKillCodeId(long killCodeId, String periodId);

    int updateRightNums(Long killCodeId, Integer rightNums);

    int updateUserScore(Long killCodeId, Integer userAwardScore);

    int updateSocialKillCode(SocialKillCode socialKillCode);

    List<SocialKillCode> getUnDistributeKillNums(long gameId, String periodId);

    int updateToDistribute(Long killCodeId, String periodId);
}
