package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.AwardInfo;

import java.util.List;

public interface AwardInfoDao {
    List<AwardInfo> getAwardInfos(Long gameId, String periodId);

    List<AwardInfo> getGameAwardInfos(Long gameId, Integer periodLoaded);

    void insert(AwardInfo awardInfo);

    AwardInfo getAwardInfo(Long gameId, String periodId, String awardLevel);

    void update(AwardInfo awardInfo);

    void addAwardInfoBatch(List<AwardInfo> awardInfos, long gameId);
}
