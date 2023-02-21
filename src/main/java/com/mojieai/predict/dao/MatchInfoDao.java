package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.MatchInfo;

import java.util.List;

public interface MatchInfoDao {

    MatchInfo getMatchInfoByMatchId(Integer matchId, Boolean isLock);

    List<MatchInfo> getMatchInfoByTagId(Integer tagId);

    List<MatchInfo> getAllNoStartMatchInfo(Integer tagId);

    List<MatchInfo> getAllTagMatchInfo();

    List<Integer> getAllTagMatchId();

    Integer update(MatchInfo matchInfo);

    Integer updateMatchTagId(Integer matchId, String oldTags, String newTags);

    Integer saveMatchRemark(Integer matchId, String oldRemark, String newRemark);

    Integer insert(MatchInfo matchInfo);
}
