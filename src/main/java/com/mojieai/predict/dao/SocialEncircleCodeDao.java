package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialEncircle;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_ENCIRCLE_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_ENCIRCLE_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_ENCIRCLE_SHARD_BY)
public interface SocialEncircleCodeDao {
    Integer insert(SocialEncircle socialEncircle);

    SocialEncircle getSocialEncircleByEncircleId(Long gameId, String periodId, Long encircleId);

    List<SocialEncircle> getSocialEncircleByCondition(Long gameId, String periodId, Long encircleId, Long userId,
                                                      Integer codeType, Integer isHot);

    List<SocialEncircle> getSocialEncircleByPeriodId(Long gameId, String periodId);

    List<SocialEncircle> getPeriodHotEncircle(Long gameId, String periodId, Integer isHot, Integer socialType);

    int updateRightNums(Long encircleCodeId, Integer rightNums);

    int updateUserScore(Long encircleCodeId, Integer userAwardScore);

    int updateSocialEncircle(SocialEncircle socialEncircle);

    int updateUserRankByencircleId(Long gameId, String periodId, Long encircleId, Integer followKillNums);

    int setEncircleIsHot(long gameId, String periodId, Long encircleId, Integer isHot);

    List<SocialEncircle> getUnDistributeSocialEncircle(Long gameId, String periodId);

    int updateToDistribute(Long encircleCodeId, String periodId);

    PaginationList<SocialEncircle> getSocialEncircleByPage(long gameId, String periodId, Integer page);

}
