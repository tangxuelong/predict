package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.SocialStatistic;

import java.sql.Timestamp;
import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_STATISTIC_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_STATISTIC_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_STATISTIC_SHARD_BY)
public interface SocialStatisticDao {
    List<SocialStatistic> getOnePeriodSocialStatistic(long gameId, String periodId, Timestamp currentStaticTime,
                                                      boolean ifContainEnd);

    SocialStatistic getSocialStatisticByIdForUpdate(Long statisticId, String periodId, boolean isLock);

    SocialStatistic getSocialStatisticByUnitKey(long gameId, String periodId, Timestamp statisticTime, Integer
            dataType);

    Integer updateSocialBigData(Long statisticId, String periodId, String socialData);

    Integer insert(SocialStatistic socialStatistic);
}
