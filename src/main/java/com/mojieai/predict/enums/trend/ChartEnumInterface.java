package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public interface ChartEnumInterface {
    Logger log = LogConstant.commonLog;

    Boolean IfStat();

    Boolean IfOpenAwardTemp();

    Boolean IfConsecutiveNumbersShow();

    ChartEnum getChartEnum();//使用哪种方式绘制

    String getChartName();

    TrendEnumInterface getTrendEnum();

    ChartEnumInterface getTrendChartEnum();//使用哪种图表关联

    Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao);

    Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num);//组合redis
}