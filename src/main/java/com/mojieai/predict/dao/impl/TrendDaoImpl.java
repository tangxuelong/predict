package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.TrendDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TrendDaoImpl extends BaseDao implements TrendDao {
    @Override
    public void insert(String tableName, Map<String, Object> trendMap) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("trendMap", trendMap);
        sqlSessionTemplate.insert("Trend.insertTrend", params);
    }

    @Override
    public Map<String, Object> getTrendById(Long gameId, String periodId, String tableName) {
        Map<String, Object> param = new HashMap<>();
        param.put("gameId", gameId);
        param.put("periodId", periodId);
        param.put("tableName", tableName);
        return sqlSessionTemplate.selectOne("Trend.getTrendById", param);
    }

    @Override
    public boolean existTrend(Long gameId, String periodId, String tableName) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("tableName", tableName);
        Map result = sqlSessionTemplate.selectOne("Trend.existTrend", params);
        return result == null ? Boolean.FALSE : Boolean.TRUE;
    }
}