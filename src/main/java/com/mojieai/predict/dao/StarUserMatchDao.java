package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.StarUserMatch;

import java.util.List;
import java.util.Map;

public interface StarUserMatchDao {

    StarUserMatch getStarUserMatchByUniKey(Integer activityId, Long userId, Integer matchId);

    List<StarUserMatch> getNeedBuildListStarUserMatch(Integer activityId, Long userId, Integer count);

    List<Map<String,Object>> getNeedBuildListStarUserId(Integer activityId, Integer count);

    Integer updateIsRightAndAward(StarUserMatch starUserMatch);

    Integer insert(StarUserMatch starUserMatch);
}
