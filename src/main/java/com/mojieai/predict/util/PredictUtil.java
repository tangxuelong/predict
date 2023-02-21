package com.mojieai.predict.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictNumbers;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.enums.predict.SsqPickNumEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.game.GameFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class PredictUtil {
    private static Logger log = LogConstant.commonLog;

    public static Set<String> decompressGBList(byte[] predictNumbers, String periodId) {
        if (predictNumbers == null) {
            log.error("could not get raw number for periodId=" + periodId);
            throw new BusinessException("could not get raw number for periodId=" + periodId);
        }
        Set<String> gbListSet = new HashSet<>();
        List<String> gbList;
        try {
            Long startTime = System.currentTimeMillis();
            String str = GzipUtil.uncompress(predictNumbers);
            gbList = JSON.parseArray(str, String.class);
            Long endTime = System.currentTimeMillis();
            if (endTime - startTime > CommonConstant.MAX_COMPRESS_TIME) {
                log.error("gzip compress cost more than " + CommonConstant.MAX_COMPRESS_TIME + "ms.");
            }
            if (gbList == null || gbList.isEmpty()) {
                log.error("decompress raw data failed, orderId=" + periodId);
                throw new BusinessException("号码处理错误");
            }
            for (String temp : gbList) {
                gbListSet.add(temp);
            }
        } catch (Exception ex) {
            log.error("decompress raw lottery number encountered exception, orderId=" + periodId, ex);
            throw new BusinessException("号码处理错误");
        }
        return gbListSet;
    }

    public static byte[] compressGBList(Set<String> rawGBList, long gameId, String periodId) {
        byte[] compressedRN = null;
        try {
            Long startTime = System.currentTimeMillis();
            String str = JSONObject.toJSONString(rawGBList);
            compressedRN = GzipUtil.compress(str);
            Long endTime = System.currentTimeMillis();
            if (endTime - startTime > CommonConstant.MAX_COMPRESS_TIME) {
                log.error("gzip compress cost more than " + CommonConstant.MAX_COMPRESS_TIME + "ms for gameId="
                        + gameId + " periodId=" + periodId);
            }
        } catch (Exception ex) {
            log.error("compress lottery number error, gameId=" + gameId + " periodId=" + periodId, ex);
            throw new BusinessException("预测号码处理错误");
        }
        return compressedRN;
    }

    /*将号码过滤处理*/
    public static Set<String> filterPredictNum(long gameId, String periodId, List<String> allPredictNum, int
            predictCount) {
        int allNumCount = allPredictNum.size();
        Set<String> result = new HashSet<>();
        List<GamePeriod> periods = PeriodRedis.getLastAllOpenPeriodsByGameId(gameId);
        Map tempMap = new HashMap();
        for (GamePeriod gamePeriod : periods) {
            String key = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0].trim();
            tempMap.put(key, gamePeriod.getPeriodId());
        }
        List<Integer> orderList = new ArrayList<>();
        for (int i = 0; i < allPredictNum.size(); i++) {
            orderList.add(i);
        }
        Collections.shuffle(orderList, new Random(System.currentTimeMillis()));
        int count = 0;
        Set<String> allConsecutiveNums = GameFactory.getInstance().getGameBean(gameId).getDefaultConsecutiveNumbers();
        for (int i = 0; i < orderList.size(); i++) {
            if (count >= predictCount) {
                break;
            }
            String orderNums = TrendUtil.orderNum(allPredictNum.get(orderList.get(i)));
            String redCodeNum = orderNums.split(CommonConstant.COMMON_COLON_STR)[0].trim();
            if ((allNumCount - predictCount) > 0) {
                if (tempMap.get(redCodeNum) != null || !filterConsecutiveNumbers(redCodeNum, allConsecutiveNums)) {
                    allNumCount--;
                    continue;
                }
                result.add(orderNums);
                count++;
            }
        }
        return result;
    }

    /*过滤4连号以及以上的连号码*/
    //如果是连号返回false  不是连号返回true
    public static Boolean filterConsecutiveNumbers(String redCodeNum, Set<String> allConsecutiveNums) {
        Boolean result = Boolean.FALSE;
        for (String nums : allConsecutiveNums) {
            if (!redCodeNum.contains(nums)) {
                return true;
            }
        }
        return result;
    }

    /**
     * 5+n
     *
     * @param redWinNum
     * @param redBalls
     * @param blueBall
     * @return 蓝球取了指定的一个（指定蓝球为中奖蓝球，三等奖。指定蓝球为非中奖蓝球就是四等奖）
     */
    public static Set<String> ssqGenerateFouthPrizeOnlyRedBall(List<String> redWinNum, List<String> redBalls, String
            blueBall) {
        if (redWinNum.size() < 6) {
            throw new BusinessException("红球个数不能小于6");
        }
        Set<String> result = new HashSet<>();

        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                for (int k = j + 1; k < 4; k++) {
                    for (int m = k + 1; m < 5; m++) {
                        for (int n = m + 1; n < 6; n++) {
                            for (int redIndex = 0; redIndex < redBalls.size(); redIndex++) {
                                String tempNum = redWinNum.get(i) + CommonConstant.SPACE_SPLIT_STR + redWinNum.get(j) +
                                        CommonConstant.SPACE_SPLIT_STR + redWinNum.get(k) + CommonConstant
                                        .SPACE_SPLIT_STR + redWinNum.get(m) + CommonConstant.SPACE_SPLIT_STR + redWinNum
                                        .get(n) + CommonConstant.SPACE_SPLIT_STR + redBalls.get(redIndex) +
                                        CommonConstant.COMMON_COLON_STR + blueBall;
                                result.add(TrendUtil.orderNum(tempNum.trim()));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<String> dltGenerateFivePrize3And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                         List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        result.addAll(dltBaseGenerateFivePrize2And2(redWinBalls, blueWinBalls, redBalls));
        result.addAll(dltBaseGenerateFivePrize3And1(redWinBalls, blueWinBalls, redBalls, blueBalls));
        return result;
    }

    public static List<String> dltGenerateSixPrize2And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                        List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        result.addAll(dltBaseGenerateSixPrize2And1(redWinBalls, blueWinBalls, redBalls, blueBalls));
        result.addAll(dltBaseGenerateSixPrize1And2(redWinBalls.get(0), blueWinBalls, redBalls));
        result.addAll(dltBaseGenerateSixPrize0And2(blueWinBalls, redBalls));
        return result;
    }

    public static List<String> dltGenerateSixPrize3And1(List<String> redWinBalls, List<String> blueWinBalls,
                                                        List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();

        result.addAll(dltBaseGenerateSixPrize2And1(redWinBalls, blueWinBalls, redBalls, blueBalls));
        result.addAll(dltBaseGenetateSixPrize3And0(redWinBalls, blueWinBalls, redBalls));

        return result;
    }

    public static List<String> dltGenerateSixPrize3And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                        List<String> redBalls, List<String> blueBalls) {
        /*几个条件都加上有9K，去除红球少的*/
        List<String> result = new ArrayList<>();
//        result.addAll(dltBaseGenerateSixPrize0And2(blueWinBalls, redBalls));
//        result.addAll(dltBaseGenerateSixPrize1And2(redWinBalls.get(0), blueWinBalls, redBalls));
        result.addAll(dltBaseGenetateSixPrize3And0(redWinBalls, redBalls, blueBalls));
        result.addAll(dltBaseGenerateSixPrize2And1(redWinBalls, blueWinBalls, redBalls, blueBalls));
        return result;
    }

    public static List<String> ssqGenerateFourPrize6And0(List<String> redWinBalls, List<String> redBalls,
                                                         List<String> blueBalls, int maxCount) {
        if (redWinBalls.size() < 6) {
            throw new BusinessException("红球个数不足");
        }
        List<String> result = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                for (int k = j + 1; k < 4; k++) {
                    for (int m = k + 1; m < 5; m++) {
                        for (int n = m + 1; n < 6; n++) {
                            if (result.size() >= maxCount) {
                                return result;
                            }
                            String tempConfirm = redWinBalls.get(i) + CommonConstant.SPACE_SPLIT_STR + redWinBalls
                                    .get(j) + CommonConstant.SPACE_SPLIT_STR + redWinBalls
                                    .get(k) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(m) + CommonConstant
                                    .SPACE_SPLIT_STR + redWinBalls.get(n);
                            result.addAll(ssqBaseGenerateFourPrize5And0(tempConfirm, redBalls, blueBalls.get(0),
                                    PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<String> ssqGenerateFivePrize6And0(List<String> redWinBalls, List<String> redBalls,
                                                         List<String> blueBalls, int maxCount) {
        if (redWinBalls.size() < 6) {
            throw new BusinessException("红球个数不足");
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                for (int k = j + 1; k < 5; k++) {
                    for (int m = k + 1; m < 6; m++) {
                        if (result.size() >= maxCount) {
                            return result;
                        }
                        String tempConfirm = redWinBalls.get(i) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(j)
                                + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(k) + CommonConstant
                                .SPACE_SPLIT_STR + redWinBalls.get(m);
                        result.addAll(ssqBaseGenerateFivePrize4And0(tempConfirm, redBalls, blueBalls.get(0),
                                PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
                    }
                }
            }
        }

        return result;
    }

    public static List<String> ssqGenerateFivePrize5And0(List<String> redWinBalls, List<String> redBalls,
                                                         List<String> blueBalls, int maxCount) {
        if (redWinBalls.size() < 5) {
            throw new BusinessException("红球个数不足");
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                for (int k = j + 1; k < 4; k++) {
                    for (int m = k + 1; m < 5; m++) {
                        if (result.size() >= maxCount) {
                            return result;
                        }
                        String tempConfirm = redWinBalls.get(i) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(j)
                                + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(k) + CommonConstant
                                .SPACE_SPLIT_STR + redWinBalls.get(m);
                        result.addAll(ssqBaseGenerateFivePrize4And0(tempConfirm, redBalls, blueBalls.get(0),
                                PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
                    }
                }
            }
        }
        return result;
    }

    /***ssq四等奖基础方法***/
    public static Set<String> ssqBaseGenerateFourPrize5And0(String confirmFiveRedBall, List<String> redBalls,
                                                            String blueBall, int maxCount) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < redBalls.size(); i++) {
            if (result.size() >= maxCount) {
                break;
            }
            String temp = confirmFiveRedBall.trim() + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                    .COMMON_COLON_STR + blueBall;
            result.add(TrendUtil.orderNum(temp.trim()));
        }
        return result;
    }

    /***ssq五等奖基础方法***/
    public static Set<String> ssqBaseGenerateFivePrize4And0(String confirmFourRedBall, List<String> redBalls, String
            blueBall, int maxCount) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < redBalls.size() - 1; i++) {
            if (result.size() >= maxCount) {
                break;
            }
            for (int j = i + 1; j < redBalls.size(); j++) {
                if (result.size() >= maxCount) {
                    break;
                }
                String tempNum = confirmFourRedBall.trim() + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) +
                        CommonConstant.SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.COMMON_COLON_STR + blueBall;
                result.add(TrendUtil.orderNum(tempNum));
            }
        }
        return result;
    }

    /*** 二等奖基础方法***/
    public static List<String> dltBaseGenerateSecoundPrize(String winNum, List<String> blueBalls) {
        if (StringUtils.isBlank(winNum)) {
            throw new BusinessException("开奖号码不能为空");
        }
        List<String> result = new ArrayList<>();
        String[] winNumArr = winNum.split(CommonConstant.COMMON_COLON_STR);
        String[] blueWinNum = winNum.split(CommonConstant.SPACE_SPLIT_STR);
        for (String winBlueTemp : blueWinNum) {
            for (int i = 0; i < blueBalls.size(); i++) {
                String predictNum = winNumArr[0] + CommonConstant.COMMON_COLON_STR + winBlueTemp + CommonConstant
                        .SPACE_SPLIT_STR + blueBalls.get(0);
                result.add(TrendUtil.orderNum(predictNum));
            }

        }
        return result;
    }

    /*** 三等奖基础***/
    public static List<String> dltBaseGenerateThridPrize5And0(String winNum, List<String> blueBalls) {
        List<String> result = new ArrayList<>();

        String[] winNumArr = winNum.split(CommonConstant.COMMON_COLON_STR);
        for (int i = 0; i < blueBalls.size() - 1; i++) {
            for (int j = i + 1; j < blueBalls.size(); j++) {
                String temp = winNumArr[0] + CommonConstant.COMMON_COLON_STR + blueBalls.get(i) + CommonConstant
                        .SPACE_SPLIT_STR + blueBalls.get(j);
                if (!result.contains(TrendUtil.orderNum(temp))) {
                    result.add(TrendUtil.orderNum(temp));
                }
            }
        }
        return result;
    }

    public static List<String> dltBaseGenerateThridPrize4And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                              List<String> redBalls) {
        if (redWinBalls.size() != 4 || blueWinBalls.size() != 2) {
            throw new BusinessException("dlt构建四等奖4-2红球个数不能少于4个, 蓝球个数必须为2");
        }
        List<String> result = new ArrayList<>();
        StringBuffer redWinNumStr = new StringBuffer();
        redWinBalls.forEach(n -> redWinNumStr.append(n).append(CommonConstant.SPACE_SPLIT_STR));
        String restBalls = redWinNumStr.toString();

        for (String redTemp : redBalls) {
            String tempNum = restBalls + redTemp + CommonConstant.COMMON_COLON_STR + blueWinBalls.get(0) +
                    blueWinBalls.get(1);
            if (!result.contains(TrendUtil.orderNum(tempNum))) {
                result.add(TrendUtil.orderNum(tempNum));
            }
        }
        return result;
    }

    /*** 四等奖基础方法***/
    public static List<String> dltBaseGenerateFourPrize4And1(List<String> redWinBalls, String blueWinBall,
                                                             List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        String confirmNum = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) + CommonConstant
                .SPACE_SPLIT_STR + redWinBalls.get(2) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(3);
        for (int i = 0; i < redBalls.size(); i++) {
            String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                    .COMMON_COLON_STR + blueWinBall + CommonConstant.SPACE_SPLIT_STR + blueBalls.get(0);
            result.add(TrendUtil.orderNum(tempNum));
        }
        return result;
    }

    public static List<String> dltBaseGenerateFourPrize3And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                             List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        String confirmNum = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) + CommonConstant
                .SPACE_SPLIT_STR + redWinBalls.get(2);
        for (int i = 0; i < redBalls.size() - 1; i++) {
            for (int j = i + 1; j < redBalls.size(); j++) {
                String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                        .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.COMMON_COLON_STR + blueWinBalls.get(0)
                        + CommonConstant.SPACE_SPLIT_STR + blueBalls.get(1);
                result.add(TrendUtil.orderNum(tempNum));
            }
        }
        return result;
    }

    /*** 五等奖基础方法  三个for循环4060没必要这么多***/
    public static List<String> dltBaseGenerateFivePrize2And2(List<String> redWinBalls, List<String> blueWinBalls,
                                                             List<String> redBalls) {
        List<String> result = new ArrayList<>();

        String confirmNums = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) + CommonConstant
                .SPACE_SPLIT_STR + redBalls.get(0);
        for (int i = 1; i < redBalls.size() - 2; i++) {
            for (int j = i + 1; j < redBalls.size() - 1; j++) {
                String tempNum = confirmNums + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                        .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.COMMON_COLON_STR + blueWinBalls.get
                        (0) + CommonConstant.SPACE_SPLIT_STR + blueWinBalls.get(1);
                result.add(TrendUtil.orderNum(tempNum));
            }
        }
        return result;
    }

    public static List<String> dltBaseGenerateFivePrize3And1(List<String> redWinBalls, List<String> blueWinBalls,
                                                             List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        String frontWinNum = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) +
                CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(2);
        for (int i = 0; i < redBalls.size() - 1; i++) {
            for (int j = i + 1; j < redBalls.size(); j++) {
                String temp = frontWinNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                        .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.COMMON_COLON_STR + blueWinBalls.get(0)
                        + CommonConstant.SPACE_SPLIT_STR + blueBalls.get(0);
                result.add(TrendUtil.orderNum(temp));
            }
        }
        return result;
    }

    public static List<String> dltBaseGenerateFivePrize4And0(List<String> redWinBalls, List<String> redBalls,
                                                             List<String> blueBalls) {
        List<String> result = new ArrayList<>();

        String confirmNum = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) + CommonConstant
                .SPACE_SPLIT_STR + redWinBalls.get(2) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(3);
        for (int i = 0; i < redBalls.size(); i++) {
            String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                    .COMMON_COLON_STR + blueBalls.get(0) + blueBalls.get(1);
            result.add(TrendUtil.orderNum(tempNum));
        }
        return result;
    }

    /*** 六等奖基础方法**/
    public static List<String> dltBaseGenerateSixPrize1And2(String redWinBall, List<String> blueWinBalls,
                                                            List<String> redBalls) {
        List<String> result = new ArrayList<>();
        String confirmNum = redBalls.get(0);
        for (int i = 1; i < redBalls.size() - 3; i++) {
            for (int j = i + 1; j < redBalls.size() - 2; j++) {
                for (int k = j + 1; k < redBalls.size() - 1; k++) {
                    String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redWinBall + CommonConstant
                            .SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant.SPACE_SPLIT_STR + redBalls.get(j) +
                            CommonConstant.SPACE_SPLIT_STR + redBalls.get(k) + CommonConstant.SPACE_SPLIT_STR +
                            CommonConstant.COMMON_COLON_STR + blueWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR +
                            blueWinBalls.get(1);
                    result.add(TrendUtil.orderNum(tempNum));
                }
            }
        }

        return result;
    }

    public static List<String> dltBaseGenerateSixPrize2And1(List<String> redWinBalls, List<String> blueWinNums,
                                                            List<String> redBalls, List<String> blueBalls) {
        List<String> result = new ArrayList<>();

        String confirmNums = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1);
        for (int i = 0; i < redBalls.size() - 2; i++) {
            for (int j = i + 1; j < redBalls.size() - 1; j++) {
                for (int k = j + 1; k < redBalls.size(); k++) {
                    String tempNum = confirmNums + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                            .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.SPACE_SPLIT_STR + redBalls.get(k)
                            + CommonConstant.COMMON_COLON_STR + blueBalls.get(0) + CommonConstant.SPACE_SPLIT_STR +
                            blueWinNums.get(0);
                    result.add(TrendUtil.orderNum(tempNum));
                }
            }
        }

        return result;
    }

    public static List<String> dltBaseGenetateSixPrize3And0(List<String> redWinBalls, List<String> redBalls,
                                                            List<String> blueBalls) {
        List<String> result = new ArrayList<>();
        String confirmNum = redWinBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redWinBalls.get(1) + CommonConstant
                .SPACE_SPLIT_STR + redWinBalls.get(2);
        for (int i = 0; i < redBalls.size() - 1; i++) {
            for (int j = i + 1; j < redBalls.size() - 1; j++) {
                String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                        .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.COMMON_COLON_STR + blueBalls.get(0)
                        + CommonConstant.SPACE_SPLIT_STR + blueBalls.get(1);
                result.add(TrendUtil.orderNum(tempNum));
            }
        }
        return result;
    }

    public static List<String> dltBaseGenerateSixPrize0And2(List<String> blueWinNums, List<String> redBalls) {
        List<String> result = new ArrayList<>();
        String confirmNum = redBalls.get(0) + CommonConstant.SPACE_SPLIT_STR + redBalls.get(1);
        for (int i = 2; i < redBalls.size() - 3; i++) {
            for (int j = i + 1; j < redBalls.size() - 2; j++) {
                for (int k = j + 1; k < redBalls.size() - 1; k++) {
                    String tempNum = confirmNum + CommonConstant.SPACE_SPLIT_STR + redBalls.get(i) + CommonConstant
                            .SPACE_SPLIT_STR + redBalls.get(j) + CommonConstant.SPACE_SPLIT_STR + redBalls
                            .get(k) + CommonConstant.COMMON_COLON_STR + blueWinNums.get(0) + CommonConstant
                            .SPACE_SPLIT_STR + blueWinNums.get(1);
                    result.add(TrendUtil.orderNum(tempNum));
                }
            }
        }
        return result;
    }

    public static String randomGetPredictNumFromOptions(String[] options, int count, Long randomSeed) {
        if (options == null || options.length < count || count <= 0) {
            return null;
        }
        //将预测备选号码打乱
        List<String> allKillNums = Arrays.asList(options);
        Collections.shuffle(allKillNums, new Random(randomSeed));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(allKillNums.get(i)).append(CommonConstant.SPACE_SPLIT_STR);
        }
        return sb.toString().trim();
    }

    public static String extractOnePhaseHotColdModel(List<Map<String, Object>> numColdHotData, Integer stateNum) {
        //1.获得某期内所有号的冷热值
        Map<String, Integer> numTimesMap = new HashMap<>();
        for (Map<String, Object> numInfo : numColdHotData) {
            String codeNum = numInfo.get("codeNum").toString();
            Integer showTimes = (Integer) numInfo.get("period" + stateNum);
            numTimesMap.put(codeNum, showTimes);
        }
        //2排序
        List<Map.Entry<String, Integer>> num30Times = numTimesMap.entrySet().stream().sorted(Map.Entry.<String,
                Integer>comparingByValue().reversed()).collect(Collectors.toList());

        //3.将冷热号拼接
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> temp : num30Times) {
            sb.append(temp.getKey()).append(CommonConstant.SPACE_SPLIT_STR);
        }
        return sb.toString().trim();
    }

    public static Map<String, String> packMorePredictNum(List<PredictRedBall> predictRedBalls) {
        //将更多预测数据组装程  periodId:predictNum treeMap放倒redis
        Map<String, String> predictNumbers = new TreeMap<>(Comparator.reverseOrder());

        for (PredictRedBall predictRedBall : predictRedBalls) {
            predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
        }
        return predictNumbers;
    }

    public static String getShowTextCal(String gameEn, String awardMsg, String msg) {
        Long gameId = GameCache.getGame(gameEn).getGameId();
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod period = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod.getPeriodId());
        // 如果当前时间大于该期次结束时间 小于开奖时间
        if (DateUtil.compareDate(period.getEndTime(), new Date()) && DateUtil.compareDate(new Date(), period
                .getAwardTime())) {
            return awardMsg;
        }
        return msg;
    }

    public static String generateColdStateKill3(PickNumPredict pickNumPredict, String periodId, String numModel,
                                                Integer endIndex) {
        String[] allColdHotNumArr = numModel.split(CommonConstant.SPACE_SPLIT_STR);
        int from = allColdHotNumArr.length - 1 - endIndex;
        //1.预测冷态杀三
        String[] coldArr = Arrays.copyOfRange(allColdHotNumArr, from, allColdHotNumArr.length - 1);
        return randomGetPredictNumFromOptions(coldArr, pickNumPredict.getNumCount(), Long.valueOf(periodId +
                pickNumPredict.getColdHotStateNum()));
    }


    public static String generateHotStateKill3(PickNumPredict pickNumPredict, String periodId, String numModel,
                                               Integer endIndex) {
        String[] allColdHotNumArr = numModel.split(CommonConstant.SPACE_SPLIT_STR);
        String[] hotArr = Arrays.copyOfRange(allColdHotNumArr, 0, endIndex);
        return randomGetPredictNumFromOptions(hotArr, pickNumPredict.getNumCount(), Long.valueOf(periodId +
                pickNumPredict.getColdHotStateNum()));
    }

    //本期开奖号码中为上期冷号的号码称为回补态 3.6需求
    public static String generateCallBackStateKill3(PickNumPredict pickNumPredict, long gameId, String periodId,
                                                    String lastPeriodNumModel, Integer endIndex) {
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
        //1.获取开奖号码
        String winRed = lastPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
        String[] lastNumArr = lastPeriodNumModel.split(CommonConstant.SPACE_SPLIT_STR);
        int from = lastNumArr.length - 1 - endIndex;
        //2.从开奖号提取回补号
        String[] lastColdArr = Arrays.copyOfRange(lastNumArr, from, lastNumArr.length - 1);
        List<String> callBacks = new ArrayList<>();
        for (String temp : lastColdArr) {
            if (winRed.contains(temp)) {
                callBacks.add(temp);
            }
        }
        //2.如果回补态号不够直接在上期热号中取
        int callBackSize = callBacks.size();
        if (callBackSize < pickNumPredict.getNumCount()) {
            String[] lastHotArr = Arrays.copyOfRange(lastNumArr, 0, endIndex);
            List<String> lastHots = Arrays.asList(lastHotArr);
            Collections.shuffle(lastHots, new Random(Long.valueOf(periodId + pickNumPredict.getColdHotStateNum())));
            for (int i = 0; i < pickNumPredict.getNumCount() - callBackSize; i++) {
                callBacks.add(lastHots.get(i));
            }
        }
        String[] callBackOptionArr = new String[callBacks.size()];
        for (int i = 0; i < callBacks.size(); i++) {
            callBackOptionArr[i] = callBacks.get(i);
        }
        return PredictUtil.randomGetPredictNumFromOptions(callBackOptionArr, pickNumPredict.getNumCount(), Long
                .valueOf(periodId + pickNumPredict.getColdHotStateNum()));
    }

    public static String getColdHotChartNameByNumType(long gameId, Integer numType) {
        if (numType == null) {
            return null;
        }
        Game game = GameCache.getGame(gameId);
        if (game.getGameEn().equals(GameConstant.SSQ)) {
            if (numType.equals(PredictConstant.PREDICT_NUM_TYPE_RED_BALL)) {
                return "RED_COLD_HOT";
            }
            return "BLUE_COLD_HOT";
        } else if (game.getGameEn().equals(GameConstant.DLT)) {
            if (numType.equals(PredictConstant.PREDICT_NUM_TYPE_RED_BALL)) {
                return "FRONT_COLD_HOT";
            }
            return "BACK_COLD_HOT";
        }
        return "";
    }

    public static Integer getProgramTypeByFirstBuy(Integer firstBuy, Integer programType) {
        if (firstBuy.equals(PredictConstant.USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_NO)) {
            return programType;
        }
        if (programType.equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED)) {
            return PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED_FIRST;
        } else if (programType.equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE)) {
            return PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST;
        }
        return programType;
    }

    public static String getKillStateRefundMissionId(long gameId, int periodId, Integer programType, Integer
            predictType, Long userId, String partMoney) {
        return gameId + ":" + periodId + ":" + programType + ":" + predictType + ":" + userId + ":" + partMoney;
    }

    public static String getPartKillStateRefundMissionId(long gameId, int periodId, Integer programType, Integer
            predictType, Long userId) {
        return gameId + ":" + periodId + ":" + programType + ":" + predictType + ":" + userId + ":";
    }

    public static String getPartKillStateRefundMissionId(long gameId, Integer periodId, Integer programType, Integer
            predictType) {
        return gameId + ":" + periodId + ":" + programType + ":" + predictType + ":";
    }
}
