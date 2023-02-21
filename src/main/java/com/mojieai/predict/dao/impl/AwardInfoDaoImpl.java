package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.AwardInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AwardInfoDaoImpl extends BaseDao implements AwardInfoDao {
    @Override
    public List<AwardInfo> getAwardInfos(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("AwardInfo.getAwardInfos", params);
    }

    @Override
    public List<AwardInfo> getGameAwardInfos(Long gameId, Integer periodLoaded) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodLoaded", periodLoaded);
        return sqlSessionTemplate.selectList("AwardInfo.getGameAwardInfos", params);
    }

    @Override
    public void insert(AwardInfo awardInfo) {
        sqlSessionTemplate.insert("AwardInfo.insert", awardInfo);
    }

    @Override
    public AwardInfo getAwardInfo(Long gameId, String periodId, String awardLevel) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("awardLevel", awardLevel);
        return sqlSessionTemplate.selectOne("AwardInfo.getAwardInfo", params);
    }

    @Override
    public void update(AwardInfo awardInfo) {
        sqlSessionTemplate.update("AwardInfo.update", awardInfo);
    }

    @Override
    public void addAwardInfoBatch(List<AwardInfo> awardInfos, long gameId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("awardInfos", awardInfos);
        sqlSessionTemplate.insert("AwardInfo.addAwardInfoBatch", params);
    }
}