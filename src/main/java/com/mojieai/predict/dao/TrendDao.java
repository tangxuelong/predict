package com.mojieai.predict.dao;

import java.util.Map;

public interface TrendDao {
    void insert(String tableName, Map<String, Object> trendMap);

    Map<String, Object> getTrendById(Long gameId, String periodId, String tableName);

    boolean existTrend(Long gameId, String periodId, String tableName);
}