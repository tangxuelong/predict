package com.mojieai.predict.enums.trend;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.TrendChartUtil;
import com.mojieai.predict.util.TrendUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
public enum ChartEnum {
    PERIOD {
        @Override
        public boolean processAllTrendChart(Long gameId, String periodId, ChartEnumInterface chartEnum, TrendDao
                trendDao, RedisService redisService) {
            boolean flag = Boolean.TRUE;//是否可以update执行计划
            int maxLength = 0;
            int minLength = 100;
            for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
                maxLength = (maxLength > trendPeriodEnum.getNum() ? maxLength : trendPeriodEnum.getNum());
                minLength = (minLength < trendPeriodEnum.getNum() ? minLength : trendPeriodEnum.getNum());
            }
            //判断是否存在
            String currentChartKey = RedisConstant.getCurrentChartKey(gameId, periodId, chartEnum.toString(),
                    minLength);
            Boolean ifExist = redisService.isKeyByteExist(currentChartKey);
            if (!ifExist) {
                GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, periodId);
                String lastChartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartEnum
                        .toString(), maxLength);
                Map<String, Object> chartMap = redisService.kryoGet(lastChartKey, HashMap.class);
                if (chartMap != null) {
                    processNewTrendChart(gameId, periodId, chartEnum, trendDao, redisService, chartMap);
                } else {
                    LogConstant.commonLog.error("saveTrend2Redis error." + CommonUtil.mergeUnionKey(gameId, periodId,
                            chartEnum.toString()));
                    flag = Boolean.FALSE;
                }
            }
            return flag;
        }
    }, TABLE {
        @Override
        public boolean processAllTrendChart(Long gameId, String periodId, ChartEnumInterface chartEnum, TrendDao
                trendDao, RedisService redisService) {
            boolean flag = Boolean.TRUE;//是否可以update执行计划
            String currentChartKey = RedisConstant.getCurrentChartKey(gameId, periodId, chartEnum.toString(), null);
            Boolean ifExist = redisService.isKeyByteExist(currentChartKey);
            if (!ifExist) {
                List<Map<String, Object>> coldHotMapList = TrendChartUtil.calColdHotMapList(gameId, periodId,
                        chartEnum, redisService);
                redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, coldHotMapList);
            }
            return flag;
        }
    }, PACKAGE_OTHER {
        @Override
        public boolean processAllTrendChart(Long gameId, String periodId, ChartEnumInterface chartEnum, TrendDao
                trendDao, RedisService redisService) {
            boolean flag = true;
            int maxLength = 0;
            int minLength = 100;
            for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
                maxLength = (maxLength > trendPeriodEnum.getNum() ? maxLength : trendPeriodEnum.getNum());
                minLength = (minLength < trendPeriodEnum.getNum() ? minLength : trendPeriodEnum.getNum());
            }
            //判断是否存在
            String currentChartKey = RedisConstant.getCurrentChartKey(gameId, periodId, chartEnum.toString(),
                    minLength);
            Boolean ifExist = redisService.isKeyByteExist(currentChartKey);
            if (!ifExist) {
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
                for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
                    boolean res = chartEnum.combineOtherChart(gamePeriod, redisService, trendPeriodEnum.getNum());
                    if (!res) {
                        flag = false;
                    }
                }
            }
            return flag;
        }
    };

    abstract public boolean processAllTrendChart(Long gameId, String periodId, ChartEnumInterface chartEnum, TrendDao
            trendDao, RedisService redisService);

    public void processNewTrendChart(Long gameId, String periodId, ChartEnumInterface chartEnum, TrendDao trendDao,
                                     RedisService redisService, Map<String, Object> chartMap) {
        List<Map<String, Object>> chartList = (List<Map<String, Object>>) chartMap.get(TrendConstant
                .KEY_TREND_PERIOD);
        if (chartList != null) {
            Map<String, Object> newTrend = chartEnum.generateChart(gameId, periodId, trendDao);
            chartList.remove(0);
            chartList.add(newTrend);
            if (chartEnum.IfConsecutiveNumbersShow()) {
                TrendUtil.dealEndLineConsecutiveNumbers(chartList);
            }
            for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
                int num = trendPeriodEnum.getNum();
                String chartKey = RedisConstant.getCurrentChartKey(gameId, periodId, chartEnum.toString(), num);
                List<Map<String, Object>> subChartList = new ArrayList<>();// 这里转化是因为反序列化时不能转义arrayList|SubList
                List<Map<String, Object>> subChartListTemp = chartList.subList(chartList.size() - num, chartList.size
                        ());
                if (chartEnum.IfConsecutiveNumbersShow()) {
                    TrendUtil.dealBeginConsecutiveNumbers(subChartListTemp);
                }
                subChartList.addAll(subChartListTemp);
//                period_list:[
//                {"period_num":2017073,"period_name":"073期","red_ball":"03 06 16 23 26 30","blue_ball":"14"},
//                {"period_num":2017074,"period_name":"074期","red_ball":"03 06 16 23 26 30","blue_ball":"12"}]
//                或者 period_list:[
//                {period_num:2017073,period_name:"073期",omit_num:[5,0,0,1,3,19,2,2,0,9,4,1,4,8,1,7]},
//                {period_num:2017074,period_name:"074期",omit_num:[5,0,0,1,3,19,2,2,0,9,4,1,4,8,1,7]}]
                chartMap.put(TrendConstant.KEY_TREND_PERIOD, subChartList);
                //统计
                if (chartEnum.IfStat()) {
                    List<Map<String, Object>> statisticsList = new ArrayList<>();
                    List<Map<String, Object>> statisticsListTemp = new ArrayList<>();
                    for (StatisticsEnum se : StatisticsEnum.values()) {
                        Map<String, Object> stat = new HashMap<>();
                        stat.put(TrendConstant.KEY_TREND_PERIOD_NAME, se.getStatisticsCn());
                        List<Integer> statList = se.processStatList(subChartList);
                        stat.put(TrendConstant.KEY_TREND_OMIT_NUM, statList);
                        statisticsList.add(stat);

                        //为－1的情况
                        Map<String, Object> statTemp = new HashMap<>();
                        statTemp.put(TrendConstant.KEY_TREND_PERIOD_NAME, se.getStatisticsCn());
                        List<Integer> statListTemp = new ArrayList<>();
                        for (int i = 0; i < statList.size(); i++) {
                            statListTemp.add(-1);
                        }
                        statTemp.put(TrendConstant.KEY_TREND_OMIT_NUM, statListTemp);
                        statisticsListTemp.add(statTemp);
                    }
                    chartMap.put(TrendConstant.KEY_TREND_STATISTICS, statisticsList);
                    chartMap.put(TrendConstant.KEY_TREND_STATISTICS_TEMP, statisticsListTemp);
                }

                //开奖中间状态
                if (chartEnum.IfOpenAwardTemp()) {
                    List<Map<String, Object>> openAwardTempList = new ArrayList<>();
                    for (Map<String, Object> temp : subChartListTemp) {
                        Map<String, Object> tempMap = new HashMap<>();
                        TrendUtil.deepCopyChartMap(temp, tempMap);
                        openAwardTempList.add(tempMap);
                    }

                    if (openAwardTempList != null && openAwardTempList.size() > 0) {
                        GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);

                        Map<String, Object> emptyTrend = new HashMap<>();

                        List<Integer> sizeOmit = (List<Integer>) openAwardTempList.get(0).get(TrendConstant
                                .KEY_TREND_OMIT_NUM);
                        List<Integer> omitNums = new ArrayList<>();
                        for (int i = 0; i < sizeOmit.size(); i++) {
                            omitNums.add(-1);
                        }

                        emptyTrend.put(TrendConstant.KEY_TREND_PERIOD_NAME, TrendUtil.getPeriodSubEn(gameId, gamePeriod
                                .getPeriodId()) + "期");
                        emptyTrend.put(TrendConstant.KEY_TREND_PERIOD_NUM, gamePeriod.getPeriodId());
                        emptyTrend.put(TrendConstant.KEY_TREND_OMIT_NUM, omitNums);

                        openAwardTempList.remove(0);
                        if (chartEnum.IfConsecutiveNumbersShow()) {
                            TrendUtil.dealBeginConsecutiveNumbers(openAwardTempList);
                        }
                        openAwardTempList.add(emptyTrend);
                        chartMap.put(TrendConstant.KEY_TREND_OPEN_AWARD_TEMP, openAwardTempList);
                    }
                }
                redisService.kryoSetEx(chartKey, RedisConstant.EXPIRE_TREND_COMMON, chartMap);
            }
        }
    }
}