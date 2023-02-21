package com.mojieai.predict.enums.trend;

import java.util.Map;

public interface TrendEnumInterface {
    String getTableName(Long gameId);

    String[] getBalls(String winningNumber);

    String[] getBallColumns();

    void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object> lastTrend);

    String[] getExtaColumn();
}