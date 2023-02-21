package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.ListMatchInfo;
import com.mojieai.predict.entity.bo.PersistTagMatchInfoModel;
import com.mojieai.predict.entity.po.MatchInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface MatchInfoService {

    TreeSet<Map<String, Object>> getAllMatchInfoFromRedis();

    TreeSet<Map<String, Object>> rebuildMatchInfoRedis();

    //*********** db ******
    Map<String, Object> getWorldCupMatchInfo(Integer tagId);

    void buildNewMatchTagTimeLine(Integer tagId);

    Map<String, Object> getTagList();

    Map<String, Object> getTagMatches(Integer tagId);

    Map<String, Object> getTimeLineTagMatches(Integer tagId, Long userId, Long lastHistoryId, Long lastFutureId);

    void rebuildTagMatchListTimeLine();

    Boolean saveTagMatches2TimeLine(MatchInfo matchInfo, Integer tagId, Long userId);

    Boolean saveMatchInfo2Redis(MatchInfo matchInfo);

    MatchInfo getMatchInfoFromRedis(Integer matchId);

    List<Map<String, Object>> getFocusMatches();

    List<Map<String, Object>> rebuildFocusMatches();

    Map<String, Object> focusMatchHandler(Integer matchId, Integer weight, Integer ifFocus);

    Boolean saveTagMatchInfo(PersistTagMatchInfoModel persistTagMatchInfoModel);

    Boolean addTag2MatchInfo(Integer matchId, String tagIds, Integer operateType);

    Integer saveTagMatchHit2Remark(String matchId, Integer rightCount);
}
