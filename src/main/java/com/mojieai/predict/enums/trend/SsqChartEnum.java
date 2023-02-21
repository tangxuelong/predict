package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum SsqChartEnum implements ChartEnumInterface {
    AWARD(0, "AWARD") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, new String[]{TrendConstant.KEY_TREND_RED_BALL,
                    TrendConstant.KEY_TREND_BLUE_BALL});
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, RED(1, "RED") {
        @Override
        public Boolean IfStat() {
            return Boolean.TRUE;
        }

        @Override
        public Boolean IfOpenAwardTemp() {
            return Boolean.TRUE;
        }

        @Override
        public Boolean IfConsecutiveNumbersShow() {
            return Boolean.TRUE;
        }

        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.RED;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, RED, trendDao);
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
                keyTrendOpenAwardTemp[0] = TrendConstant.KEY_TREND_OPEN_AWARD_TEMP;
                keyTrendOpenAwardTemp[1] = TrendConstant.KEY_TREND_STATISTICS_TEMP;
            }
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, BLUE(2, "BLUE") {
        @Override
        public Boolean IfStat() {
            return Boolean.TRUE;
        }

        @Override
        public Boolean IfOpenAwardTemp() {
            return Boolean.TRUE;
        }

        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.BLUE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, BLUE, trendDao);
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
                keyTrendOpenAwardTemp[0] = TrendConstant.KEY_TREND_OPEN_AWARD_TEMP;
                keyTrendOpenAwardTemp[1] = TrendConstant.KEY_TREND_STATISTICS_TEMP;
            }
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, RED_COLD_HOT(3, "RED_COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.RED;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return RED;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }
    }, BLUE_COLD_HOT(4, "BLUE_COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.BLUE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return BLUE;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }
    }, LEADING_BALL(5, "LEADING_BALL") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.LEADING_BALL;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, LEADING_BALL, trendDao);
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, SWALLOW_TAIL(6, "SWALLOW_TAIL") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.SWALLOW_TAIL;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, SWALLOW_TAIL, trendDao);
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, BLUE_FORM(7, "BLUE_FORM") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.BLUE_FORM;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, BLUE_FORM, trendDao);
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, JIOU(8, "JIOU") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.JIOU;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, JIOU, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, BIG_SMALL(9, "BIG_SMALL") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.BIG_SMALL;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, BIG_SMALL, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, PRIME(10, "PRIME") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.PRIME;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, PRIME, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, ZERO_ONE_TWO_WAY(11, "ZERO_ONE_TWO_WAY") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.ZERO_ONE_TWO_WAY;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, ZERO_ONE_TWO_WAY, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, AC_VALUE(12, "AC_VALUE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.AC_VALUE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, AC_VALUE, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, SPAN_VALUE(13, "SPAN_VALUE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.SPAN_VALUE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, SPAN_VALUE, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, HEZHI(14, "HEZHI") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.HEZHI;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            Map<String, Object> resultMap = TrendUtil.generateChart(gameId, periodId, HEZHI, trendDao);
            resultMap.put("winningNumber", PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId)
                    .getWinningNumbers());
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, WEI_VALUE(15, "WEI_VALUE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return SsqTrendEnum.WEI_VALUE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            String redWinNum = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId).getWinningNumbers().split
                    (CommonConstant.COMMON_COLON_STR)[0];
            Map<String, Object> resultMap = TrendUtil.generateWeiShuChart(gameId, periodId, redWinNum, WEI_VALUE,
                    trendDao);
            return resultMap;
        }

        @Override
        public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    };

    private int chartType;

    public int getChartType() {
        return this.chartType;
    }

    private String chartName;

    public String getChartName() {
        return this.chartName;
    }

    SsqChartEnum(int chartType, String chartName) {
        this.chartType = chartType;
        this.chartName = chartName;
    }

    public static SsqChartEnum getByChartType(int chartType) {
        for (SsqChartEnum k : SsqChartEnum.values()) {
            if (chartType == k.getChartType()) {
                return k;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
        return null;
    }

    @Override
    public TrendEnumInterface getTrendEnum() {
        return null;
    }

    @Override
    public ChartEnumInterface getTrendChartEnum() {
        return null;
    }

    @Override
    public Boolean IfStat() {
        return Boolean.FALSE;
    }

    @Override
    public Boolean IfOpenAwardTemp() {
        return Boolean.FALSE;
    }

    @Override
    public Boolean IfConsecutiveNumbersShow() {
        return Boolean.FALSE;
    }

    public List<Map<String, Object>> getChart(GamePeriod lastPeriod, Long gameId, Integer showNum, RedisService
            redisService, String gameEn, Integer areaType) {
        /* 获取走势图*/
        List<Map<String, Object>> periodList = new ArrayList<>();
        if (lastPeriod != null && StringUtils.isNotEmpty(lastPeriod.getPeriodId())) {
            String[] keyTrendOpenAwardTemp = new String[]{TrendConstant.KEY_TREND_PERIOD, TrendConstant
                    .KEY_TREND_STATISTICS};
            getEnumChart(periodList, gameId, lastPeriod, showNum, redisService, areaType, keyTrendOpenAwardTemp);
        }
        return periodList;
    }

    public void getBasicChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                              RedisService redisService, String[] keyTrendOpenAwardTemp) {


        String chartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartName,
                showNum);
        Map<String, Object> chartMap = redisService.kryoGet(chartKey, HashMap.class);
        if (chartMap == null) {
            log.error("走势图获取异常" + " playType:" + " chartType:" + chartType);
        }
        periodList.addAll((Collection<? extends Map<String, Object>>) chartMap.get
                (keyTrendOpenAwardTemp[0]));
        if (chartMap.get(TrendConstant.KEY_TREND_STATISTICS) != null) {
            periodList.addAll((Collection<? extends Map<String, Object>>) chartMap.get
                    (keyTrendOpenAwardTemp[1]));
        }
    }

    public void getColdHotChart(List<Map<String, Object>> periodList, GamePeriod lastPeriod, Long gameId, RedisService
            redisService) {
        String chartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartName, null);
        List chartList = redisService.kryoGet(chartKey, ArrayList.class);

        if (chartList == null || chartList.size() <= 0) {
            GamePeriod lastTwoPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, lastPeriod
                    .getPeriodId());
            chartKey = RedisConstant.getCurrentChartKey(gameId, lastTwoPeriod.getPeriodId(), chartName,
                    null);
            chartList = redisService.kryoGet(chartKey, ArrayList.class);
        }
        if (chartList != null && chartList.size() > 0) {
            periodList.addAll(chartList);
        }
    }

    public void getEnumChart(List<Map<String, Object>> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                             RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
        throw new AbstractMethodError();
    }

    @Override
    public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
        return Boolean.FALSE;
    }
}