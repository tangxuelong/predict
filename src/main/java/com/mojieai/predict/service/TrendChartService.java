package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.enums.trend.ChartEnumInterface;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;

import java.util.Map;

public interface TrendChartService {
    void saveTrend2Redis(Task task, PeriodSchedule dirtyPeriodSchedule);

    Map<String, Object> getTrendListData(String lotteryClass, String playType, String chartType, int showNum);

    Map<String,Object> getBlueMatrixTrendChart(long gameId);

    void manualSaveTrend2Redis(long gameId, String periodId);

    /*手动构建制定期次的100/50/30期缓存*/
    void generate100ChartData(long gameId, String currentPeriodId, int beginPeriod, int endPeriodId, int num);

    Map<String, Object> generate100ChartContinueToRedis(long gameId, String beginPeriod, String endPeriodId, int num,
                                                        String currentPeriodId);

    void generate3LevelChartData(Long gameId, String periodId, Integer num);

    void tableChart(ChartEnumInterface chartEnum, long gameId, String currentPeriodId);
}
