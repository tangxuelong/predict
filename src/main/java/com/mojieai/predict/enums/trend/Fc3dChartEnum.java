package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.TrendBallVo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.TrendChartUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum Fc3dChartEnum implements ChartEnumInterface {
    BASE(0, "BASE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PACKAGE_OTHER;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return null;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return BASE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
            Boolean res = Boolean.FALSE;

            //1.获取上一次基本走势图data
            GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gamePeriod.getGameId(), gamePeriod
                    .getPeriodId());
            String lastBaseChartKey = RedisConstant.getCurrentChartKey(gamePeriod.getGameId(), lastPeriod.getPeriodId(),
                    getTrendChartEnum().getChartName(), num);
            Map<String, Object> chartMap = redisService.kryoGet(lastBaseChartKey, HashMap.class);
            List<Map<String, Object>> chartList = (List<Map<String, Object>>) chartMap.get(TrendConstant
                    .KEY_TREND_PERIOD);
            if (chartList == null || chartList.size() <= 0) {
                return Boolean.FALSE;
            }
            //2.增量修改上一期走势图
            //2.1移除第一个
            chartList.remove(0);
            //2.2重构前三个和最后两行
            List<Map<String, Object>> combineOmits = TrendChartUtil.getCombineOmitList(gamePeriod, num, redisService);
            if (combineOmits == null || combineOmits.size() != num) {
                return Boolean.FALSE;
            }
            String periodId1 = combineOmits.get(0).get("periodNum").toString();
            GamePeriod gamePeriod1 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId1);
            List<Integer> omitNum1 = (List<Integer>) combineOmits.get(0).get("omitNum");
            List<TrendBallVo> first = TrendChartUtil.convertOmitArry2Vo(gamePeriod1, omitNum1);

            String periodId2 = combineOmits.get(1).get("periodNum").toString();
            GamePeriod gamePeriod2 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId2);
            List<Integer> omitNum2 = (List<Integer>) combineOmits.get(1).get("omitNum");
            List<TrendBallVo> second = TrendChartUtil.convertOmitArry2Vo(gamePeriod2, omitNum2);

            String periodId3 = combineOmits.get(2).get("periodNum").toString();
            GamePeriod gamePeriod3 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId3);
            List<Integer> omitNum3 = (List<Integer>) combineOmits.get(2).get("omitNum");
            List<TrendBallVo> third = TrendChartUtil.convertOmitArry2Vo(gamePeriod3, omitNum3);

            //后3个
            String periodId4 = combineOmits.get(combineOmits.size() - 3).get("periodNum").toString();
            GamePeriod gamePeriod4 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId4);
            List<Integer> omitNum4 = (List<Integer>) combineOmits.get(combineOmits.size() - 3).get("omitNum");
            List<TrendBallVo> last1 = TrendChartUtil.convertOmitArry2Vo(gamePeriod4, omitNum4);

            String periodId5 = combineOmits.get(combineOmits.size() - 2).get("periodNum").toString();
            GamePeriod gamePeriod5 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId5);
            List<Integer> omitNum5 = (List<Integer>) combineOmits.get(combineOmits.size() - 2).get("omitNum");
            List<TrendBallVo> last2 = TrendChartUtil.convertOmitArry2Vo(gamePeriod5, omitNum5);

            String periodId6 = combineOmits.get(combineOmits.size() - 1).get("periodNum").toString();
            GamePeriod gamePeriod6 = PeriodRedis.getPeriodByGameIdAndPeriod(gamePeriod.getGameId(), periodId6);
            List<Integer> omitNum6 = (List<Integer>) combineOmits.get(combineOmits.size() - 1).get("omitNum");
            List<TrendBallVo> last3 = TrendChartUtil.convertOmitArry2Vo(gamePeriod6, omitNum6);

            if (!gamePeriod6.getPeriodId().equals(gamePeriod.getPeriodId())) {
                log.error("combine trend error wait for the next times");
                return false;
            }

            chartList.get(0).put("omitNums", first);
            chartList.get(1).put("omitNums", second);
            chartList.get(2).put("omitNums", third);
            chartList.get(chartList.size() - 2).put("omitNums", last1);
            chartList.get(chartList.size() - 1).put("omitNums", last2);
            //2.3最后新增一个
            Map<String, Object> lastOneMap = new HashMap<>();
            int length = GameFactory.getInstance().getGameBean(gamePeriod.getGameId()).getPeriodDateFormat().length();
            lastOneMap.put("omitNums", last3);
            lastOneMap.put("periodNum", gamePeriod.getPeriodId());
            lastOneMap.put("periodName", gamePeriod.getPeriodId().substring(length) + "期");
            chartList.add(lastOneMap);

            //3.统计表完全重构
            List<Map<String, Object>> statisListAll = TrendChartUtil.getCombineStatistics(gamePeriod, num,
                    redisService);
            List<Map<String, Object>> statisList = TrendChartUtil.cvrtBaseTStatistics2StatisticVos(gamePeriod,
                    statisListAll);

            chartMap.put(TrendConstant.KEY_TREND_STATISTICS, statisList);
            chartMap.put(TrendConstant.KEY_TREND_PERIOD, chartList);

            String currentChartKey = RedisConstant.getCurrentChartKey(gamePeriod.getGameId(), gamePeriod.getPeriodId(),
                    getChartName(), num);
            redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, chartMap);
            return res;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
                redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }

    }, ONE_AND_SHAPE(1, "ONE_AND_SHAPE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PACKAGE_OTHER;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return null;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return ONE_AND_SHAPE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
            Boolean res = Boolean.FALSE;
            Map<String, Object> charMap = new HashMap<>();
            Map<String, Object> tempChartMap = TrendChartUtil.combineOnePlaceAndShapeRedis(gamePeriod.getGameId(),
                    gamePeriod.getPeriodId(), redisService, ONE, ONE_SHAPE, num);
            if (tempChartMap != null && !tempChartMap.isEmpty()) {
                String currentChartKey = RedisConstant.getCurrentChartKey(gamePeriod.getGameId(), gamePeriod
                        .getPeriodId(), getChartName(), num);
                charMap.put("winningNumber", gamePeriod.getWinningNumbers());
                charMap.putAll(tempChartMap);
                redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, charMap);
                res = Boolean.TRUE;
            }
            return res;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }

    }, TEN_AND_SHAPE(2, "TEN_AND_SHAPE") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PACKAGE_OTHER;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return null;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return TEN_AND_SHAPE;
        }

        @Override
        public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
            Boolean res = Boolean.FALSE;
            Map<String, Object> charMap = new HashMap<>();
            Map<String, Object> tempChartMap = TrendChartUtil.combineOnePlaceAndShapeRedis(gamePeriod.getGameId(),
                    gamePeriod.getPeriodId(), redisService, TEN, TEN_SHAPE, num);
            if (tempChartMap != null && !tempChartMap.isEmpty()) {
                String currentChartKey = RedisConstant.getCurrentChartKey(gamePeriod.getGameId(), gamePeriod
                        .getPeriodId(), getChartName(), num);
                charMap.put("winningNumber", gamePeriod.getWinningNumbers());
                charMap.putAll(tempChartMap);
                redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, charMap);
                res = Boolean.TRUE;
            }
            return res;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }
    }, HUNDRED_AND_SHAPE(3, "HUNDRED_AND_SHAPE") {
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
            return ChartEnum.PACKAGE_OTHER;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return null;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return HUNDRED_AND_SHAPE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
            Boolean res = Boolean.FALSE;
            Map<String, Object> charMap = new HashMap<>();
            Map<String, Object> tempChartMap = TrendChartUtil.combineOnePlaceAndShapeRedis(gamePeriod.getGameId(),
                    gamePeriod.getPeriodId(), redisService, HUNDRED, HUNDRED_SHAPE, num);
            if (tempChartMap != null && !tempChartMap.isEmpty()) {
                String currentChartKey = RedisConstant.getCurrentChartKey(gamePeriod.getGameId(), gamePeriod
                        .getPeriodId(), getChartName(), num);
                charMap.put("winningNumber", gamePeriod.getWinningNumbers());
                charMap.putAll(tempChartMap);
                redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, charMap);
                res = Boolean.TRUE;
            }
            return res;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                                 RedisService redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getBasicChart(periodList, gameId, lastPeriod, showNum, redisService, keyTrendOpenAwardTemp);
        }
    }, HUNDRED(4, "HUNDRED") {
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
            return Fc3dTrendEnum.HUNDRED;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return HUNDRED;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, TEN(5, "TEN") {
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
            return Fc3dTrendEnum.TEN;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return TEN;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, ONE(6, "ONE") {
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
            return Fc3dTrendEnum.ONE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return ONE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, INDISTINCT_LOCATION(7, "INDISTINCT_LOCATION") {
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
            return Fc3dTrendEnum.INDISTINCT_LOCATION;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return INDISTINCT_LOCATION;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, BIG_SMALL_RATIO(8, "BIG_SMALL_RATIO") {
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
            return Fc3dTrendEnum.BIG_SMALL_RATIO;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return BIG_SMALL_RATIO;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, ODD_EVEN_RATIO(9, "ODD_EVEN_RATIO") {
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
            return Fc3dTrendEnum.ODD_EVEN_RATIO;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return ODD_EVEN_RATIO;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, PRIME_COMPOSITE_RATIO(10, "PRIME_COMPOSITE_RATIO") {
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
            return Fc3dTrendEnum.PRIME_COMPOSITE_RATIO;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return PRIME_COMPOSITE_RATIO;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, HUNDRED_SHAPE(11, "HUNDRED_SHAPE") {
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
            return Boolean.FALSE;
        }

        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.HUNDRED_SHAPE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return HUNDRED_SHAPE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, TEN_SHAPE(12, "TEN_SHAPE") {
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
            return Boolean.FALSE;
        }

        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.TEN_SHAPE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return TEN_SHAPE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, ONE_SHAPE(13, "ONE_SHAPE") {
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
            return Boolean.FALSE;
        }

        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.PERIOD;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.ONE_SHAPE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return ONE_SHAPE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return TrendUtil.generateChart(gameId, periodId, getTrendChartEnum(), trendDao);
        }
    }, COLD_HOT(14, "COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.INDISTINCT_LOCATION;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return INDISTINCT_LOCATION;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
                redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }

    }, HUNDRED_COLD_HOT(15, "HUNDRED_COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.HUNDRED;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return HUNDRED;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
                redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }
    }, TEN_COLD_HOT(16, "TEN_COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.TEN;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return TEN;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
                redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }
    }, ONE_COLD_HOT(17, "ONE_COLD_HOT") {
        @Override
        public ChartEnum getChartEnum() {
            return ChartEnum.TABLE;
        }

        @Override
        public TrendEnumInterface getTrendEnum() {
            return Fc3dTrendEnum.ONE;
        }

        @Override
        public ChartEnumInterface getTrendChartEnum() {
            return ONE;
        }

        @Override
        public Map<String, Object> generateChart(Long gameId, String periodId, TrendDao trendDao) {
            return null;
        }

        @Override
        public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
                redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
            getColdHotChart(periodList, lastPeriod, gameId, redisService);
        }
    };

    private int chartType;
    private String chartName;

    Fc3dChartEnum(int chartType, String chartName) {
        this.chartType = chartType;
        this.chartName = chartName;
    }

    public static Fc3dChartEnum getByChartType(int chartType) {
        for (Fc3dChartEnum k : Fc3dChartEnum.values()) {
            if (chartType == k.getChartType()) {
                return k;
            }
        }
        return null;
    }

    public int getChartType() {
        return chartType;
    }

    public String getChartName() {
        return chartName;
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

    @Override
    public Boolean combineOtherChart(GamePeriod gamePeriod, RedisService redisService, int num) {
        return Boolean.FALSE;
    }

    public List getChart(GamePeriod lastPeriod, Long gameId, Integer showNum, RedisService
            redisService, String gameEn, Integer areaType) {
        /* 获取走势图*/
        List periodList = new ArrayList<>();
        if (lastPeriod != null && StringUtils.isNotEmpty(lastPeriod.getPeriodId())) {
            String[] keyTrendOpenAwardTemp = new String[]{TrendConstant.KEY_TREND_PERIOD, TrendConstant
                    .KEY_TREND_STATISTICS};
            getEnumChart(periodList, gameId, lastPeriod, showNum, redisService, areaType, keyTrendOpenAwardTemp);
        }
        return periodList;
    }

    public void getEnumChart(List periodList, Long gameId, GamePeriod lastPeriod, int showNum, RedisService
            redisService, Integer areaType, String[] keyTrendOpenAwardTemp) {
        throw new AbstractMethodError();
    }

    public void getBasicChart(List<TrendBallVo> periodList, Long gameId, GamePeriod lastPeriod, int showNum,
                              RedisService redisService, String[] keyTrendOpenAwardTemp) {
        String chartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartName,
                showNum);
        Map<String, Object> chartMap = redisService.kryoGet(chartKey, HashMap.class);
        if (chartMap == null) {
            log.error("走势图获取异常" + " playType:" + " chartType:" + chartType);
        }

        periodList.addAll((Collection<? extends TrendBallVo>) chartMap.get(keyTrendOpenAwardTemp[0]));
        if (chartMap.get(TrendConstant.KEY_TREND_STATISTICS) != null) {
            periodList.addAll((Collection<? extends TrendBallVo>) chartMap.get(keyTrendOpenAwardTemp[1]));
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

}
