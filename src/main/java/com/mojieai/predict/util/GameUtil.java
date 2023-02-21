package com.mojieai.predict.util;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by bowu on 2017/7/13.
 */
public class GameUtil {
    protected static Logger log = LogConstant.commonLog;

    public static boolean checkDanshiFushiIfValid(String lotteryNumber, Pattern redBallPattern, Pattern
            blueBallPattern) {
        try {
            String[] redAndBlue = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
            if (!(redBallPattern.matcher(redAndBlue[0]).find())) {
                //log.error("[校验投注号码格式]单复式红球格式非法" + lotteryNumber);
                return false;
            }
            if (!(blueBallPattern.matcher(redAndBlue[1]).find())) {
                //log.error("[校验投注号码格式]单复式蓝球格式非法" + lotteryNumber);
                return false;
            }
            String[] redBalls = redAndBlue[0].split(CommonConstant.SPACE_SPLIT_STR);
            String[] blueBalls = redAndBlue[1].split(CommonConstant.SPACE_SPLIT_STR);
            Arrays.sort(redBalls);
            Arrays.sort(blueBalls);
            if (checkDuplicate(redBalls) || checkDuplicate(blueBalls)) {
                log.error("[校验投注号码格式]stakeNumber is not valid! duplicate error!" + lotteryNumber);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[校验投注号码格式_单复式]异常" + lotteryNumber, e);
            return false;
        }
    }

    public static boolean checkRedBallValid(String lotteryNumber, Pattern redBallPattern) {
        try {
            String[] redAndBlue = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
            String singleBalls = redAndBlue[0];
            if (StringUtils.isBlank(redAndBlue[0])) {
                singleBalls = redAndBlue[1];
            }
            if (!(redBallPattern.matcher(singleBalls).find())) {
                //log.error("[校验投注号码格式]单复式红球格式非法" + lotteryNumber);
                return false;
            }
            String[] redBalls = singleBalls.split(CommonConstant.SPACE_SPLIT_STR);
            Arrays.sort(redBalls);
            if (checkDuplicate(redBalls)) {
                log.error("[校验投注号码格式]stakeNumber is not valid! duplicate error!" + lotteryNumber);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[校验投注号码格式_单复式]异常" + e.getMessage(), e);
            return false;
        }
    }

    public static boolean checkDuplicate(String[] balls) {
        for (int i = 1; i < balls.length; i++) {
            if (balls[i].equalsIgnoreCase(balls[i - 1])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将long型位图表示的投注号码转换成字符串形式
     */
    public static String redBlueToLotteryNumber(long src, int blueCount) {
        long bitRedNumber = src >> blueCount;
        long bitBlueNumber = src & ((1l << blueCount) - 1);
        return longToStakeNumber(bitRedNumber, " ") + ":" + longToStakeNumber(bitBlueNumber, " ");
    }

    public static String fc3dToLotteryNumber(long src, int blueCount) {
        long bitHundredNumber = src >> 20;
        long bitTenNumber = (src >> 10) & ((1l << 10) - 1);
        long bitOneNumber = src & ((1l << 10) - 1);
        return longToStakeNumber1(bitHundredNumber, " ") + " " + longToStakeNumber1(bitTenNumber, " ") + " " +
                longToStakeNumber1(bitOneNumber, " ");
    }

    public static String longToStakeNumber1(long src, String splitChar) {
        StringBuffer resultBuf = new StringBuffer();
        int count = 0;
        while (src != 0) {
            if ((src & 1l) == 1) //看最低位是否为1
            {
                if (count < 9) {
                    resultBuf.append(String.valueOf(count) + splitChar);
                } else {
                    resultBuf.append(String.valueOf(count) + splitChar);
                }
            }
            count++;
            src = src >> 1;
        }
        String result = resultBuf.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * long型位图表示的开奖号码转换成字符串表示的开奖号码
     */
    public static String longToStakeNumber(long src, String splitChar) {
        StringBuffer resultBuf = new StringBuffer();
        int count = 0;
        while (src != 0) {
            if ((src & 1l) == 1) //看最低位是否为1
            {
                if (count < 9) {
                    resultBuf.append("0" + String.valueOf(count + 1) + splitChar);
                } else {
                    resultBuf.append(String.valueOf(count + 1) + splitChar);
                }
            }
            count++;
            src = src >> 1;
        }
        String result = resultBuf.toString();
        return result.substring(0, result.length() - 1);
    }

    // 将投注号码，分为胆码和拖码两部分
    public static String[][] dantuo(String stakeNumber, int max) {
        String[][] result = new String[2][];
        String[] danList = {};
        String[] tuoList;
        if (stakeNumber.indexOf('(') == 0) {
            int p = stakeNumber.indexOf(')');
            if (p < 0) {
                return null;
            }
            danList = stakeNumber.substring(1, p).split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .SPACE_SPLIT_STR);
            tuoList = stakeNumber.substring(p + 1).split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .SPACE_SPLIT_STR);
        } else {
            tuoList = stakeNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SPACE_SPLIT_STR);
        }
        if (danList.length >= max || danList.length + tuoList.length < max) {
            return null;
        }
        if (danList.length > 0 && danList.length + tuoList.length < max) {
            return null;
        }
        result[0] = danList;
        result[1] = tuoList;
        if (checkDuplicate(result)) {
            return null;
        }
        return result;
    }

    public static boolean checkDuplicate(String[][] balls) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < balls.length; i++) {
            for (int j = 0; j < balls[i].length; j++) {
                if (map.get(balls[i][j]) != null) {
                    return true;
                }
                map.put(balls[i][j], balls[i][j]);
            }
        }
        return false;
    }

    public static BigDecimal calcBigAwardBonus(int bigAwardLevel, String ratioStr, BigDecimal sale, List<AwardInfo>
            awardInfos) {
        BigDecimal fixBonus = BigDecimal.ZERO;
        for (AwardInfo info : awardInfos) {
            if (Integer.parseInt(info.getAwardLevel()) > bigAwardLevel) {
                fixBonus = fixBonus.add(info.getBonus().multiply(new BigDecimal(info.getAwardCount())));
            }
        }
        BigDecimal bigAwardBonus = sale.multiply(new BigDecimal(ratioStr)).subtract(fixBonus);
        bigAwardBonus = bigAwardBonus.compareTo(BigDecimal.ZERO) > 0 ? bigAwardBonus : BigDecimal.ZERO;
        return bigAwardBonus;
    }

    //isContain为false时pool可以为null
    public static BigDecimal calcBonusAndPool(boolean isContain, String ratioStr, BigDecimal bigAwardBonus, BigDecimal
            pool) {
        BigDecimal bonus = bigAwardBonus.multiply(new BigDecimal(ratioStr));
        if (isContain) {
            bonus = bonus.add(pool);
        }
        bonus = bonus.compareTo(CommonConstant.BONUS_MAX_500_LIMIT) > 0 ? CommonConstant.BONUS_MAX_500_LIMIT : bonus
                .setScale(0, BigDecimal.ROUND_DOWN);
        return bonus;
    }

    public static BigDecimal calcBonusContainPool(BigDecimal pool, BigDecimal bigAwardBonus, BigDecimal[] poolArray,
                                                  List<String[]> ratioArrayList) {
        BigDecimal bonus = BigDecimal.ZERO;
        int index = 0;
        for (int i = 0; i < poolArray.length; i++) {
            if (pool.compareTo(poolArray[i]) >= 0) {
                break;
            }
            index++;
        }
        for (int m = 0; m < ratioArrayList.get(index).length; m++) {
            bonus = bonus.add(calcBonusAndPool(m == 0, ratioArrayList.get(index)[m], bigAwardBonus, pool));
        }
        return bonus;
    }

    //解析开奖号码
    public static String parseCommonGameWinningNumber(Long gameId, String winningNumber) {
        String red = null;
        String blue = null;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.FC3D)) {
            return winningNumber;
        }
        switch (GameCache.getGame(gameId).getGameEn()) {
            case GameConstant.DLT:
                red = winningNumber.substring(0, 14);
                blue = winningNumber.substring(15);
                break;
            case GameConstant.SSQ:
                red = winningNumber.substring(0, 17);
                blue = winningNumber.substring(18);
                break;
        }
        return new StringBuffer().append(red).append(CommonConstant.COMMON_COLON_STR).append(blue).toString();
    }
}
