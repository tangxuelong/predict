package com.mojieai.predict.util;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.bo.StrComparator;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.ColdHotAttrVo;
import com.mojieai.predict.entity.vo.ColdHotNumVo;
import com.mojieai.predict.enums.FilterEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.enums.trend.ChartEnumInterface;
import com.mojieai.predict.enums.trend.TrendEnum;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 走势图工具类
 */
public class TrendUtil {
    private static final Logger log = LogConstant.commonLog;

    public static void processMissingNumber(String periodId, String[] balls, String[] ballColumns, Map<String, Object>
            lastTrend) {
        for (String ballColumn : ballColumns) {
            int missingNumber = (int) lastTrend.get(ballColumn);
            boolean ifContain = Boolean.FALSE;
            for (String ball : balls) {
                if (ballColumn.contains(ball)) {
                    ifContain = Boolean.TRUE;
                    break;
                }
            }
            if (ifContain) {
                lastTrend.put(ballColumn, 0);
            } else {
                lastTrend.put(ballColumn, missingNumber + 1);
            }
        }
        //覆盖period_id, create_time，去掉update_time
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.remove("UPDATE_TIME");
    }

    public static void processFormBallMissingFormNumber(long gameId, String periodId, String balls, String
            winBallColumn, Map<String, Object> lastTrend) {

        Map<String, Integer> divOmit = null;
        if (lastTrend.containsKey("SMALL")) {
            divOmit = getBigSmall(gameId, Integer.valueOf(balls));
        } else {
            divOmit = getRedNumDivOmit(gameId, Integer.valueOf(balls));
        }

        divOmit.putAll(getOddEven(Integer.valueOf(balls)));
        divOmit.putAll(getRoute(Integer.valueOf(balls)));
        divOmit.putAll(getPrimeCompOmit(Integer.valueOf(balls)));

        for (Map.Entry<String, Integer> temp : divOmit.entrySet()) {
            if (temp.getValue() == 0) {
                lastTrend.put(temp.getKey(), 0);
            } else {
                lastTrend.put(temp.getKey(), temp.getValue() + Integer.valueOf(lastTrend.get(temp.getKey()).toString
                        ()));
            }
        }

        //覆盖period_id, create_time，去掉update_time
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.put(winBallColumn, "'" + balls + "'");
        lastTrend.remove("UPDATE_TIME");
    }

    /* 奇偶走势图计算*/
    public static void processJiOuMissingNumber(long gameId, String periodId, String winningNumber, Map<String, Object>
            lastTrend, String[] winningNumberRed, String[] ballColumns, String type) {
        try {
            /* 遍历开奖号码*/
            int sCount = 0;
            int dCount = 0;
            Integer columnsLength = GameEnum.getGameEnumById(gameId).getSingleRedBallLength() * 2;
            for (int i = 0; i < winningNumberRed.length; i++) {
            /* 条件判断一个号码 奇偶 奇偶比*/
                Map<String, Integer> map = new HashMap<>();
                if (type.equals(TrendConstant.TREND_TYPE_JIOU)) {
                    map = TrendUtil.getOddEven(Integer.valueOf(winningNumberRed[i]));
                }
                if (type.equals(TrendConstant.TREND_TYPE_BIG_SMALL)) {
                    map = TrendUtil.getBigSmallRed(gameId, Integer.valueOf(winningNumberRed[i]));
                }
                if (type.equals(TrendConstant.TREND_TYPE_PRIME)) {
                    map = TrendUtil.getPrimeCompOmit(Integer.valueOf(winningNumberRed[i]));
                }
                Integer oddMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[i * 2]).toString());
                lastTrend.put(ballColumns[i * 2], map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FIRST")) == 0 ? 0 :
                        oddMissingNumber + 1);

                Integer evenMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[i * 2 + 1]).toString());
                lastTrend.put(ballColumns[i * 2 + 1], map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_SECOND")) ==
                        0 ? 0 : evenMissingNumber + 1);

                if (map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FIRST")) == 0) {
                    sCount++;
                } else {
                    dCount++;
                }

            }
            /* 奇偶比 大小比 质合比*/
            String[] arr = FilterEnum.getFilterEnum(FilterConstant.FILTER_SINGLE_DOUBLE).getTwoDivStr(GameCache.getGame
                    (gameId).getGameEn());
            for (int j = 0; j < arr.length; j++) {
                if (arr[j].equals(sCount + ":" + dCount)) {
                    lastTrend.put(ballColumns[columnsLength + j], 0);
                    continue;
                }
                Integer lastMissNumber = Integer.valueOf(lastTrend.get(ballColumns[columnsLength + j]).toString());
                lastTrend.put(ballColumns[columnsLength + j], lastMissNumber + 1);
            }
            //覆盖period_id, create_time，去掉update_time
            lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
            lastTrend.put("PERIOD_ID", "'" + periodId + "'");
            lastTrend.remove("UPDATE_TIME");
        } catch (Exception e) {
            log.error("processJiOuMissingNumber error" + e);
            throw new BusinessException("processJiOuMissingNumber error", e);
        }

    }

    /*  012路走势图计算*/
    public static void processZeroOneTwoMissingNumber(long gameId, String periodId, String winningNumber, Map<String,
            Object> lastTrend, String[] winningNumberRed, String[] ballColumns, String type) {
        try {
            /* 遍历开奖号码*/
            int zCount = 0;
            int oCount = 0;
            int tCount = 0;
            Integer columnsLength = GameEnum.getGameEnumById(gameId).getSingleRedBallLength() * 3;
            for (int i = 0; i < winningNumberRed.length; i++) {
                /* 条件判断一个号码 0 1 2 路*/
                Map<String, Integer> map = new HashMap<>();
                map = getRoute(Integer.valueOf(winningNumberRed[i]));

                Integer zeroMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[i * 3]).toString());
                lastTrend.put(ballColumns[i * 3], map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FIRST")) == 0 ? 0 :
                        zeroMissingNumber + 1);

                Integer oneMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[i * 3 + 1]).toString());
                lastTrend.put(ballColumns[i * 3 + 1], map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_SECOND")) ==
                        0 ? 0 : oneMissingNumber + 1);

                Integer twoMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[i * 3 + 2]).toString());
                lastTrend.put(ballColumns[i * 3 + 2], map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_THIRD")) ==
                        0 ? 0 : twoMissingNumber + 1);

                if (map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FIRST")) == 0) {
                    zCount++;
                }
                if (map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_SECOND")) == 0) {
                    oCount++;
                }
                if (map.get(TrendConstant.TREND_TYPE_MAP.get(type + "_THIRD")) == 0) {
                    tCount++;
                }

            }
            /* 奇偶比 大小比 质合比*/
            String[] arr = FilterEnum.getFilterEnum(FilterConstant.FILTER_ZERO_ONE_TWO).getThreeDivStr(GameCache.getGame
                    (gameId).getGameEn());
            for (int j = 0; j < arr.length; j++) {
                if (arr[j].equals(zCount + ":" + oCount + ":" + tCount)) {
                    lastTrend.put(ballColumns[columnsLength + j], 0);
                    continue;
                }
                Integer lastMissNumber = Integer.valueOf(lastTrend.get(ballColumns[columnsLength + j]).toString());
                lastTrend.put(ballColumns[columnsLength + j], lastMissNumber + 1);
            }
            //覆盖period_id, create_time，去掉update_time
            lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
            lastTrend.put("PERIOD_ID", "'" + periodId + "'");
            lastTrend.remove("UPDATE_TIME");
        } catch (Exception e) {
            log.error("processZeroOneTwoMissingNumber error" + e);
            throw new BusinessException("processZeroOneTwoMissingNumber error", e);
        }
    }

    /*  AC值 跨度走势图计算*/
    public static void processACMissingNumber(long gameId, String periodId, String winningNumber, Map<String,
            Object> lastTrend, String[] winningNumberRed, String[] ballColumns, String type) {
        try {
            Integer value = 0;
            Game game = GameCache.getGame(gameId);
            List<Integer> list = new ArrayList<>();
            for (String ball : winningNumberRed) {
                list.add(Integer.valueOf(ball));
            }

            if (type.equals(TrendConstant.TREND_TYPE_AC_VALUE)) {
                /* AC值*/
                for (int i = 0; i <= GameEnum.getGameEnumByEn(game.getGameEn()).AcMax(); i++) {
                    if (FilterEnum.getFilterEnum(FilterConstant.FILTER_AC).filterAction(list, String.valueOf(i),
                            GameCache.getGame(gameId).getGameEn())) {
                        value = i;
                        lastTrend.put(ballColumns[5 + i], 0);
                        continue;
                    }
                    Integer lastOmit = Integer.valueOf(lastTrend.get(ballColumns[5 + i]).toString());
                    lastTrend.put(ballColumns[5 + i], lastOmit + 1);
                }
            }
            if (type.equals(TrendConstant.TREND_TYPE_SPAN_VALUE)) {
                /* SPAN值*/
                for (int i = GameEnum.getGameEnumByEn(game.getGameEn()).SpanMin(); i <= GameEnum.getGameEnumByEn(game
                        .getGameEn()).SpanMax(); i++) {
                    if (FilterEnum.getFilterEnum(FilterConstant.FILTER_SPAN).filterAction(list, String.valueOf(i),
                            GameCache.getGame(gameId).getGameEn())) {
                        value = i;
                        lastTrend.put(ballColumns[5 + i - GameEnum.getGameEnumByEn(game.getGameEn()).SpanMin()], 0);
                        continue;
                    }
                    Integer lastOmit = Integer.valueOf(lastTrend.get(ballColumns[5 + i - GameEnum.getGameEnumByEn
                            (game.getGameEn()).SpanMin()]).toString());
                    lastTrend.put(ballColumns[5 + i - GameEnum.getGameEnumByEn(game.getGameEn()).SpanMin()],
                            lastOmit + 1);
                }
            }
            lastTrend.put(ballColumns[0], value);
            Map<String, Integer> jiouMap = TrendUtil.getOddEven(Integer.valueOf(value));
            Integer oddMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[1]).toString());
            lastTrend.put(ballColumns[1], jiouMap.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FIRST")) == 0 ? 0 :
                    oddMissingNumber + 1);

            Integer evenMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[2]).toString());
            lastTrend.put(ballColumns[2], jiouMap.get(TrendConstant.TREND_TYPE_MAP.get(type + "_SECOND")) ==
                    0 ? 0 : evenMissingNumber + 1);

            Map<String, Integer> primeMap = TrendUtil.getPrimeCompOmit(Integer.valueOf(value));
            Integer primeMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[3]).toString());
            lastTrend.put(ballColumns[3], primeMap.get(TrendConstant.TREND_TYPE_MAP.get(type + "_THIRD")) == 0 ? 0 :
                    primeMissingNumber + 1);

            Integer comMissingNumber = Integer.valueOf(lastTrend.get(ballColumns[4]).toString());
            lastTrend.put(ballColumns[4], primeMap.get(TrendConstant.TREND_TYPE_MAP.get(type + "_FOURTH")) ==
                    0 ? 0 : comMissingNumber + 1);

            //覆盖period_id, create_time，去掉update_time
            lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
            lastTrend.put("PERIOD_ID", "'" + periodId + "'");
            lastTrend.remove("UPDATE_TIME");
        } catch (Exception e) {
            log.error("processACMissingNumber error" + e);
            throw new BusinessException("processACMissingNumber error", e);
        }
    }

    /* 和值走势遗漏计算*/
    public static void processHeZhiMissingNumber(long gameId, String periodId, String[] winningRedNum, Map<String,
            Object> lastTrend, String[] ballColumns) {
        Integer heZhi = CommonUtil.getIntegerArrSum(winningRedNum);
        Integer maxAreaHZ = getHeZhiLevelByHeZhi(gameId, heZhi);

        for (String columnName : ballColumns) {
            if (columnName.equals("RED_HZ_" + maxAreaHZ)) {
                lastTrend.put(columnName, -heZhi);
                continue;
            }
            Integer omitValue = Integer.valueOf(lastTrend.get(columnName).toString()) < 0 ? 0 : Integer.valueOf
                    (lastTrend.get(columnName).toString());
            lastTrend.put(columnName, omitValue + 1);
        }

        lastTrend.put("RED_HZ_VALUE", heZhi);
        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.remove("UPDATE_TIME");
    }

    /* 尾数走势计算*/
    public static void processWeiShuMissingNumber(String periodId, String[] winningRedNum, Map<String, Object>
            lastTrend, String[] ballColumns) {
        Map<String, Integer> winNumShowTimes = analysisNumTimes(winningRedNum);
        for (String column : ballColumns) {
            if (winNumShowTimes.containsKey(column)) {
                lastTrend.put(column, winNumShowTimes.get(column));
            } else {
                lastTrend.put(column, 0);
            }
        }
        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.remove("UPDATE_TIME");

    }

    /* 奇偶.大小质合比 走势计算*/
    public static void processOddEvenRatioMissingNumber(long gameId, String periodId, String[] winningNum, Map<String,
            Object> lastTrend, String[] ballColumns, String type) {
        Map<String, Integer> winNumRatio = null;
        winNumRatio = analysisNumRatio(gameId, winningNum, TrendConstant.TREND_COLUMN_RATIO_PREFIX, type);
        if (winningNum == null || winNumRatio.isEmpty()) {
            throw new BusinessException("计算走势图比例异常" + periodId + " type:" + type);
        }
        for (String column : ballColumns) {
            if (winNumRatio.containsKey(column)) {
                lastTrend.put(column, 0);
            } else {
                int lastOmit = lastTrend.get(column) == null ? 0 : Integer.valueOf(lastTrend.get(column).toString());
                lastTrend.put(column, lastOmit + 1);
            }
        }

        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.remove("UPDATE_TIME");
    }

    /* 号码所有形态走势*/
    public static void processShapeMissingNumber(long gameId, String periodId, String ball, Map<String, Object>
            lastTrend) {
        int lastOmit = 0;
        Map<String, Integer> shapeMap = new HashMap<>();
        shapeMap.putAll(getBigSmallRed(gameId, Integer.valueOf(ball)));
        shapeMap.putAll(getOddEven(Integer.valueOf(ball)));
        shapeMap.putAll(getPrimeCompOmit(Integer.valueOf(ball)));
        shapeMap.putAll(getRoute(Integer.valueOf(ball)));
        for (String mapKey : shapeMap.keySet()) {
            if (shapeMap.get(mapKey) == 0) {
                lastOmit = 0;
            } else {
                lastOmit = lastTrend.get(mapKey) == null ? 0 : Integer.valueOf(lastTrend.get(mapKey).toString());
            }
            lastTrend.put(mapKey, lastOmit + shapeMap.get(mapKey));
        }

        lastTrend.put("PERIOD_ID", "'" + periodId + "'");
        lastTrend.put("CREATE_TIME", "'" + DateUtil.getCurrentTimestamp() + "'");
        lastTrend.remove("UPDATE_TIME");
    }

    public static Map<String, Object> generateChart(Long gameId, String periodId, ChartEnumInterface cei, TrendDao
            trendDao) {
        Map<String, Object> newTrend = new HashMap<>();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NUM, periodId);
        int length = GameFactory.getInstance().getGameBean(gameId).getPeriodDateFormat().length();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NAME, periodId.substring(length) + "期");
        List<Integer> omitList = new ArrayList<>();
        Map<String, Object> trend = trendDao.getTrendById(gameId, periodId, cei.getTrendEnum().getTableName(gameId));
        if (trend == null) {
            throw new BusinessException("gameId:" + gameId + "periodId: " + periodId + " lastTrend not in db.");
        }
        for (String column : cei.getTrendEnum().getBallColumns()) {
            omitList.add((Integer) trend.get(column));
        }
        newTrend.put(TrendConstant.KEY_TREND_OMIT_NUM, omitList);
        if (null != cei.getTrendEnum().getExtaColumn()) {
            for (String column : cei.getTrendEnum().getExtaColumn()) {
                newTrend.put(TrendUtil.underlineToCamel(column), trend.get(column));
            }
        }
        return newTrend;
    }

    /* 获取尾数图*/
    public static Map<String, Object> generateWeiShuChart(Long gameId, String periodId, String redWinNum,
                                                          ChartEnumInterface cei, TrendDao trendDao) {
        Map<String, Object> newTrend = new HashMap<>();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NUM, periodId);
        int length = GameFactory.getInstance().getGameBean(gameId).getPeriodDateFormat().length();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NAME, periodId.substring(length) + "期");
        List<Integer> omitList = new ArrayList<>();
        List<String> showTimeList = new ArrayList<>();
        Map<String, Object> trend = trendDao.getTrendById(gameId, periodId, cei.getTrendEnum().getTableName(gameId));
        if (trend == null) {
            throw new BusinessException("gameId:" + gameId + "periodId: " + periodId + " lastTrend not in db.");
        }
        for (String column : cei.getTrendEnum().getBallColumns()) {
            Integer columnNum = getWeiShuColumnNum(column);
            if (Integer.valueOf(trend.get(column).toString()) <= 0) {
                showTimeList.add(null);
            } else if (Integer.valueOf(trend.get(column).toString()) == 1) {
                showTimeList.add(columnNum + "");
            } else {
                showTimeList.add(columnNum + CommonConstant.COMMON_SPLIT_STR + trend.get(column));
            }
        }
        newTrend.put("numShowTimes", showTimeList);
        generateWeiShuOmitNum(gameId, omitList, redWinNum);
        newTrend.put(TrendConstant.KEY_TREND_OMIT_NUM, omitList);
        if (null != cei.getTrendEnum().getExtaColumn()) {
            for (String column : cei.getTrendEnum().getExtaColumn()) {
                newTrend.put(TrendUtil.underlineToCamel(column), trend.get(column));
            }
        }

        return newTrend;
    }

    public static Map<String, Object> generateChart(Long gameId, String periodId, String[] names) {
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        Map<String, Object> newTrend = new HashMap<>();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NUM, periodId);
        int length = GameFactory.getInstance().getGameBean(gameId).getPeriodDateFormat().length();
        newTrend.put(TrendConstant.KEY_TREND_PERIOD_NAME, periodId.substring(length) + "期");
        String splitStr = CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_COLON_STR;
        for (int i = 0; i < names.length; i++) {
            newTrend.put(names[i], gamePeriod.getWinningNumbers().split(splitStr)[i]);
        }
        return newTrend;
    }

    public static Map<String, String> getWinParams(String gameEn, String gamePeriod) {
        Map<String, String> rtnMap = new HashMap<>();
        String lotteryGameEn = convertLotteryGameEn(gameEn);
        rtnMap.put("client", "client=4");
        rtnMap.put("lotteryGameEn", lotteryGameEn);
        return rtnMap;
    }

    public static String convertLotteryGameEn(String gameEn) {
        return gameEn;
    }

    /*将前台传入的参数转为系统需要参数*/
    public static Map<String, String> convertParamToTrend(String lotteryClass, String playType,
                                                          String trendChartType) {
        Map<String, String> resMap = new HashMap<>();

        ChartEnumInterface[] ceis = TrendEnum.getTrendEnumByEn(lotteryClass).getChartEnum();
        List<String> redisType = new ArrayList<>();

        for (ChartEnumInterface cei : ceis) {
            redisType.add(cei.toString());
        }
        String chartName = redisType.get(Integer.valueOf(trendChartType));

        String gameId = GameCache.getGame(lotteryClass).getGameId().toString();
        resMap.put("gameId", gameId);
        resMap.put("chartName", chartName);

        return resMap;
    }

    /*给号码排序*/
    public static String orderNum(String nums) {
        if (StringUtils.isEmpty(nums)) {
            return "";
        }
        StringBuffer resStr = new StringBuffer();
        String[] redNum = nums.split(CommonConstant.COMMON_COLON_STR);
        String[] redNums = redNum[0].split(CommonConstant.SPACE_SPLIT_STR);
        TreeSet<StrComparator> set = new TreeSet();
        for (String temp : redNums) {
            set.add(new StrComparator(temp));
        }
        int count = 1;
        for (StrComparator tempStr : set) {
            resStr.append(tempStr.getStrNum());
            if (count != set.size()) {
                resStr.append(CommonConstant.SPACE_SPLIT_STR);
            }
            count++;
        }
        if (redNum.length >= 2) {
            String blue = orderNum(redNum[1]);
            resStr.append(CommonConstant.COMMON_COLON_STR).append(blue);
        }
        return resStr.toString();
    }

    public static String GetCH(int input) {
        String sd = "";
        switch (input) {
            case 1:
                sd = "一";
                break;
            case 2:
                sd = "二";
                break;
            case 3:
                sd = "三";
                break;
            case 4:
                sd = "四";
                break;
            case 5:
                sd = "五";
                break;
            case 6:
                sd = "六";
                break;
            case 7:
                sd = "七";
                break;
            case 8:
                sd = "八";
                break;
            case 9:
                sd = "九";
                break;
            default:
                break;
        }
        return sd;
    }

    /*获取当前时间到指定天的second*/
    public static int getExprieSecond(Timestamp appointDay, int second) {
        int exprieTime = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), appointDay) + second;
        if (exprieTime <= 0) {
            exprieTime = 1;
        }
        return exprieTime;
    }

    public static String getPeriodSubEn(long gameId, String periodId) {
        int length = GameFactory.getInstance().getGameBean(gameId).getPeriodDateFormat().length();
        return periodId.substring(length);
    }

    public static String packageMoney(String money) {
        if (StringUtils.isNotBlank(money)) {
            StringBuffer stringBuffer = new StringBuffer();

            long tempMoneyThousand = Long.valueOf(money) % 10000;
            int tempMoneyTenThousand = (Integer.valueOf(money) / 10000) % 10000;
            int tempMoneyHundredMillin = Integer.valueOf(money) / 100000000;
            if (tempMoneyHundredMillin > 0) {
                stringBuffer.append(tempMoneyHundredMillin).append("亿");
            }
            if (tempMoneyTenThousand > 0) {
                stringBuffer.append(tempMoneyTenThousand).append("万");
            }
            if (tempMoneyThousand > 0) {
                if (tempMoneyTenThousand > 0) {
                    DecimalFormat df = new DecimalFormat("0000");
                    stringBuffer.append(df.format(tempMoneyThousand));
                } else {
                    stringBuffer.append(tempMoneyThousand);
                }

            }
            money = stringBuffer.toString();
        }
        return money;
    }

    /**
     * 根据时间段获取信息
     * 1区为currentPeriod 等于awardcurrentPeriod 且有开奖号码  20:00 之前
     * 2区为currentPeriod 不等于awardcurrentPeriod  20：00～21:15
     * 3区为currentPeriod 等于awardcurrentPeriod 无开奖号码 21:15～拿到开奖号码
     */
    public static Map getAreaTypeAndPeriod(long gameId, RedisService redisService) {
        Map resultMap = new HashMap();

        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);//
        GamePeriod awardCurrent = PeriodRedis.getAwardCurrentPeriod(gameId);

        if (gamePeriod == null || awardCurrent == null) {
            resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_3);
            resultMap.put("period", awardCurrent);
            return resultMap;
        }

        if (awardCurrent.getPeriodId().equals(gamePeriod.getPeriodId())) {
            String predictNumsKey = RedisConstant.getPredictNumsKey(gameId, awardCurrent.getPeriodId(),
                    RedisConstant.PREDICT_NUMS_TEN_THOUSAND, null);
            if (redisService.isKeyByteExist(predictNumsKey)) {
                resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_1);
            } else {
                resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_3);
            }
        } else {
            resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_2);
        }
        resultMap.put("period", awardCurrent);
        return resultMap;
    }

    public static Map getAreaTypeAndPeriodTrend(long gameId, RedisService redisService, String chartName, int
            showNum, GamePeriod currentPeriod) {
        Map resultMap = new HashMap();
        GamePeriod awardCurrent = PeriodRedis.getAwardCurrentPeriod(gameId);

        if (currentPeriod == null || awardCurrent == null) {
            return resultMap;
        }

        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, awardCurrent.getPeriodId());
        if (awardCurrent.getPeriodId().equals(currentPeriod.getPeriodId())) {

            Integer showNumInt = showNum; //
            if (chartName.contains("COLD_HOT")) {
                showNumInt = null;
            }
            String chartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartName, showNumInt);

            if (redisService.isKeyByteExist(chartKey)) {
                resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_1);
            } else {
                resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_3);
            }
        } else {
            resultMap.put("areaType", GameConstant.PERIOD_TIME_AREA_TYPE_2);
        }
        resultMap.put("period", awardCurrent);
        return resultMap;
    }

    public static int[] parseStrToIntArr(String str, String splitStr) {
        int[] resultArr = null;
        String[] tempStrArr = str.split(splitStr);
        if (tempStrArr != null && tempStrArr.length > 0) {
            resultArr = new int[tempStrArr.length];
            for (int i = 0; i < resultArr.length; i++) {
                resultArr[i] = Integer.valueOf(tempStrArr[i]);
            }
        }
        return resultArr;
    }

    public static String processMoney(String value) {
        try {
            BigDecimal result = new BigDecimal(value);
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                return "";
            }
            return result.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static void dealEndLineConsecutiveNumbers(List<Map<String, Object>> chartList) {
        Map currentMap = chartList.get(chartList.size() - 1);
        Map lastMap = chartList.get(chartList.size() - 2);
        Map beforeLastMap = chartList.get(chartList.size() - 3);

        left3Check(currentMap);
        checkAboveContinue(beforeLastMap, lastMap, currentMap, TrendConstant.TREND_CONTINUE_NUMBERS_DIRECTION_LEFT_UP);
        checkAboveContinue(beforeLastMap, lastMap, currentMap, TrendConstant.TREND_CONTINUE_NUMBERS_DIRECTION_RIGHT_UP);
        checkAboveContinue(beforeLastMap, lastMap, currentMap, TrendConstant.TREND_CONTINUE_NUMBERS_DIRECTION_UP);
    }

    public static void left3Check(Map currentMap) {
        List<Integer> omitNum = (List<Integer>) currentMap.get("omitNum");
        for (int i = 0; i < omitNum.size() - 2; i++) {
            if (omitNum.get(i) == 0 || omitNum.get(i) == -2) {
                if ((omitNum.get(i + 1) == 0 || omitNum.get(i + 1) == -2) && (omitNum.get(i + 2) == 0 || omitNum.get
                        (i + 2) == 0)) {
                    for (int j = i; j < omitNum.size(); j++) {
                        if (omitNum.get(j) == 0) {
                            omitNum.set(j, -2);
                            currentMap.put("omitNum", omitNum);
                        } else if (omitNum.get(j) == -2) {
                            continue;
                        } else {
                            break;
                        }
                        i = j;
                    }
                }
            }
        }
    }

    /*走势图最下面新增一行，校验上／右上／左上方法红球连号*/
    public static void checkAboveContinue(Map beforeLastMap, Map lastMap, Map currentMap, String direction) {
        int lastOmitOffset = 0;
        int beforeOmitOffset = 0;
        int maxLengthOffset = 0;
        int beginOffset = 0;
        List<Integer> beforeLastOmitNum = (List<Integer>) beforeLastMap.get("omitNum");
        List<Integer> lastOmitNum = (List<Integer>) lastMap.get("omitNum");
        List<Integer> currentOmitNum = (List<Integer>) currentMap.get("omitNum");

        if (direction.equals(TrendConstant.TREND_CONTINUE_NUMBERS_DIRECTION_RIGHT_UP)) {//右上
            lastOmitOffset = 1;
            beforeOmitOffset = 2;
            maxLengthOffset = 2;
        } else if (direction.equals(TrendConstant.TREND_CONTINUE_NUMBERS_DIRECTION_LEFT_UP)) {//左上
            lastOmitOffset = -1;
            beforeOmitOffset = -2;
            beginOffset = 2;
        }

        for (int i = beginOffset; i < currentOmitNum.size() - maxLengthOffset; i++) {
            if ((currentOmitNum.get(i) == 0 || currentOmitNum.get(i) == -2) && (lastOmitNum.get(i + lastOmitOffset)
                    == 0 || lastOmitNum.get(i + lastOmitOffset) == -2) && (beforeLastOmitNum.get(i +
                    beforeOmitOffset) == 0 || beforeLastOmitNum.get(i + beforeOmitOffset) == -2)) {
                currentOmitNum.set(i, -2);
                lastOmitNum.set(i + lastOmitOffset, -2);
                beforeLastOmitNum.set(i + beforeOmitOffset, -2);
                currentMap.put("omitNum", currentOmitNum);
                beforeLastMap.put("omitNum", beforeLastOmitNum);
                lastMap.put("omitNum", lastOmitNum);
            }
        }
    }

    public static void dealBeginConsecutiveNumbers(List<Map<String, Object>> chartList) {
        Map currentMap = chartList.get(0);
        Map lastMap = chartList.get(1);
        Map beforeLastMap = chartList.get(2);
        Map before3Map = chartList.get(3);

        List<Integer> currentOmitNums = (List<Integer>) currentMap.get("omitNum");
        List<Integer> lasttOmitNums = (List<Integer>) lastMap.get("omitNum");
        List<Integer> beforeOmitNums = (List<Integer>) beforeLastMap.get("omitNum");
        List<Integer> before3OmitNums = (List<Integer>) before3Map.get("omitNum");

        for (int i = 0; i < currentOmitNums.size(); i++) {
            if (currentOmitNums.get(i) == -2) {
                //左三
                if (i < currentOmitNums.size() - 2 && currentOmitNums.get(i + 1) == -2 && currentOmitNums.get(i + 2) ==
                        -2) {
                    continue;
                }
                //横中间
                if (i > 0 && i < currentOmitNums.size() - 1 && currentOmitNums.get(i - 1) == -2 && currentOmitNums.get
                        (i + 1) == -2) {
                    continue;
                }
                //右三
                if (i > 1 && currentOmitNums.get(i - 1) == -2 && currentOmitNums.get(i - 2) == -2) {
                    continue;
                }

                //下3
                if (lasttOmitNums.get(i) == -2 && beforeOmitNums.get(i) == -2) {
                    continue;
                }
                //左下
                if (i > 1 && lasttOmitNums.get(i - 1) == -2 && beforeOmitNums.get(i - 2) == -2) {
                    continue;
                }
                //右下
                if (i < currentOmitNums.size() - 2 && lasttOmitNums.get(i + 1) == -2 && beforeOmitNums.get(i + 2) ==
                        -2) {
                    continue;
                }
                currentOmitNums.set(i, 0);
            }
        }
        currentMap.put("omitNum", currentOmitNums);

        for (int i = 0; i < lasttOmitNums.size(); i++) {
            if (lasttOmitNums.get(i) == -2) {
                //左三
                if (i < lasttOmitNums.size() - 2 && lasttOmitNums.get(i + 1) == -2 && lasttOmitNums.get(i + 2) ==
                        -2) {
                    continue;
                }
                //横中间
                if (i > 0 && i < lasttOmitNums.size() - 1 && lasttOmitNums.get(i - 1) == -2 && lasttOmitNums.get
                        (i + 1) == -2) {
                    continue;
                }
                //右三
                if (i > 1 && lasttOmitNums.get(i - 1) == -2 && lasttOmitNums.get(i - 2) == -2) {
                    continue;
                }
                //下3
                if (before3OmitNums.get(i) == -2 && beforeOmitNums.get(i) == -2) {
                    continue;
                }
                //中间
                //上下
                if (currentOmitNums.get(i) == -2 && beforeOmitNums.get(i) == -2) {
                    continue;
                }
                //左下
                if (i > 1 && beforeOmitNums.get(i - 1) == -2 && before3OmitNums.get(i - 2) == -2) {
                    continue;
                }
                //右下
                if (i < beforeOmitNums.size() - 2 && beforeOmitNums.get(i + 1) == -2 && before3OmitNums.get(i + 2) ==
                        -2) {
                    continue;
                }
                //右上左下
                if (i > 0 && i < lasttOmitNums.size() - 1 && currentOmitNums.get(i + 1) == -2 && beforeOmitNums.get(i -
                        1) == -2) {
                    continue;
                }
                //左上右下
                if (i > 0 && i < lasttOmitNums.size() - 1 && currentOmitNums.get(i - 1) == -2 && beforeOmitNums.get(i
                        + 1) == -2) {
                    continue;
                }
                lasttOmitNums.set(i, 0);
            }
        }
        lastMap.put("omitNum", lasttOmitNums);
    }

    /*判断3区*/
    public static Map<String, Integer> getRedNumDivOmit(long gameId, int ball) {
        Map<String, Integer> result = new HashMap<>();
        int area_1 = 0;
        int area_2 = 0;
        int area_3 = 0;
        if (ball < GameEnum.getGameEnumById(gameId).getGameRedNumberDiv1Length()) {
            area_2 = 1;
            area_3 = 1;
        } else if (ball < GameEnum.getGameEnumById(gameId).getGameRedNumberDiv2Length()) {
            area_1 = 1;
            area_3 = 1;
        } else {
            area_1 = 1;
            area_2 = 1;
        }
        result.put("AREA_1", area_1);
        result.put("AREA_2", area_2);
        result.put("AREA_3", area_3);
        return result;
    }

    public static Map<String, Integer> getOddEven(int ball) {
        Map<String, Integer> result = new HashMap<>();
        if (ball % 2 != 0) {
            result.put("ODD", 0);
            result.put("EVEN", 1);
        } else {
            result.put("ODD", 1);
            result.put("EVEN", 0);
        }
        return result;
    }

    /*0.1.2路*/
    public static Map<String, Integer> getRoute(Integer ball) {
        Map<String, Integer> result = new HashMap<>();
        int route_0 = 0;
        int route_1 = 0;
        int route_2 = 0;
        if ((ball % 3) == 0) {
            route_1 = 1;
            route_2 = 1;
        } else if ((ball % 3) == 1) {
            route_0 = 1;
            route_2 = 1;
        } else if ((ball % 3) == 2) {
            route_0 = 1;
            route_1 = 1;
        }
        result.put("ROUTE_0", route_0);
        result.put("ROUTE_1", route_1);
        result.put("ROUTE_2", route_2);
        return result;
    }

    public static Map<String, Integer> getPrimeCompOmit(Integer ball) {
        Map<String, Integer> result = new HashMap<>();
        Integer[] primeArr = {1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
        List<Integer> primeList = new ArrayList<>();
        Collections.addAll(primeList, primeArr);
        if (primeList.contains(ball)) {
            result.put("PRIME", 0);
            result.put("COMPOSITE", 1);
        } else {
            result.put("PRIME", 1);
            result.put("COMPOSITE", 0);
        }
        return result;
    }

    public static Map<String, Integer> getBigSmall(long gameId, Integer ball) {
        Map<String, Integer> result = new HashMap<>();

        if (ball < GameEnum.getGameEnumById(gameId).getGameBlueNumberMiddleLength()) {
            result.put("SMALL", 0);
            result.put("BIG", 1);
        } else {
            result.put("SMALL", 1);
            result.put("BIG", 0);
        }
        return result;
    }

    public static Map<String, Integer> getBigSmallRed(long gameId, Integer ball) {
        Map<String, Integer> result = new HashMap<>();

        if (ball < GameEnum.getGameEnumById(gameId).getGameRedNumberMiddleLength()) {
            result.put("SMALL", 0);
            result.put("BIG", 1);
        } else {
            result.put("SMALL", 1);
            result.put("BIG", 0);
        }
        return result;
    }

    /* 3区次数*/
    public static Map<String, Integer> getRed3AreaTimes(long gameId, String[] winBalls) {
        Map<String, Integer> res = new HashMap<>();
        int area1 = 0;
        int area2 = 0;
        int area3 = 0;
        for (String ball : winBalls) {
            if (StringUtils.isBlank(ball)) {
                continue;
            }
            if (Integer.valueOf(ball) <= GameEnum.getGameEnumById(gameId).getGameRed1AreaMaxNum()) {
                area1++;
            } else if (Integer.valueOf(ball) <= GameEnum.getGameEnumById(gameId).getGameRed2AreaMaxNum()) {
                area2++;
            } else {
                area3++;
            }
        }
        res.put("area1", area1);
        res.put("area2", area2);
        res.put("area3", area3);
        return res;
    }

    public static Map<String, Integer> getRed3RouteTimes(String[] balls) {
        Map<String, Integer> res = new HashMap<>();
        int route0 = 0;
        int route1 = 0;
        int route2 = 0;
        for (String ballStr : balls) {
            if (StringUtils.isBlank(ballStr)) {
                continue;
            }
            int ball = Integer.valueOf(ballStr);
            if ((ball % 3) == 0) {
                route0++;
            } else if ((ball % 3) == 1) {
                route1++;
            } else if ((ball % 3) == 2) {
                route2++;
            }
        }

        res.put("route0", route0);
        res.put("route1", route1);
        res.put("route2", route2);
        return res;
    }

    /*转驼峰*/
    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        param = param.toLowerCase();
        StringBuilder sb = new StringBuilder(param);
        Matcher mc = Pattern.compile("_").matcher(param);
        int i = 0;
        while (mc.find()) {
            int position = mc.end() - (i++);
            sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
        }
        return sb.toString();
    }

    public static List<ColdHotNumVo> rebuildColdHotList(GamePeriod lastPeriod, RedisService redisService) {
        List<ColdHotNumVo> result = new ArrayList<>();
        //1.获取冷热走势
        String chartName = TrendEnum.getTrendEnumById(lastPeriod.getGameId()).getChartEnumName(Integer.valueOf(3));
        String blueChartName = TrendEnum.getTrendEnumById(lastPeriod.getGameId()).getChartEnumName(Integer.valueOf(4));
        String redBallKey = RedisConstant.getCurrentChartKey(lastPeriod.getGameId(), lastPeriod.getPeriodId(),
                chartName, null);
        String blueBallKey = RedisConstant.getCurrentChartKey(lastPeriod.getGameId(), lastPeriod.getPeriodId(),
                blueChartName, null);

        List<Map<String, Object>> redList = redisService.kryoGet(redBallKey, ArrayList.class);
        List<Map<String, Object>> blueList = redisService.kryoGet(blueBallKey, ArrayList.class);
        if (redList != null && blueList != null && redList.size() > 0 && blueList.size() > 0) {
            ColdHotNumVo coldHotNum30 = new ColdHotNumVo("30期");
            ColdHotNumVo coldHotNum50 = new ColdHotNumVo("50期");
            ColdHotNumVo coldHotNum100 = new ColdHotNumVo("100期");
            calColdHotNumVoList(lastPeriod, result, redList, "RED", coldHotNum30, coldHotNum50, coldHotNum100);
            calColdHotNumVoList(lastPeriod, result, blueList, "BLUE", coldHotNum30, coldHotNum50, coldHotNum100);

            result.add(coldHotNum30);
            result.add(coldHotNum50);
            result.add(coldHotNum100);
        }
        return result;
    }

    private static void calColdHotNumVoList(GamePeriod lastPeriod, List<ColdHotNumVo> result, List<Map<String, Object
            >> numShowTimeList, String ballType, ColdHotNumVo coldHotNum30, ColdHotNumVo coldHotNum50, ColdHotNumVo
                                                    coldHotNum100) {
        //1.组合拼装要返回的VOlist
        for (Map<String, Object> ballsInfo : numShowTimeList) {
            for (TrendPeriodEnum trendPeriodEnum : TrendPeriodEnum.values()) {
                int num = trendPeriodEnum.getNum();
                ColdHotAttrVo coldHotAttrVo = new ColdHotAttrVo();
                if (ballsInfo.containsKey("period" + num)) {
                    int tempShowTime = Integer.valueOf(ballsInfo.get("period" + num).toString());

                    coldHotAttrVo.setBallNum(ballsInfo.get("codeNum").toString());
                    coldHotAttrVo.setColdHeatName(calcuColdHeatName(lastPeriod.getGameId(), num, ballType, tempShowTime,
                            "name"));
                    coldHotAttrVo.setColor(calcuColdHeatName(lastPeriod.getGameId(), num, ballType, tempShowTime,
                            "color"));
                    coldHotAttrVo.setColdHeatVal(tempShowTime);
                    coldHotAttrVo.setOmitNum(Integer.valueOf(ballsInfo.get("omitNum").toString()));

                    if (coldHotNum30.getPeriodNum().contains("" + num)) {
                        saveMaxShowTime(ballType, coldHotNum30, coldHotAttrVo, tempShowTime);
                    } else if (coldHotNum50.getPeriodNum().contains("" + num)) {
                        saveMaxShowTime(ballType, coldHotNum50, coldHotAttrVo, tempShowTime);
                    } else if (coldHotNum100.getPeriodNum().contains("" + num)) {
                        saveMaxShowTime(ballType, coldHotNum100, coldHotAttrVo, tempShowTime);
                    }
                }
            }
        }
        //2.计算百分比（上面没有办法计算百分比，单拿出来计算）
        List<ColdHotAttrVo> coldHotAttrVos30 = coldHotNum30.getColdHotAttrVoList(ballType);
        List<ColdHotAttrVo> coldHotAttrVos50 = coldHotNum50.getColdHotAttrVoList(ballType);
        List<ColdHotAttrVo> coldHotAttrVos100 = coldHotNum100.getColdHotAttrVoList(ballType);
        for (int i = 0; i < coldHotAttrVos30.size(); i++) {
            calcuPercent(coldHotNum30.getMaxShowTime(ballType), coldHotAttrVos30.get(i));
            calcuPercent(coldHotNum50.getMaxShowTime(ballType), coldHotAttrVos50.get(i));
            calcuPercent(coldHotNum100.getMaxShowTime(ballType), coldHotAttrVos100.get(i));
        }
    }

    /*计算冷热选号百分比*/
    private static void calcuPercent(Integer maxShowTime, ColdHotAttrVo coldHotAttrVo) {
        int showTimes = coldHotAttrVo.getColdHeatVal();
        Double percent = new Double(Math.round(showTimes * 1000 / maxShowTime) / 1000.0);
        coldHotAttrVo.setColdHeatPercent(percent);
    }

    private static void saveMaxShowTime(String ballType, ColdHotNumVo coldHotNum, ColdHotAttrVo coldHotAttrVo, int
            tempShowTime) {
        int maxTimes = coldHotNum.getMaxShowTime(ballType);
        maxTimes = maxTimes > tempShowTime ? maxTimes : tempShowTime;
        coldHotNum.setMaxShowTime(maxTimes, ballType);
        coldHotNum.addColdHostAttrVo(coldHotAttrVo, ballType);
    }

    /*将号码出现次数转为文字  热号  温号  冷号*/
    public static String calcuColdHeatName(long gameId, Integer periodNum, String ballType, Integer showTimes, String
            type) {
        if (showTimes < GameEnum.getGameEnumById(gameId).getGameColdNumberLength(periodNum, ballType)) {
            if (type.equals("color")) {
                return TrendConstant.TREND_COLOR_GREEN_VAL;
            }
            return TrendConstant.TREND_COLD_HEAT_NUMBERS_COLD;
        } else if (showTimes < GameEnum.getGameEnumById(gameId).getGameWarmNumberLength(periodNum, ballType)) {
            if (type.equals("color")) {
                return TrendConstant.TREND_COLOR_ORANGE_VAL;
            }
            return TrendConstant.TREND_COLD_HEAT_NUMBERS_WARM;
        } else {
            if (type.equals("color")) {
                return TrendConstant.TREND_COLOR_RED_VAL;
            }
            return TrendConstant.TREND_COLD_HEAT_NUMBERS_HEAT;
        }
    }

    /*将中奖号码的遗漏值转换*/
    public static Integer convertMinusOmitNum(Integer omitNum) {
        if (omitNum < 0) {
            return 0;
        }
        return omitNum;
    }

    public static void deepCopyChartMap(Map<String, Object> originMap, Map<String, Object> destinateMap) {
        List<Integer> omitTempNum = new ArrayList<>();
        List<Integer> omitNum = (List<Integer>) originMap.get("omitNum");

        omitTempNum.addAll(omitNum);
        destinateMap.put(TrendConstant.KEY_TREND_PERIOD_NUM, originMap.get("periodNum").toString());
        destinateMap.put(TrendConstant.KEY_TREND_PERIOD_NAME, originMap.get("periodName").toString());
        destinateMap.put(TrendConstant.KEY_TREND_OMIT_NUM, omitTempNum);
    }

    public static Integer getHeZhiLevelByHeZhi(Long gameId, Integer heZhi) {
        if (GameEnum.getGameEnumById(gameId).getGameEn().equals(GameConstant.SSQ)) {
            return getSsqHeZhiLevelByHeZhi(heZhi);
        } else {
            return getDltHeZhiLevelByHeZhi(heZhi);
        }
    }

    public static Integer getSsqHeZhiLevelByHeZhi(Integer heZhi) {
        Integer result = null;
        Integer[] maxLevel = new Integer[]{183, 139, 129, 119, 109, 99, 89, 79, 69, 59, 49};
        for (int i = 0; i < maxLevel.length - 1; i++) {
            if (heZhi <= maxLevel[i] && heZhi > maxLevel[i + 1]) {
                result = maxLevel[i];
            }
        }
        if (result == null && maxLevel[maxLevel.length - 1] >= heZhi) {
            result = maxLevel[maxLevel.length - 1];
        }
        return result;
    }

    public static Integer getDltHeZhiLevelByHeZhi(Integer heZhi) {
        Integer result = null;
        Integer[] maxLevel = new Integer[]{165, 139, 129, 119, 109, 99, 89, 79, 69, 59, 49};
        for (int i = 0; i < maxLevel.length - 1; i++) {
            if (heZhi <= maxLevel[i] && heZhi > maxLevel[i + 1]) {
                result = maxLevel[i];
            }
        }
        if (result == null && maxLevel[maxLevel.length - 1] >= heZhi) {
            result = maxLevel[maxLevel.length - 1];
        }
        return result;
    }

    public static Map<String, Integer> analysisNumTimes(String[] redWinNums) {
        Map<String, Integer> result = new HashMap<>();

        for (String redNum : redWinNums) {
            if (CommonUtil.isNumeric(redNum)) {
                Integer redNumInt = Integer.valueOf(redNum) % 10;
                if (!result.containsKey("WS_" + redNumInt)) {
                    result.put("WS_" + redNumInt, 1);
                } else {
                    result.put("WS_" + redNumInt, result.get("WS_" + redNumInt) + 1);
                }
            }
        }
        return result;
    }

    public static Integer getWeiShuColumnNum(String column) {
        Integer result = null;
        if (StringUtils.isBlank(column)) {
            return result;
        }
        String numStr = column.replace("WS_", "");
        result = Integer.valueOf(numStr);
        return result;
    }

    public static void generateWeiShuOmitNum(long gameId, List<Integer> omitNum, String redWinNum) {
        //ssq
        String[] keyArr = new String[]{"10", "20", "30", "01", "11", "21", "31", "02", "12", "22", "32", "03", "13",
                "23", "33", "04", "14", "24", "05", "15", "25", "06", "16", "26", "07", "17", "27", "08", "18", "28",
                "09", "19", "29"};
        //dlt
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            keyArr = new String[]{"10", "20", "30", "01", "11", "21", "31", "02", "12", "22", "32", "03", "13", "23",
                    "33", "04", "14", "24", "34", "05", "15", "25", "35", "06", "16", "26", "07", "17", "27", "08",
                    "18", "28", "09", "19", "29"};
        }

        for (String key : keyArr) {
            if (redWinNum.contains(key)) {
                omitNum.add(Integer.valueOf(key));
            } else {
                omitNum.add(TrendConstant.TREND_CHART_SPECIAL_NOT_SHOW);
            }
        }
    }

    public static Map<String, Integer> analysisNumRatio(long gameId, String[] winningNum, String prefix, String type) {
        Map<String, Integer> res = new HashMap<>();
        if (type.equals(TrendConstant.TREND_TYPE_JIOU)) {
            prefix = prefix + getOddEvenRatio(winningNum);
        } else if (type.equals(TrendConstant.TREND_TYPE_BIG_SMALL)) {
            prefix = prefix + getBigSmallRatio(winningNum, GameEnum.getGameEnumById(gameId)
                    .getGameRedNumberMiddleLength());
        } else if (type.equals(TrendConstant.TREND_TYPE_PRIME)) {
            prefix = prefix + getPrimeCompRatio(winningNum);
        }
        res.put(prefix, 1);
        return res;
    }

    /* 获取数组中奇偶ratio*/
    public static String getOddEvenRatio(String[] balls) {
        int odd = 0;
        int even = 0;
        for (String ball : balls) {
            if (Integer.valueOf(ball) % 2 == 0) {
                even++;
            } else {
                odd++;
            }
        }
        return odd + CommonConstant.COMMON_SPLIT_STR + even;
    }

    /* 获取数组中大小ratio  mid为大的最小*/
    public static String getBigSmallRatio(String[] balls, int midNum) {
        int big = 0;
        int small = 0;
        for (String ball : balls) {
            if (Integer.valueOf(ball) < midNum) {
                small++;
            } else {
                big++;
            }
        }
        return big + CommonConstant.COMMON_SPLIT_STR + small;
    }

    public static String getPrimeCompRatio(String[] balls) {
        int prime = 0;
        int comp = 0;
        for (String ball : balls) {
            if (AbstractGame.COMMON_GAME_PRIME_LIST.contains(Integer.valueOf(ball))) {
                prime++;
            } else {
                comp++;
            }
        }
        return prime + CommonConstant.COMMON_SPLIT_STR + comp;
    }

}
