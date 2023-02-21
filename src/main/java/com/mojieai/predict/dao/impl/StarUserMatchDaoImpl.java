package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.StarUserMatchDao;
import com.mojieai.predict.entity.po.StarUserMatch;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StarUserMatchDaoImpl extends BaseDao implements StarUserMatchDao {

    @Override
    public StarUserMatch getStarUserMatchByUniKey(Integer activityId, Long userId, Integer matchId) {
        Map<String, Object> params = new HashMap<>();

        params.put("activityId", activityId);
        params.put("userId", userId);
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectOne("StarUserMatch.getStarUserMatchByUniKey", params);
    }

    @Override
    public List<StarUserMatch> getNeedBuildListStarUserMatch(Integer activityId, Long userId, Integer count) {
        Map<String, Object> params = new HashMap<>();

        params.put("activityId", activityId);
        params.put("userId", userId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("StarUserMatch.getNeedBuildListStarUserMatch", params);
    }

    @Override
    public List<Map<String,Object>> getNeedBuildListStarUserId(Integer activityId, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("count", count);
        return slaveSqlSessionTemplate.selectList("StarUserMatch.getNeedBuildListStarUserId", params);
    }

    @Override
    public Integer updateIsRightAndAward(StarUserMatch starUserMatch) {
        return sqlSessionTemplate.update("StarUserMatch.updateIsRightAndAward", starUserMatch);
    }

    @Override
    public Integer insert(StarUserMatch starUserMatch) {
        return sqlSessionTemplate.insert("StarUserMatch.insert", starUserMatch);
    }

}
