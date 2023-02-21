package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.MatchInfoDao;
import com.mojieai.predict.entity.po.MatchInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MatchInfoDaoImpl extends BaseDao implements MatchInfoDao {

    @Override
    public MatchInfo getMatchInfoByMatchId(Integer matchId, Boolean isLock) {
        Map<String, Object> param = new HashMap<>();
        param.put("matchId", matchId);
        param.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("MatchInfo.getMatchInfoByMatchId", param);
    }

    @Override
    public List<MatchInfo> getMatchInfoByTagId(Integer tagId) {
        Map<String, Object> params = new HashMap<>();
        params.put("tagId", tagId);
        return slaveSqlSessionTemplate.selectList("MatchInfo.getMatchInfoByTagId", params);
    }

    @Override
    public List<MatchInfo> getAllNoStartMatchInfo(Integer tagId) {
        Map<String, Object> params = new HashMap<>();
        params.put("tagId", tagId);
        return slaveSqlSessionTemplate.selectList("MatchInfo.getAllNoStartMatchInfo", params);
    }

    @Override
    public List<MatchInfo> getAllTagMatchInfo() {
        return slaveSqlSessionTemplate.selectList("MatchInfo.getAllTagMatchInfo");
    }

    @Override
    public List<Integer> getAllTagMatchId(){
        return slaveSqlSessionTemplate.selectList("MatchInfo.getAllTagMatchId");
    }

    @Override
    public Integer update(MatchInfo matchInfo) {
        return sqlSessionTemplate.update("MatchInfo.update", matchInfo);
    }

    @Override
    public Integer updateMatchTagId(Integer matchId, String oldTags, String newTags) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        params.put("oldTags", oldTags);
        params.put("newTags", newTags);
        return sqlSessionTemplate.update("MatchInfo.updateMatchTagId", params);
    }

    @Override
    public Integer saveMatchRemark(Integer matchId, String oldRemark, String newRemark) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        params.put("oldRemark", oldRemark);
        params.put("newRemark", newRemark);
        return sqlSessionTemplate.update("MatchInfo.saveMatchRemark", params);
    }

    @Override
    public Integer insert(MatchInfo matchInfo) {
        return sqlSessionTemplate.insert("MatchInfo.insert", matchInfo);
    }
}
