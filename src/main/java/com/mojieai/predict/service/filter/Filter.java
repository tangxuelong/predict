package com.mojieai.predict.service.filter;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.FilterEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by tangxuelong on 2017/8/16.
 */
public abstract class Filter {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private RedisService redisService;

    /* 过滤*/
    /*
    * @prams
    * @lotteryNumber号码
    * @action过滤条件
    * 每个条件之间用$符号分割
    * 每个条件的key和value之间用@分割
    * 分页 每页200条记录
    * */
    public Map<String, Object> getFilterResult(String gameEn, String lotteryNumber, String action, String
            matrixAction, Integer pageIndex) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String showText;
            Integer[] resultNum;
            Integer nextPage;
            Integer isHaveNextPage;
            Integer pageCount = 1000;
            if (null == pageIndex) {
                pageIndex = 0;
            }
            /* 红球和红球的号码个数*/
            String[] ballArr = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
            String[] redBallArr = ballArr[0].split(CommonConstant.SPACE_SPLIT_STR);
            Arrays.sort(redBallArr);
            String blueBall = ballArr[1];
            String[] blueBallArr = blueBall.split(CommonConstant.SPACE_SPLIT_STR);
            List<String> resultList = new LinkedList<>();
            /* 类型转换*/
            Integer[] redBallsInt = new Integer[redBallArr.length];
            for (int i = 0; i < redBallArr.length; i++) {
                redBallsInt[i] = Integer.valueOf(redBallArr[i]);
            }
            // 缓存以过滤前的数量为 红球数1W
            // 缓存 key 添加红球号码
            String matrixActionKey = RedisConstant.getMatrixActionKey(gameEn, ballArr[0], matrixAction, action);
            String matrixActionShowTextKey = RedisConstant.getMatrixActionShowTextKey(gameEn, ballArr[0], matrixAction,
                    action);
            // 先从缓存中取
            List<String> redisRedList = redisService.lRange(matrixActionKey, Long.valueOf(pageIndex *
                    pageCount), Long.valueOf(pageIndex * pageCount + (pageCount - 1)));
            if (null == redisRedList || redisRedList.size() == 0) {
                if (StringUtils.isNotBlank(matrixAction)) {
                    /* 旋转矩阵 先从缓存中取，如果没有，读文件后写入缓存*/
                    String matrixListKey = RedisConstant.getMatrixListKey(gameEn, redBallArr.length, matrixAction);
                    List<String> matrixList = redisService.kryoZRangeByScoreGet(matrixListKey, 0L, -1L, String.class);
                    Integer totalCount = getRedBallsCombine(redBallArr, blueBallArr);
                    Integer filterCount = 0;
                    if (null == matrixList || matrixList.size() == 0) {
                        /* 读文件*/
                        String urlSeparator = String.valueOf(File.separatorChar);
                        String path = getClass().getResource(".").getPath();
                        String subPath = path.substring(0, path.indexOf("WEB-INF") + 8);
                        String rootPath = subPath.substring(0, subPath.lastIndexOf(urlSeparator));
                        rootPath = rootPath + File.separatorChar + "classes" + File.separatorChar + "filterfile" + File
                                .separatorChar + gameEn + File.separatorChar + gameEn + (redBallArr.length < 10 ? "0" +
                                redBallArr.length : redBallArr.length) + matrixAction + ".txt";
                        File file = new File(rootPath);
                        InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt;
                        Map<Object, Long> matrixListStore = new HashMap<>();
                        while (StringUtils.isNotBlank(lineTxt = bufferedReader.readLine())) {
                            matrixListStore.put(lineTxt, System.currentTimeMillis());
                            if (!matrixFilterAction(lineTxt, redBallArr, redBallsInt, action, blueBall, resultList)) {
                                continue;
                            }
                            filterCount++;
                        }
                        read.close();
                        redisService.kryoZAddSet(matrixListKey, matrixListStore);
                        redisService.expire(matrixListKey, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY);
                    } else {
                        for (String matrixNumber : matrixList) {
                            if (!matrixFilterAction(matrixNumber, redBallArr, redBallsInt, action, blueBall,
                                    resultList)) {
                                continue;
                            }
                            filterCount++;
                        }
                    }
                    resultNum = new Integer[]{totalCount, filterCount};
                } else {
                    /* 大复式过滤*/
                    resultNum = bigMultipleFilter(resultList, redBallArr, redBallsInt, blueBall, action);
                }
                storeResult(resultNum[0], matrixActionKey, resultList, blueBall);
                showText = FilterEnum.getResultShowText(resultNum);
                nextPage = FilterEnum.getNextPage(resultNum[1], pageCount, pageIndex);
                isHaveNextPage = nextPage == pageIndex ? 0 : 1;
                redisService.kryoSetEx(matrixActionShowTextKey, RedisConstant.EXPIRE_TIME_SECOND_FIVE_MINUTE +
                        RedisConstant.EXPIRE_TIME_SECOND_FIVE_MINUTE, showText);
                if (pageIndex * pageCount + (pageCount - 1) <= resultList.size()) {
                    resultList = resultList.subList(pageIndex * pageCount, pageIndex * pageCount + (pageCount - 1));
                } else {
                    if (pageIndex * pageCount <= resultList.size()) {
                        resultList = resultList.subList(pageIndex * pageCount, resultList.size());
                    } else {
                        // 防止客户端传入不存在的pageIndex
                        resultList = new ArrayList<>();
                        isHaveNextPage = 0;
                    }
                }
            } else {
                /* 缓存只有红球的set 需要拼接篮球*/
                resultList = redisRedList;
                showText = redisService.kryoGet(matrixActionShowTextKey, String.class);
                nextPage = FilterEnum.getNextPage(Integer.valueOf(showText.substring(showText.indexOf("后") + 1,
                        showText.indexOf("注，缩水率"))), pageCount, pageIndex);
                isHaveNextPage = nextPage == pageIndex ? 0 : 1;
            }
            resultMap.put("showText", showText);
            resultMap.put("nextPage", nextPage);
            resultMap.put("isHaveNextPage", isHaveNextPage);
            resultMap.put("currentPage", pageIndex);

            GamePeriod period = PeriodRedis.getLastOpenPeriodByGameId(GameCache.getGame(gameEn).getGameId());
            GamePeriod currentPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(GameCache.getGame(gameEn)
                    .getGameId(), period.getPeriodId());
            resultMap.put("periodId", currentPeriod.getPeriodId());

            List<String> filterResult = new ArrayList<>();
            for (String filterRedBall : resultList) {
                filterResult.add(filterRedBall + ":" + blueBall);
            }
            resultMap.put("filterResult", filterResult);
            return resultMap;
        } catch (Exception e) {
            log.error("ssq getFilterResult error" + e.getMessage());
            throw new BusinessException("ssq getFilterResult error", e);
        }
    }

    /* 过滤条件展示*/
    public Map<String, Object> getFilterIndexShow(String gameEn) {
        try {
            Map<String, Object> filterIndexShow = new HashMap<>();

            /* 旋转矩阵*/
            filterIndexShow.put("matrix", getMatrixList());

            List<GamePeriod> periodList = PeriodRedis.getHistory50AwardPeriod(GameCache.getGame(gameEn).getGameId());
            /* 过滤*/
            List<Map<String, Object>> filterList = new ArrayList<>();
            for (FilterEnum filterEnum : FilterEnum.values()) {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("filterName", filterEnum.getFilterName(gameEn));
                filterMap.put("filterAction", filterEnum.getFilterAction());
                filterMap.put("filterType", filterEnum.getFilterType(gameEn));
                filterMap.put("filterTitle", filterEnum.getFilterTitle(gameEn));
                filterMap.put("filterRangeName", filterEnum.getFilterRangeName(gameEn));
                filterMap.put("filterRange", filterEnum.getFilterRange(gameEn));
                filterMap.put("filterLimit", filterEnum.getFilterLimit(gameEn));
                filterMap.put("filterCustomName", filterEnum.getFilterCustomName(gameEn));
                filterMap.put("filterCustom", filterEnum.getFilterCustom(gameEn));
                filterMap.put("filterRecommends", filterEnum.rebuildRecommend(gameEn, periodList));
                filterMap.put("filterIntroduction", filterEnum.getFilterIntroduction(gameEn));
                filterMap.put("filterIsMultipleChoose", filterEnum.getIsMultipleChoose(gameEn));
                filterList.add(filterMap);
            }
            filterIndexShow.put("filter", filterList);
            return filterIndexShow;
        } catch (Exception e) {
            log.error("ssq getFilterIndexShow error:" + e.getMessage());
            throw new BusinessException("ssq getFilterIndexShow error:" + e.getMessage());
        }
    }

    /* 前端展示不同的彩种的旋转矩阵*/
    public abstract List<Map<String, String>> getMatrixList();

    /* 不同彩种的篮球组合数*/
    public abstract String[] getBlueCombine(String[] blueArr);

    /* 组合数*/
    public abstract Integer getRedBallsCombine(String[] redArr, String[] blueArr);

    public abstract Integer getBallsCombine(Integer redCount, Integer blueCount);

    /* 不同彩种的大复式过滤*/
    public abstract Integer[] bigMultipleFilter(List<String> resultList, String[] redBalls, Integer[] redBallsInt,
                                                String blueBall, String action);

    /* 不同彩种的旋转矩阵检查*/
    public abstract String checkMatrixAction(String matrixAction, String lotteryNumber);

    /* 过滤*/
    public Boolean filterAction(List<Integer> redBalls, String action) {
        /* 条件过滤*/
        String[] actions = action.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_DOLLAR_STR);
        for (String actionMap : actions) {
            String[] actionWithValue = actionMap.split(CommonConstant.COMMON_AT_STR);
            FilterEnum filterEnum = FilterEnum.getFilterEnum(actionWithValue[0]);
            if (!filterEnum.filterAction(redBalls, actionWithValue[1], getGameEn())) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public abstract String getGameEn();

    public Boolean matrixFilterAction(String matrixNumber, String[] redBallArr, Integer[] redBallsInt, String action,
                                      String blueBall, List<String> resultList) {
        try {
            String[] placeArray = matrixNumber.split(" ");
            String number = "";
            List<Integer> redBalls = new ArrayList<>();
            /* 红球*/
            for (String place : placeArray) {
                number = number + redBallArr[Integer.parseInt(place) - 1] + " ";
                redBalls.add(redBallsInt[Integer.parseInt(place) - 1]);
            }
            /* 过滤*/
            if (null != action && !filterAction(redBalls, action)) {
                return Boolean.FALSE;
            }
            resultList.add(number.substring(0, number.length() - 1));
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("matrixFilterAction error " + Arrays.toString(redBallArr) + matrixNumber, e);
            throw new BusinessException("matrixFilterAction error ", e);
        }

    }

    public void storeResult(Integer totalCount, String matrixActionKey, List<String> resultList, String blueBall) {
        /* 过滤前的个数*/
        if (totalCount >= 10000) {
            /* 红球结果 过期时间 5分钟*/
            Boolean isDiv = Boolean.TRUE;
            while (isDiv) {
                List<String> storeList;
                if (resultList.size() - (524287) > 0) {
                    storeList = resultList.subList(0, 524287 - 1);
                    resultList = resultList.subList(524287, resultList.size() - 1);
                } else {
                    storeList = resultList;
                    isDiv = Boolean.FALSE;
                }
                if (storeList != null && storeList.size() > 0) {
                    redisService.kryoLPushStr(matrixActionKey, storeList.toArray(new String[storeList.size()]));
                }
            }
            redisService.expire(matrixActionKey, RedisConstant.EXPIRE_TIME_SECOND_FIVE_MINUTE);
            /* 篮球号码存一下*/
            redisService.kryoSetEx(RedisConstant.getMatrixActionBlueKey(matrixActionKey), RedisConstant
                    .EXPIRE_TIME_SECOND_FIVE_MINUTE, blueBall);
        }

    }
}
