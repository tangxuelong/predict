package com.mojieai.predict.util;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.TrendBallVo;
import com.mojieai.predict.enums.FilterEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.enums.trend.ChartEnumInterface;
import com.mojieai.predict.enums.trend.Fc3dChartEnum;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.game.Fc3dGame;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

/**
 * 走势图 构建redis工具类
 */

public class TrendChartUtil {
    private static final Logger log = LogConstant.commonLog;

    /* 结合1位和1位形态*/
    public static Map<String, Object> combineOnePlaceAndShapeRedis(Long gameId, String periodId, RedisService
            redisService, ChartEnumInterface firstChartEnum, ChartEnumInterface secondChartEnum, int num) {
        Map<String, Object> charMap = new HashMap<>();
        //1.获取个位走势和个位形态走势
        String oneTrendKey = RedisConstant.getCurrentChartKey(gameId, periodId, firstChartEnum.getChartName(), num);
        String oneShapeTrendKey = RedisConstant.getCurrentChartKey(gameId, periodId, secondChartEnum.getChartName(),
                num);
        if (redisService.isKeyByteExist(oneTrendKey) && redisService.isKeyByteExist(oneShapeTrendKey)) {
            Map<String, Object> oneChartMap = redisService.kryoGet(oneTrendKey, HashMap.class);
            Map<String, Object> oneShapeChartMap = redisService.kryoGet(oneShapeTrendKey, HashMap.class);

            List<Map<String, Object>> chartList = (List<Map<String, Object>>) oneChartMap.get(TrendConstant
                    .KEY_TREND_PERIOD);
            List<Map<String, Object>> oneShapechartList = (List<Map<String, Object>>) oneShapeChartMap.get
                    (TrendConstant.KEY_TREND_PERIOD);

            List<Map<String, Object>> statisticsList = (List<Map<String, Object>>) oneChartMap.get(TrendConstant
                    .KEY_TREND_STATISTICS);
            List<Map<String, Object>> oneShapeStatisList = (List<Map<String, Object>>) oneShapeChartMap.get
                    (TrendConstant.KEY_TREND_STATISTICS);

            List<Map<String, Object>> statisticsListTemp = (List<Map<String, Object>>) oneChartMap.get(TrendConstant
                    .KEY_TREND_STATISTICS_TEMP);
            List<Map<String, Object>> oneShapeStatisTemp = (List<Map<String, Object>>) oneShapeChartMap.get
                    (TrendConstant.KEY_TREND_STATISTICS_TEMP);

            List<Map<String, Object>> openAwardTempList = (List<Map<String, Object>>) oneChartMap.get(TrendConstant
                    .KEY_TREND_OPEN_AWARD_TEMP);
            List<Map<String, Object>> oneShapeOpenAwardTempList = (List<Map<String, Object>>) oneShapeChartMap.get
                    (TrendConstant.KEY_TREND_OPEN_AWARD_TEMP);

            List<Map<String, Object>> openAwardTempListRedis = new ArrayList<>();

            for (int i = 0; i < chartList.size(); i++) {
                Map temp1 = chartList.get(i);
                String tempPeriodId = temp1.get("periodNum").toString();
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, tempPeriodId);
                Map tempShape = oneShapechartList.get(i);
                List<Integer> omitNum = (List<Integer>) temp1.get("omitNum");
                List<Integer> omitNumShape = (List<Integer>) tempShape.get("omitNum");
                omitNum.addAll(omitNumShape);
                temp1.put("omitNum", omitNum);
                temp1.put("winningNumber", gamePeriod.getWinningNumbers());
            }

            for (int i = 0; i < statisticsList.size(); i++) {
                Map temp2 = statisticsList.get(i);
                Map tempShape = oneShapeStatisList.get(i);
                List<Integer> omitNum = (List<Integer>) temp2.get("omitNum");
                List<Integer> omitNumShape = (List<Integer>) tempShape.get("omitNum");
                omitNum.addAll(omitNumShape);
                temp2.put("omitNum", omitNum);
            }

            for (int i = 0; i < statisticsListTemp.size(); i++) {
                Map temp3 = statisticsListTemp.get(i);
                Map tempShape = oneShapeStatisTemp.get(i);
                List<Integer> omitNum = (List<Integer>) temp3.get("omitNum");
                List<Integer> omitNumShape = (List<Integer>) tempShape.get("omitNum");
                omitNum.addAll(omitNumShape);
                temp3.put("omitNum", omitNum);
            }

            for (int i = 0; i < openAwardTempList.size(); i++) {
                Map temp4 = openAwardTempList.get(i);
                Map tempShape = oneShapeOpenAwardTempList.get(i);
                String tempPeriodId = temp4.get("periodNum").toString();
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, tempPeriodId);
                List<Integer> omitNumShape = new ArrayList<>();
                List<Integer> omitNum = new ArrayList<>();
                omitNum.addAll((List<Integer>) temp4.get("omitNum"));
                omitNumShape.addAll((List<Integer>) tempShape.get("omitNum"));
                omitNum.addAll(omitNumShape);
                Map resMap = new HashMap();
                resMap.put("omitNum", omitNum);
                resMap.put("winningNumber", gamePeriod.getWinningNumbers());
                openAwardTempListRedis.add(resMap);
            }

            charMap.put(TrendConstant.KEY_TREND_PERIOD, chartList);
            charMap.put(TrendConstant.KEY_TREND_STATISTICS, statisticsList);
            charMap.put(TrendConstant.KEY_TREND_STATISTICS_TEMP, statisticsListTemp);
            charMap.put(TrendConstant.KEY_TREND_OPEN_AWARD_TEMP, openAwardTempListRedis);
        }
        return charMap;
    }

    /* 将一行遗漏转换为listVo*/
    public static List<TrendBallVo> convertOmitArry2Vo(GamePeriod gamePeriod, List<Integer> fc3dBaseTrendOmits) {
        List<TrendBallVo> result = new ArrayList<>();
        //1.第一列为开奖号
        result.add(new TrendBallVo(gamePeriod.getWinningNumbers()));
        //2.第二列为号码类型
        String type = getFc3dBallType(gamePeriod.getWinningNumbers());
        int fontColor = Fc3dGame.BASE_COLOR_NO;
        if (TrendConstant.FC3D_WIN_NUM_TYPE_3_SAME.equals(type)) {
            fontColor = Fc3dGame.BASE_COLOR_TEXT_RED;
        }
        result.add(new TrendBallVo(type, fontColor));
        //3.拼接omit对象
        Map<String, Integer> winNumShowTime = getBallShowTimes(gamePeriod.getWinningNumbers());
        for (Integer omitNum : fc3dBaseTrendOmits) {
            result.add(getTrendBallVoByOmit(omitNum, result.size() - 2, winNumShowTime));
        }
        //4.拼接
        Map<String, String> winNumAttr = getWinNumAnalysis(gamePeriod.getGameId(), gamePeriod.getWinningNumbers());

        result.add(new TrendBallVo(winNumAttr.get("route")));
        result.add(new TrendBallVo(winNumAttr.get("bigZhongSmall")));
        result.add(new TrendBallVo(winNumAttr.get("span")));
        result.add(new TrendBallVo(winNumAttr.get("acVal")));
        result.add(new TrendBallVo(winNumAttr.get("sum")));
        result.add(new TrendBallVo(winNumAttr.get("average")));
        return result;
    }

    /* 将一行统计转化为TrendBallVo*/
    public static List<TrendBallVo> convertStatisArry2Vo(GamePeriod gamePeriod, List<Integer> fc3dBaseTrendStatis) {
        List<TrendBallVo> result = new ArrayList<>();
        //1.前两个元素为空
        result.add(new TrendBallVo(""));
        result.add(new TrendBallVo(""));
        //2.
        for (Integer num : fc3dBaseTrendStatis) {
            if (num == -1) {
                result.add(new TrendBallVo("-"));
            } else {
                result.add(new TrendBallVo(num + ""));
            }
        }
        //3.补全后面空的TrendBallVo
        for (int i = result.size(); i < 60; i++) {
            result.add(new TrendBallVo(""));
        }
        return result;
    }

    public static Map<String, String> getWinNumAnalysis(long gameId, String winningNumbers) {
        Map<String, String> res = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        String[] ballArr = winningNumbers.split(CommonConstant.SPACE_SPLIT_STR);
        for (String ball : ballArr) {
            list.add(Integer.valueOf(ball));
        }
        //1.012路
        Map<String, Integer> routeRes = TrendUtil.getRed3RouteTimes(ballArr);
        String route = routeRes.get("route0") + CommonConstant.COMMON_COLON_STR + routeRes.get("route1") +
                CommonConstant.COMMON_COLON_STR + routeRes.get("route2");
        //2.大中小
        Map<String, Integer> bigSmalRes = TrendUtil.getRed3AreaTimes(gameId, ballArr);
        String bigZhongSmall = bigSmalRes.get("area3") + CommonConstant.COMMON_COLON_STR + bigSmalRes.get("area2") +
                CommonConstant.COMMON_COLON_STR + bigSmalRes.get("area1");
        //3.ac值
        /* AC值*/
        int acVal = 0;
        for (int i = GameEnum.getGameEnumById(gameId).AcMin(); i <= GameEnum.getGameEnumById(gameId).AcMax(); i++) {
            if (FilterEnum.getFilterEnum(FilterConstant.FILTER_AC).filterAction(list, String.valueOf(i),
                    GameCache.getGame(gameId).getGameEn())) {
                acVal = i;
            }
        }
        //4.span
        int span = 0;
        for (int i = 0; i <= list.size() - 2; i++) {
            int first = list.get(i);
            for (int j = i + 1; j <= list.size() - 1; j++) {
                int second = list.get(j);
                if (Math.abs(first - second) > span) {
                    span = Math.abs(first - second);
                }
            }

        }
        //5.和值
        int sum = 0;
        int average = 0;
        for (String ball : ballArr) {
            if (StringUtils.isBlank(ball)) {
                continue;
            }
            sum += Integer.valueOf(ball);
        }
        if (ballArr.length > 0) {
            BigDecimal sumBig = new BigDecimal(sum);
            BigDecimal ballCount = new BigDecimal(ballArr.length);
            average = sumBig.divide(ballCount, 0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        res.put("bigZhongSmall", bigZhongSmall);
        res.put("route", route);
        res.put("span", span + "");
        res.put("sum", sum + "");
        res.put("acVal", acVal + "");
        res.put("average", average + "");
        return res;
    }

    public static TrendBallVo getTrendBallVoByOmit(int omitNum, int index, Map<String, Integer> winNumShowTime) {
        int floatNum = 0;
        int color = Fc3dGame.BASE_COLOR_NO;
        String omit = omitNum + "";
        int form = TrendConstant.FC3D_BASE_TREND_FORM_TYPE_NONE;
        //1.针对遗漏小于0的处理
        if (omitNum <= 0) {
            form = TrendConstant.FC3D_BASE_TREND_FORM_TYPE_CIRCLE;
            if (omitNum == 0) {
                color = Fc3dGame.FC3D_OMIT_WIN_NUM_COLOR_LIST.get(index);
            } else if (omitNum == -2) {
                color = Fc3dGame.FC3D_OMIT_WIN_NUM_CONSECUTE_COLOR_LIST.get(index);
            }
            omit = Fc3dGame.FC3D_BASE_TREND_TITLE.get(index);
            //上浮
            if (index >= 30 && index <= 39) {
                if (winNumShowTime.containsKey(omit) && winNumShowTime.get(omit) > 1) {
                    floatNum = winNumShowTime.get(omit);
                } else {
                    floatNum = 0;
                }
            }
            //画方
            if (index >= 40) {
                form = TrendConstant.FC3D_BASE_TREND_FORM_TYPE_SQURE;
            }
        }

        return new TrendBallVo(omit, color, floatNum, form);
    }

    /* 获取福彩3d号码类型*/
    public static String getFc3dBallType(String balls) {
        Map<String, Integer> map = getBallShowTimes(balls);
        if (StringUtils.isBlank(balls) || map == null) {
            return null;
        }
        String result = "";
        if (map.size() == 1) {
            result = TrendConstant.FC3D_WIN_NUM_TYPE_3_SAME;
        } else if (map.size() == 2) {
            result = TrendConstant.FC3D_WIN_NUM_TYPE_2_SAME;
        } else if (map.size() == 3) {
            result = TrendConstant.FC3D_WIN_NUM_TYPE_NO_SAME;
        }
        return result;
    }

    /* balls 以空格分割*/
    public static Map<String, Integer> getBallShowTimes(String balls) {
        if (StringUtils.isBlank(balls)) {
            return null;
        }
        Map<String, Integer> result = new HashMap<>();
        String[] ballArr = balls.split(CommonConstant.SPACE_SPLIT_STR);
        for (String ball : ballArr) {
            if (StringUtils.isBlank(ball)) {
                continue;
            }
            if (result.containsKey(ball)) {
                int times = result.get(ball);
                result.put(ball, times + 1);
            } else {
                result.put(ball, 1);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> getCombineOmitList(GamePeriod gamePeriod, int num, RedisService
            redisService) {
        Fc3dChartEnum[] array = new Fc3dChartEnum[]{Fc3dChartEnum.HUNDRED, Fc3dChartEnum.TEN, Fc3dChartEnum.ONE,
                Fc3dChartEnum.INDISTINCT_LOCATION, Fc3dChartEnum.ODD_EVEN_RATIO, Fc3dChartEnum.BIG_SMALL_RATIO,
                Fc3dChartEnum.PRIME_COMPOSITE_RATIO};
        String perioListKey = TrendConstant.KEY_TREND_PERIOD;
        return combineOmitList(gamePeriod.getGameId(), gamePeriod.getPeriodId(), array, perioListKey, num,
                redisService);
    }

    /* 组合fc3d基本走势图的统计数据－－最后四行*/
    public static List<Map<String, Object>> getCombineStatistics(GamePeriod gamePeriod, int num, RedisService
            redisService) {
        Fc3dChartEnum[] array = new Fc3dChartEnum[]{Fc3dChartEnum.HUNDRED, Fc3dChartEnum.TEN, Fc3dChartEnum.ONE,
                Fc3dChartEnum.INDISTINCT_LOCATION, Fc3dChartEnum.ODD_EVEN_RATIO, Fc3dChartEnum.BIG_SMALL_RATIO,
                Fc3dChartEnum.PRIME_COMPOSITE_RATIO};
        String statisticKey = TrendConstant.KEY_TREND_STATISTICS;
        return combineOmitList(gamePeriod.getGameId(), gamePeriod.getPeriodId(), array, statisticKey, num,
                redisService);
    }

    /* 将多个走势图的omitNum组合到一个list*/
    private static List<Map<String, Object>> combineOmitList(long gameId, String periodId, Fc3dChartEnum[]
            combineCharts, String combineListKeyName, int num, RedisService redisService) {
        List<Map<String, Object>> res = new ArrayList<>();
        //1.获取所有走势
        Map<String, Object> chartMap = new HashMap<>();
        for (Fc3dChartEnum fc3dChartEnum : combineCharts) {
            String chartMapKey = RedisConstant.getCurrentChartKey(gameId, periodId, fc3dChartEnum.getChartName(), num);
            chartMap = redisService.kryoGet(chartMapKey, HashMap.class);

            if (chartMap == null || chartMap.isEmpty()) {
                return null;
            }
            List<Map<String, Object>> tempList = (List<Map<String, Object>>) chartMap.get(combineListKeyName);
            if (tempList == null || tempList.size() <= 0) {
                return null;
            }
            if (res.size() == 0) {
                res.addAll(tempList);
            } else {
                for (int i = 0; i < tempList.size(); i++) {
                    Map tempMap = res.get(i);

                    List<Integer> tempChartList = (List<Integer>) tempList.get(i).get("omitNum");
                    List<Integer> resList = (List<Integer>) tempMap.get("omitNum");
                    resList.addAll(tempChartList);
                    tempMap.put("omitNum", resList);
                }
            }
        }
        return res;
    }

    /* 获得彩种的所有号码 －－用于冷热号码List*/
    public static List<Map<String, Object>> getColdHotMapList(ChartEnumInterface chartEnum, long gameId) {
        Game game = GameCache.getGame(gameId);
        List<Map<String, Object>> coldHotMapList = new ArrayList<>();
        if (game.getGameEn().equals(GameConstant.FC3D)) {
            for (int i = 0; i < chartEnum.getTrendEnum().getBallColumns().length; i++) {
                String ball = CommonUtil.getBallStr(i);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put(TrendConstant.KEY_TREND_CODE_NUM, ball);
                coldHotMapList.add(dataMap);
            }
        } else {
            for (int i = 1; i <= chartEnum.getTrendEnum().getBallColumns().length; i++) {
                String ball = CommonUtil.getBallStr(i);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put(TrendConstant.KEY_TREND_CODE_NUM, ball);
                coldHotMapList.add(dataMap);
            }
        }
        return coldHotMapList;
    }

    /*将fc3d基本走势统计图表List<Map>转化为List<Vo>*/
    public static List<Map<String, Object>> cvrtBaseTStatistics2StatisticVos(GamePeriod gamePeriod, List<Map<String,
            Object>> statisListAll) {
        List<Map<String, Object>> statisList = new ArrayList<>();
        for (Map<String, Object> tempStatis : statisListAll) {
            List<Integer> statisticData = (List<Integer>) tempStatis.get("omitNum");
            List<TrendBallVo> trendBallVos = TrendChartUtil.convertStatisArry2Vo(gamePeriod, statisticData);
            Map<String, Object> tempStatisMap = new HashMap<>();
            tempStatisMap.put("omitNums", trendBallVos);
            tempStatisMap.put("periodName", tempStatis.get("periodName").toString());
            statisList.add(tempStatisMap);
        }
        return statisList;
    }

    /* 计算号码出现次数--冷热走势*/
    public static List<Map<String, Object>> calColdHotMapList(Long gameId, String periodId, ChartEnumInterface
            chartEnum, RedisService redisService) {
        List<Map<String, Object>> coldHotMapList = TrendChartUtil.getColdHotMapList(chartEnum, gameId);
        List<Integer> periodOmitList = new ArrayList<>();
        for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
            int num = trendPeriodEnum.getNum();
            String chartKey = RedisConstant.getCurrentChartKey(gameId, periodId, chartEnum.getTrendChartEnum
                    ().toString(), num);
            Map<String, Object> chartMap = redisService.kryoGet(chartKey, HashMap.class);
            if (chartMap == null || chartMap.isEmpty()) {
                continue;
            }
            if (periodOmitList.size() == 0) {
                List<Map<String, Object>> periodList = (List<Map<String, Object>>) chartMap.get(TrendConstant
                        .KEY_TREND_PERIOD);
                Map<String, Object> periodMap = periodList.get(periodList.size() - 1);
                periodOmitList = (List<Integer>) periodMap.get(TrendConstant.KEY_TREND_OMIT_NUM);
            }
            List<Map<String, Object>> statList = (List<Map<String, Object>>) chartMap.get(TrendConstant
                    .KEY_TREND_STATISTICS);
            Map<String, Object> appearMap = statList.get(0);
            List<Integer> omitList = (List<Integer>) appearMap.get(TrendConstant.KEY_TREND_OMIT_NUM);
            for (int i = 0; i < omitList.size(); i++) {
                coldHotMapList.get(i).put(trendPeriodEnum.toString().toLowerCase(), omitList.get(i));
                coldHotMapList.get(i).put(TrendConstant.KEY_TREND_OMIT_NUM, TrendUtil.convertMinusOmitNum
                        (periodOmitList.get(i)));
            }
        }
        return coldHotMapList;
    }
}
