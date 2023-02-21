package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.vo.TrendBallVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.trend.ChartEnumInterface;
import com.mojieai.predict.enums.trend.StatisticsEnum;
import com.mojieai.predict.enums.trend.TrendEnum;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.TrendChartService;
import com.mojieai.predict.util.TrendChartUtil;
import com.mojieai.predict.util.TrendUtil;
import com.mojieai.predict.util.qiniu.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrendChartServiceImpl implements TrendChartService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private PeriodScheduleDao periodScheduleDao;
    @Autowired
    private TrendDao trendDao;
    @Autowired
    private GamePeriodDao gamePeriodDao;

    @Override
    public void saveTrend2Redis(Task task, PeriodSchedule dirtyPeriodSchedule) {
        if (dirtyPeriodSchedule != null && dirtyPeriodSchedule.getIfTrendCache() == CommonStatusEnum.YES.getStatus
                ()) {
            return;
        }
        PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(task.getGameId(), task.getPeriodId());
        if (periodSchedule.getIfTrendCache() == CommonStatusEnum.NO.getStatus()) {
            TrendEnum trendEnum = TrendEnum.getTrendEnumById(task.getGameId());
            boolean flag = Boolean.TRUE;//是否可以update执行计划
            for (ChartEnumInterface cei : trendEnum.getChartEnum()) {
                boolean tempFlag = cei.getChartEnum().processAllTrendChart(task.getGameId(), task.getPeriodId(), cei,
                        trendDao, redisService);
                if (!tempFlag) {
                    flag = Boolean.FALSE;
                }
            }
            if (flag) {
                periodScheduleDao.updatePeriodSchedule(periodSchedule.getGameId(), periodSchedule.getPeriodId(),
                        "IF_TREND_CACHE", "CACHE_TIME");
            }
        }
    }

    /* 客户端参数获取走势图数据*/
    @Override
    public Map<String, Object> getTrendListData(String lotteryClass, String playType, String chartType, int showNum) {
        /* 走势图结果容器*/
        Map<String, Object> mapResult = new HashMap<>();

        /* 走势图名字 和 彩种 当前期次*/
        String chartName = TrendEnum.getTrendEnumByEn(lotteryClass).getChartEnumName(Integer.valueOf(chartType));
        long gameId = GameCache.getGame(lotteryClass).getGameId();

        /* 当前期次*/
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        /* 未开奖，开奖中，已经开奖，返回不同的结果*/
        Map<String, Object> areaTypeAndPeriod = TrendUtil.getAreaTypeAndPeriodTrend(gameId, redisService,
                chartName, showNum, currentPeriod);
        if (areaTypeAndPeriod == null) {
            log.error("获取走势图分区lotteryClass:" + lotteryClass + " playType:" + playType + " chartType:" + chartType);
            return null;
        }
        int areaType = (int) areaTypeAndPeriod.get("areaType");
        GamePeriod awardCurrentPeriod = (GamePeriod) areaTypeAndPeriod.get("period");
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, awardCurrentPeriod.getPeriodId());

        String openAwardInfo = "";

        if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            openAwardInfo = "第" + lastPeriod.getPeriodId() + "期开奖中";
            lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, lastPeriod.getPeriodId());

        } else if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_2) {
            openAwardInfo = "第" + awardCurrentPeriod.getPeriodId() + "期官方投注已截止";
        }
        /*
        * GamePeriod lastPeriod, Long gameId, Integer showNum, RedisService
            redisService, String gameEn, Integer areaType*/
        List periodList = TrendEnum.getTrendEnumByEn(lotteryClass).getChart(Integer.valueOf
                (chartType), lastPeriod, gameId, showNum, redisService, lotteryClass, areaType);
        /* 如果是红球，添加蓝球走势*/
        if (chartType.equals(TrendConstant.CHART_TYPE_RED)) {
            List<Map<String, Object>> bluePeriodList = TrendEnum.getTrendEnumByEn(lotteryClass).getChart(Integer.valueOf
                    (TrendConstant.CHART_TYPE_BLUE), lastPeriod, gameId, showNum, redisService, lotteryClass, areaType);
            mapResult.put("bluePeriodList", bluePeriodList);
        }
        mapResult.put("currentPeriodId", awardCurrentPeriod.getPeriodId());
        mapResult.put("openAwardInfo", openAwardInfo);
        mapResult.put("isShowFilter", ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IS_SHOW_FILTER, 1));
        mapResult.put("isShowAnalyze", ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IS_SHOW_ANALYZE, 1));
        mapResult.put("periodList", periodList);
        return mapResult;
    }

    @Override
    public Map<String, Object> getBlueMatrixTrendChart(long gameId) {
        Map<String, Object> res = new HashMap<>();
        GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        String key = RedisConstant.getBlueMatrixTrendKey(gamePeriod.getPeriodId());
        List<Map<String, Object>> dataList = redisService.kryoGet(key, ArrayList.class);
        if (dataList == null) {
            dataList = rebuildBlueMatrixTrendChart(gameId);
        }
        res.put("dataList", dataList);
        return res;
    }

    private List<Map<String, Object>> rebuildBlueMatrixTrendChart(long gameId) {
        List<Map<String, Object>> res = new ArrayList<>();
        List<GamePeriod> gamePeriods = PeriodRedis.getLastAllOpenPeriodsByGameId(gameId);
        int count = 0;
        for (GamePeriod period : gamePeriods) {
            if (count == 99) {
                break;
            }
            if (period == null || StringUtils.isBlank(period.getWinningNumbers())) {
                continue;
            }
            String winNum = period.getWinningNumbers();
            String[] blueBallArr = winNum.split(":");
            if (blueBallArr.length <= 1) {
                log.error("period error " + period.getPeriodId());
                continue;
            }

            Map<String, Object> temp = new HashMap<>();
            temp.put("periodName", period.getPeriodId() + "期");
            temp.put("num", blueBallArr[1]);
            if (!temp.isEmpty()) {
                count++;
                res.add(temp);
            }
        }

        if (res.size() == 0) {
            return null;
        }
        GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        Collections.reverse(res);
        String key = RedisConstant.getBlueMatrixTrendKey(gamePeriod.getPeriodId());
        redisService.kryoSetEx(key, 259200, res);
        return res;
    }

    /*定时获取最新一期放倒schedule表中*/
    public void timeSetSchedule() {
        //1.双色球
        Game ssq = GameCache.getGame(GameConstant.SSQ);
        Game dlt = GameCache.getGame(GameConstant.DLT);

    }

    /**/
    @Override
    public void manualSaveTrend2Redis(long gameId, String periodId) {
        PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(gameId, periodId);
        if (periodSchedule.getIfTrendCache() == CommonStatusEnum.NO.getStatus()) {
            TrendEnum trendEnum = TrendEnum.getTrendEnumById(gameId);
            boolean flag = Boolean.TRUE;//是否可以update执行计划
            for (ChartEnumInterface cei : trendEnum.getChartEnum()) {
                flag = cei.getChartEnum().processAllTrendChart(gameId, periodId, cei,
                        trendDao, redisService);
            }
            if (flag) {
                periodScheduleDao.updatePeriodSchedule(periodSchedule.getGameId(), periodSchedule.getPeriodId(),
                        "IF_TREND_CACHE", "CACHE_TIME");
            }
        }
    }

    /*构建100期数据*/
    @Override
    public void generate100ChartData(long gameId, String currentPeriodId, int beginPeriod, int endPeriodId, int num) {

        TrendEnum trendEnum = TrendEnum.getTrendEnumById(gameId);
        for (ChartEnumInterface cei : trendEnum.getChartEnum()) {
            Map<String, Object> chartMap = new HashMap();
            List<Map<String, Object>> subChartList = new ArrayList<>();
            if (cei.toString().contains("COLD_HOT")) {
                TrendPeriodEnum tpe = TrendPeriodEnum.getTrendPeriodEnumByNum(num);
                if (tpe == null) {
                    throw new BusinessException("TrendPeriodEnum is null num:" + num);
                }
                long begin = System.currentTimeMillis();
                tableChart(cei, gameId, currentPeriodId);
                long end = System.currentTimeMillis();
                log.info(cei.getChartName() + num + "Profile tableChart use time " + (end - begin) / 1000 + "s");
                continue;
            }
            if (cei.getChartEnum().toString().equals("PACKAGE_OTHER")) {
                continue;
            }
            int tempPeriod = beginPeriod;
            long begin = System.currentTimeMillis();
            while (tempPeriod <= endPeriodId) {
                try {
                    Map<String, Object> newChart = cei.generateChart(gameId, tempPeriod + "", trendDao);
                    if (newChart != null) {
                        subChartList.add(newChart);
                    }
                } catch (Exception e) {
                    continue;
                } finally {
                    tempPeriod = Integer.valueOf(PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, tempPeriod + "")
                            .getPeriodId());
                }
            }

            chartMap.put(TrendConstant.KEY_TREND_PERIOD, subChartList);
            String chartKey = RedisConstant.getCurrentChartKey(gameId, currentPeriodId, cei.toString(), num);
            //统计
            if (cei.IfStat()) {
                List<Map<String, Object>> statisticsList = new ArrayList<>();
                List<Map<String, Object>> statisticsListTemp = new ArrayList<>();
                for (StatisticsEnum se : StatisticsEnum.values()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put(TrendConstant.KEY_TREND_PERIOD_NAME, se.getStatisticsCn());
                    List<Integer> statList = se.processStatList(subChartList);
                    stat.put(TrendConstant.KEY_TREND_OMIT_NUM, statList);
                    statisticsList.add(stat);

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
            if (cei.IfOpenAwardTemp()) {
                List<Map<String, Object>> openAwardTempList = new ArrayList<>();
                subChartList.forEach(n -> openAwardTempList.add(n));
                if (openAwardTempList != null && openAwardTempList.size() > 0) {
                    GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, currentPeriodId);

                    Map<String, Object> emptyTrend = new HashMap<>();

                    List<Integer> sizeOmit = (List<Integer>) openAwardTempList.get(0).get(TrendConstant
                            .KEY_TREND_OMIT_NUM);
                    List<Integer> omitNums = new ArrayList<>();
                    for (int i = 0; i < sizeOmit.size(); i++) {
                        omitNums.add(-1);
                    }

                    emptyTrend.put(TrendConstant.KEY_TREND_PERIOD_NAME, TrendUtil.getPeriodSubEn(gameId, gamePeriod
                            .getPeriodId()));
                    emptyTrend.put(TrendConstant.KEY_TREND_PERIOD_NUM, gamePeriod.getPeriodId());
                    emptyTrend.put(TrendConstant.KEY_TREND_OMIT_NUM, omitNums);

                    openAwardTempList.remove(0);
                    openAwardTempList.add(emptyTrend);
                    chartMap.put(TrendConstant.KEY_TREND_OPEN_AWARD_TEMP, openAwardTempList);
                }
            }
            if (redisService.isKeyByteExist(chartKey)) {
                redisService.del(chartKey);
            }
            redisService.kryoSet(chartKey, chartMap);
            long end = System.currentTimeMillis();
            log.info(cei.getChartName() + num + "Profile baseChart use time " + (end - begin) / 1000 + "s");
        }

    }

    /**
     * 计算连号
     */
    @Override
    public Map<String, Object> generate100ChartContinueToRedis(long gameId, String beginPeriod, String endPeriodId,
                                                               int num, String currentPeriodId) {
        Map<String, Object> result = new HashMap<>();
        for (ChartEnumInterface cif : TrendEnum.getTrendEnumById(gameId).getChartEnum()) {
            if (cif.IfConsecutiveNumbersShow()) {
                generateContinueCode(gameId, num, currentPeriodId, result, cif.getChartName());
            }
        }
        return result;
    }

    private void generateContinueCode(long gameId, int num, String currentPeriodId, Map<String, Object> result,
                                      String chartName) {
        String chartKey = RedisConstant.getCurrentChartKey(gameId, currentPeriodId, chartName, num);
        Map<String, Object> chartMap = redisService.kryoGet(chartKey, HashMap.class);
        if (chartMap != null) {
            ArrayList<Map<String, Object>> periodList = (ArrayList<Map<String, Object>>) chartMap.get(TrendConstant
                    .KEY_TREND_PERIOD);
            ArrayList<Map<String, Object>> periodListTemp = (ArrayList<Map<String, Object>>) chartMap.get
                    (TrendConstant.KEY_TREND_OPEN_AWARD_TEMP);

            //100期走势图数据
            selectContinueCode(periodList, 0);
            selectContinueCode(periodListTemp, 1);

            result.put(TrendConstant.KEY_TREND_PERIOD, periodList.size());
            result.put(TrendConstant.KEY_TREND_OPEN_AWARD_TEMP, periodListTemp.size());
            redisService.kryoSet(chartKey, chartMap);
        }
    }

    @Override
    public void generate3LevelChartData(Long gameId, String periodId, Integer num) {

        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        TrendEnum trendEnum = TrendEnum.getTrendEnumById(gameId);
        for (ChartEnumInterface cei : trendEnum.getChartEnum()) {
            if (!cei.getChartEnum().toString().equals("PACKAGE_OTHER")) {
                continue;
            }
            if (!cei.getChartName().equals("BASE")) {
                cei.combineOtherChart(period, redisService, num);
                continue;
            }
            Map<String, Object> chartMap = new HashMap<>();
            List<Map<String, Object>> chartList = new ArrayList<>();
            String currentChartKey = RedisConstant.getCurrentChartKey(gameId, period.getPeriodId(), cei
                    .getTrendChartEnum().getChartName(), num);

            //转换listvo
            List<Map<String, Object>> combineOmits = TrendChartUtil.getCombineOmitList(period, num, redisService);
            for (Map<String, Object> tempMap : combineOmits) {
                String tempPeriodId = tempMap.get("periodNum").toString();
                String periodName = tempMap.get("periodName").toString();
                GamePeriod tempPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, tempPeriodId);
                List<Integer> combineOmit = (List<Integer>) tempMap.get("omitNum");
                List<TrendBallVo> omitNums = TrendChartUtil.convertOmitArry2Vo(tempPeriod, combineOmit);

                tempMap.remove("omitNum");
                tempMap.put("omitNums", omitNums);
                chartList.add(tempMap);
            }

            //设置统计表
            List<Map<String, Object>> statisListAll = TrendChartUtil.getCombineStatistics(period, num, redisService);
            List<Map<String, Object>> statisList = TrendChartUtil.cvrtBaseTStatistics2StatisticVos(period,
                    statisListAll);

            //保存到redis
            chartMap.put(TrendConstant.KEY_TREND_STATISTICS, statisList);
            chartMap.put(TrendConstant.KEY_TREND_PERIOD, chartList);
            redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, chartMap);
        }
    }

    private void selectContinueCode(ArrayList<Map<String, Object>> periodList, int lastLength) {
        //空间记录位置 left:左 leftDown:左下 down3:下 rightDown:右下  连号结尾标识 0:结尾 1:未结尾
        //index_left : 0   某一行的index_方向 是否结尾
        List<Map<String, Integer>> spaceIndex = new ArrayList<>();
        for (int i = 0; i < periodList.size() - lastLength; i++) {//i为行号
            List<Integer> omitNums = (List<Integer>) periodList.get(i).get("omitNum");
            if (omitNums == null) {
                omitNums = (List<Integer>) periodList.get(i).get("omitNums");
            }
            Map<String, Integer> spaceIndexMap = null;
            if (spaceIndex.size() > i && spaceIndex.get(i) != null) {
                spaceIndexMap = spaceIndex.get(i);
            } else {
                spaceIndexMap = new HashMap<>();
            }
            for (int j = 0; j < omitNums.size(); j++) {
                if (omitNums.get(j) != 0 && omitNums.get(j) != -2) {
                    continue;
                }
                //左三
                if (j < (omitNums.size() - 2)) {//j为列号
                    //找上一位看是否是连号末尾
                    if (!checkEndContinue(i, j - 1, spaceIndex, "left")) {
                        if ((omitNums.get(j + 1) == 0 || omitNums.get(j + 1) == -2) && (omitNums.get(j + 2) == 0 ||
                                omitNums.get(j + 2) == -2)) {
                            for (int k = j; k < omitNums.size(); k++) {
                                if (omitNums.get(k) == 0 || omitNums.get(k) == -2) {
                                    omitNums.set(k, -2);
                                    if (k + 1 >= omitNums.size() || (omitNums.get(k + 1) != 0 && omitNums.get(k + 1)
                                            != -2)) {
                                        spaceIndexMap.put(j + "_left", 0);
                                    } else {
                                        spaceIndexMap.put(j + "_left", 1);
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
                //左下 -- 下3 -- 右下
                if (i < periodList.size() - lastLength - 2) {
                    List<Integer> nextOmitNums = (List<Integer>) periodList.get(i + 1).get("omitNum");
                    List<Integer> afterOmitNums = (List<Integer>) periodList.get(i + 2).get("omitNum");
                    //左下
                    if (j < omitNums.size() - 2 && !checkEndContinue(i - 1, j - 1, spaceIndex, "leftDown")) {
                        if ((nextOmitNums.get(j + 1) == 0 || nextOmitNums.get(j + 1) == -2) && (afterOmitNums.get(j + 2)
                                == 0 || afterOmitNums.get(j + 2) == -2)) {
                            int count = j;
                            for (int k = i; k < periodList.size() - lastLength; k++) {
                                List<Integer> tempOmitNums = (List<Integer>) periodList.get(k).get("omitNum");
                                List<Integer> tempNextOmitNums = new ArrayList<>();
                                if (k + 1 < periodList.size() - lastLength) {
                                    tempNextOmitNums = (List<Integer>) periodList.get(k + 1).get("omitNum");
                                }
                                if (count == 33) {
                                    break;
                                }
                                if (tempOmitNums.get(count) == 0 || tempOmitNums.get(count) == -2) {
                                    tempOmitNums.set(count, -2);
                                    if (count > nextOmitNums.size() - 2 || tempNextOmitNums == null ||
                                            tempNextOmitNums.size() == 0 || (tempNextOmitNums.get(count + 1) != 0 &&
                                            tempNextOmitNums.get(count + 1) != -2)) {
                                        spaceIndexMap.put(j + "_leftDown", 0);
                                    } else {
                                        spaceIndexMap.put(j + "_leftDown", 1);
                                    }
                                } else {
                                    break;
                                }
                                if (count < tempOmitNums.size() - 1) {
                                    count++;
                                } else {
                                    break;
                                }
                                periodList.get(k).put("omitNum", tempOmitNums);
                            }
                        }
                    }
                    //右下
                    if (j >= 2 && !checkEndContinue(i - 1, j + 1, spaceIndex, "rightDown")) {
                        if ((nextOmitNums.get(j - 1) == 0 || nextOmitNums.get(j - 1) == -2) && (afterOmitNums.get(j -
                                2) == 0 || afterOmitNums.get(j - 2) == -2)) {
                            int count = j;
                            for (int k = i; k < periodList.size() - lastLength; k++) {
                                List<Integer> tempOmitNums = (List<Integer>) periodList.get(k).get("omitNum");
                                List<Integer> tempNextOmitNums = null;
                                if (k + 1 < periodList.size() - lastLength) {
                                    tempNextOmitNums = (List<Integer>) periodList.get(k + 1).get("omitNum");
                                }

                                if (tempOmitNums.get(count) == 0 || tempOmitNums.get(count) == -2) {
                                    tempOmitNums.set(count, -2);
                                    if (count <= 0 || tempNextOmitNums == null || (tempNextOmitNums.get(count - 1) != 0
                                            && tempNextOmitNums.get(count - 1) != -2)) {
                                        spaceIndexMap.put(j + "_rightDown", 0);
                                    } else {
                                        spaceIndexMap.put(j + "_rightDown", 1);
                                    }
                                } else {
                                    break;
                                }
                                if (count > 0) {
                                    count--;
                                } else {
                                    break;
                                }
                                periodList.get(k).put("omitNum", tempOmitNums);
                            }
                        }
                    }
                    //下3
                    if (i < periodList.size() - lastLength - 2 && !checkEndContinue(i - 1, j, spaceIndex, "down3")) {
                        if ((nextOmitNums.get(j) == 0 || nextOmitNums.get(j) == -2) && (afterOmitNums.get(j) == 0 ||
                                afterOmitNums.get(j) == -2)) {
                            for (int k = i; k < periodList.size() - lastLength; k++) {
                                List<Integer> tempOmitNums = (List<Integer>) periodList.get(k).get("omitNum");

                                if (k + 1 == periodList.size() - lastLength) {
                                    if (tempOmitNums.get(j) == 0) {
                                        tempOmitNums.set(j, -2);
                                        spaceIndexMap.put(j + "_down3", 0);
                                    }
                                } else {
                                    List<Integer> tempNextOmitNums = (List<Integer>) periodList.get(k + 1).get
                                            ("omitNum");
                                    if (tempOmitNums.get(j) == 0 || tempOmitNums.get(j) == -2) {
                                        tempOmitNums.set(j, -2);
                                        if (tempNextOmitNums.get(j) != 0 && tempNextOmitNums.get(j) != -2) {
                                            spaceIndexMap.put(j + "_down3", 0);
                                        } else {
                                            spaceIndexMap.put(j + "_down3", 1);
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                periodList.get(k).put("omitNum", tempOmitNums);
                            }
                        }
                    }
                }
            }
            if (spaceIndex.size() > i) {
                spaceIndex.set(i, spaceIndexMap);
            }
            if (periodList.size() - lastLength > i) {
                periodList.get(i).put("omitNum", omitNums);
            }
        }
    }

    //查看是否连尾号
    //是末尾返回false  不是true
    private Boolean checkEndContinue(int rowNum, int currentIndex, List<Map<String, Integer>> spaceIndex, String
            direction) {
        Boolean res = Boolean.FALSE;
        if (rowNum < 0 || currentIndex < 0 || rowNum >= spaceIndex.size()) {
            return res;
        }
        Map<String, Integer> indexInfoMap = spaceIndex.get(rowNum);
        if (indexInfoMap.get(currentIndex + "_" + direction) != null || indexInfoMap.get(currentIndex + "_" +
                direction) == 1) {
            res = Boolean.TRUE;
        }
        return res;
    }

    @Override
    public void tableChart(ChartEnumInterface chartEnum, long gameId, String currentPeriodId) {
        String currentChartKey = RedisConstant.getCurrentChartKey(gameId, currentPeriodId, chartEnum.toString(), null);
        redisService.del(currentChartKey);

        List<Map<String, Object>> coldHotMapList = TrendChartUtil.calColdHotMapList(gameId, currentPeriodId,
                chartEnum, redisService);
        redisService.kryoSetEx(currentChartKey, RedisConstant.EXPIRE_TREND_COMMON, coldHotMapList);

    }
}
