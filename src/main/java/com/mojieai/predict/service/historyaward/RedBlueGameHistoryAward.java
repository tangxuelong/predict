package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.entity.bo.HistoryAwardDetail;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.GameUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 针对红蓝双色球的彩种,例如双色球和大乐透。其他彩种是否支持未知。
 */
public abstract class RedBlueGameHistoryAward extends HistoryAward {

    @Autowired
    private RedisService redisService;

    /**
     * 获取蓝球号码数
     */
    protected abstract int getTotalBlueCount();

    /**
     * 获取需要投注的红球号码数
     */
    protected abstract int getBetRedBallNum();

    /**
     * 获取需要投注的蓝球球号码数
     */
    protected abstract int getBetBlueBallNum();

    /**
     * 获取中奖规则
     */
    protected abstract int[][] getLevelInfos();

    /**
     * 铁定不中奖判断
     */
    protected abstract boolean noBingGo(int matchRedNumber, int matchBlueNumber);

    protected abstract Game getGame();

    protected abstract int getHistoryAwardDetailDisplayNum();

    @Override
    public Map<String, Object[]> getAllAwardRecords(String lotteryNumber, int periodNum) {
        //历史上所选号码列表的中奖信息,key为奖级，Object[]第一项为奖级中奖次数，第二项为中奖明细list，list有长度限制
        //当key为-1时，回传当前计算期次信息
        Map<String, Object[]> resultMap = new HashMap<>();
        if (periodNum < 0) {
            log.info("[查看历史中奖]期次数小于0。periodNum:" + periodNum);
            return resultMap;
        }
        List<GamePeriod> periods = PeriodRedis.getLastAllOpenPeriodsByGameId(getGame().getGameId());
        String currentPeriodId;
        if (periods != null) {
            currentPeriodId = periods.get(0).getPeriodId();
        } else {
            log.error("periods is null. " + CommonUtil.mergeUnionKey(lotteryNumber, periodNum));
            return resultMap;
        }
        int awardSize = periods.size();
        if (periodNum == 0 || periodNum > awardSize) {
            periodNum = awardSize; //0或者超过开奖号码记录数，都认为是查询所有
        }

        int[][] levelInfos = getLevelInfos();
        int countLevel = levelInfos[levelInfos.length - 1][0]; //总共多少个奖级
        String[] numberArray = lotteryNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);
        long[][] betNumberInfos = new long[numberArray.length][8];
        for (int i = 0; i < numberArray.length; i++) {
            String betNumber = numberArray[i];
            betNumberInfos[i] = parseLotteryNumber(betNumber);
        }
        //某彩种的某一期
        for (int i = awardSize - periodNum; i <= awardSize - 1; i++) {
            calcOneHistoryAward(countLevel, periods.get(i), betNumberInfos, resultMap);
        }
        for (int i = 1; i <= countLevel; i++) {
            String key = String.valueOf(i);
            //如果不包含某个奖级，把相应奖级的中奖次数置为0
            if (!resultMap.containsKey(key)) {
                resultMap.put(key, new Object[]{0, null});
            }
        }
        resultMap.put(CommonConstant.STR_MINUS_ONE, new Object[]{currentPeriodId, null}); //把当前计算到哪期也一起返回
        return resultMap;
    }


    private void calcOneHistoryAward(int countLevel, GamePeriod period, long[][] betNumberInfos, Map<String,
            Object[]> resultMap) {
        HistoryAwardDetail dto = generateHistoryAwardDto(period);
        //每个奖级中奖明细都是一个list，这些list保存在一个list数组里面，大小即为奖级数
        //奖级明细list每个元素都是一个数组，数组由期次号、时间、中奖号组成
        List<String[]>[] detailArray = new ArrayList[countLevel];
        long resultForOnePeriod = 0l; //某一期所选号码列表的中奖信息
        long redWinningNumber = dto.getWinningNumber() >> getTotalBlueCount();
        long blueWinningNumber = dto.getWinningNumber() & ((1l << getTotalBlueCount()) - 1);
        for (long[] betNumberInfo : betNumberInfos) {
            //如果对于某一期，投注号码已经命中所有奖级，那么剩下的投注号码不用再计算了。因为计算的是中奖次数，不是注数
            if (calOneNumber(resultForOnePeriod) == countLevel) {
                break;
            }
            long historyAwardForOnePeriod = getHistoryAwardForOnePeriod(betNumberInfo, redWinningNumber,
                    blueWinningNumber, getLevelInfos(), getBetRedBallNum(), getBetBlueBallNum());
            //某一期某一行号码的中奖信息
            resultForOnePeriod |= historyAwardForOnePeriod;
        }

        int level = 0;
        while (resultForOnePeriod != 0) {
            //中了level+1等奖
            if ((resultForOnePeriod & 1l) == 1) {
                if (detailArray[level] == null) {
                    detailArray[level] = new ArrayList<>();
                }
                if (detailArray[level].size() < getHistoryAwardDetailDisplayNum()) {
                    detailArray[level].add(new String[]{dto.getPeriodId(), dto.getAwardTime(), GameUtil
                            .redBlueToLotteryNumber(dto.getWinningNumber(), getTotalBlueCount())});
                }
                String key = String.valueOf(level + 1);
                // 格式转换
                List<Object> detailList = new ArrayList<>();
                Map<String, String> detailMap = new HashMap<>();
                detailMap.put("period", detailArray[level].get(0)[0]);
                detailMap.put("awardTime", detailArray[level].get(0)[1]);
                detailMap.put("awardNumber", detailArray[level].get(0)[2]);
                detailList.add(detailMap);
                if (!resultMap.containsKey(key)) {
                    resultMap.put(key, new Object[]{1, detailList});
                } else {
                    resultMap.get(key)[0] = ((Integer) resultMap.get(key)[0]) + 1;
                    List<Object> list = (List<Object>) resultMap.get(key)[1];
                    if (list.size() < 10) {
                        list.addAll(detailList);
                        resultMap.get(key)[1] = list;
                    }
                }
            }
            resultForOnePeriod >>= 1;
            level++;
        }
    }

    private HistoryAwardDetail generateHistoryAwardDto(GamePeriod period) {
        String winningNumber = period.getWinningNumbers();
        int blueCount = getTotalBlueCount();
        String[] numberArray = winningNumber.split(CommonConstant.COMMON_COLON_STR);
        String[] blueNumberArray = numberArray[1].split(CommonConstant.SPACE_SPLIT_STR);
        String[] redNumberArray = numberArray[0].split(CommonConstant.SPACE_SPLIT_STR);
        long bitWinningNumber = 0l; //位图法表示的中奖号码。每一bit分别对应一个红球或者蓝球，1表示中了，0表示每中
        for (String blueNumber : blueNumberArray) {
            bitWinningNumber |= (1l << (Integer.parseInt(blueNumber) - 1)); //低位存蓝球
        }
        for (String redNumber : redNumberArray) {
            bitWinningNumber |= (1l << (Integer.parseInt(redNumber) + blueCount - 1)); //高位存红球
        }
        HistoryAwardDetail wrh = new HistoryAwardDetail();
        wrh.setAwardTime(DateUtil.formatTime(period.getAwardTime(), DateUtil.DEFAULT_DATE_FORMAT));
        wrh.setPeriodId(period.getPeriodId());
        wrh.setWinningNumber(bitWinningNumber);
        return wrh;
    }

    /**
     * 单式复式胆拖玩法,计算一行号码的历史中奖信息。计算中奖次数，而不是中奖注数
     *
     * @param betNumberInfo     一行号码,红胆红拖蓝胆蓝拖的位图形式
     * @param redWinningNumber  某个彩种某一期红球号码的开奖信息，位图表示
     * @param blueWinningNumber 某个彩种某一期蓝球号码的开奖信息，位图表示
     * @param levelInfos        奖级信息
     * @param betRedBallNum     红球数量
     * @param betBlueBallNum    蓝球数量
     * @return 某一期某一行号码的中奖等级，用位图法表示。最低位表示一等奖
     */
    protected long getHistoryAwardForOnePeriod(long[] betNumberInfo, long redWinningNumber, long blueWinningNumber,
                                               int[][] levelInfos, int betRedBallNum, int betBlueBallNum) {
        long hasWinningLevel = 0l; //某一期该注号码的中奖信息。有多注号码中了X等奖，也只算中了一次X等奖。最低位第0位表示是否中一等奖

        int numRedDan = (int) betNumberInfo[1];
        int numRedTuo = (int) betNumberInfo[3];
        int numBlueDan = (int) betNumberInfo[5];
        int numBlueTuo = (int) betNumberInfo[7];

        int overBetRedNumber = numRedDan + numRedTuo - betRedBallNum; //多选了几个红球
        int overBetBlueNumber = numBlueDan + numBlueTuo - betBlueBallNum; //多选了几个蓝球

        int matchRedNumberDan = calOneNumber(redWinningNumber & betNumberInfo[0]);
        int matchRedNumberTuo = calOneNumber(redWinningNumber & betNumberInfo[2]);

        int matchBlueNumberDan = calOneNumber(blueWinningNumber & betNumberInfo[4]);
        int matchBlueNumberTuo = calOneNumber(blueWinningNumber & betNumberInfo[6]);

        int matchRedNumberMax = matchRedNumberDan; //红球最多中多少个
        int matchBlueNumberMax = matchBlueNumberDan; //蓝球最多中多少个
        //胆码是必选的。还需要投多少个红拖与红拖命中数，取小的
        if (matchRedNumberTuo > betRedBallNum - numRedDan) {
            matchRedNumberMax += (betRedBallNum - numRedDan);
        } else {
            matchRedNumberMax += matchRedNumberTuo;
        }
        //胆码是必选的
        if (matchBlueNumberTuo > betBlueBallNum - numBlueDan) {
            matchBlueNumberMax += (betBlueBallNum - numBlueDan);
        } else {
            matchBlueNumberMax += matchBlueNumberTuo;
        }

        if (noBingGo(matchRedNumberMax, matchBlueNumberMax)) {
            return hasWinningLevel;
        }
        int redMinus = overBetRedNumber > (numRedTuo - matchRedNumberTuo) ? (numRedTuo - matchRedNumberTuo)
                //最多能踢掉多少个中的球，拿进来同等量的不中的球
                : overBetRedNumber; //matchRedNumber为最大匹配的红色号码数，matchRedNumber-redMinus为最小匹配红色号码数
        int blueMinus = overBetBlueNumber > (numBlueTuo - matchBlueNumberTuo) ? (numBlueTuo - matchBlueNumberTuo)
                : overBetBlueNumber; //matchRedNumber为最大匹配的蓝色号码数，matchRedNumber-blueMinus为最小匹配蓝色号码数

        int matchRedNumberMin = matchRedNumberMax - redMinus;
        int matchBlueNumberMin = matchBlueNumberMax - blueMinus;

        for (int[] level : levelInfos) {
            if (level[1] >= matchRedNumberMin && level[1] <= matchRedNumberMax && level[2] >= matchBlueNumberMin
                    && level[2] <= matchBlueNumberMax) {
                hasWinningLevel |= (1l << (level[0] - 1));
            }
        }

        return hasWinningLevel;
    }

    /**
     * 将投注号码解析成红胆红拖蓝胆蓝拖的位图形式
     */
    protected long[] parseLotteryNumber(String stakeNumber) {
        String[] redAndBlue = stakeNumber.split(":");

        String redBalls = "";
        if (redAndBlue[0].charAt(0) == '(') {
            redBalls = redAndBlue[0].substring(1).trim();
        } else {
            redBalls = redAndBlue[0].trim();
        }

        String blueBalls = "";
        if (redAndBlue[1].charAt(0) == '(') {
            blueBalls = redAndBlue[1].substring(1).trim();
        } else {
            blueBalls = redAndBlue[1].trim();
        }

        long bitBetRedNumberDan = 0l; // 胆码红球，每一bit分别对应一个红球胆码，1表示中了，0表示没中
        long bitBetRedNumberTuo = 0l; // 拖码红球，每一bit分别对应一个红球拖码，1表示中了，0表示没中

        if (redBalls.indexOf(")") >= 0) {
            bitBetRedNumberDan = generateByBalls((redBalls.split("\\)")[0].split(CommonConstant.SPACE_SPLIT_STR)));
            bitBetRedNumberTuo = generateByBalls((redBalls.split("\\)")[1].split(CommonConstant.SPACE_SPLIT_STR)));
        } else {
            bitBetRedNumberTuo = generateByBalls(redBalls.split(CommonConstant.SPACE_SPLIT_STR)); //红球不是胆拖模式
        }

        long bitBetBlueNumberDan = 0l; // 胆码蓝球，每一bit分别对应一个蓝球胆码，1表示中了，0表示没中
        long bitBetBlueNumberTuo = 0l; // 拖码蓝球，每一bit分别对应一个蓝球拖码，1表示中了，0表示没中

        if (blueBalls.indexOf(")") >= 0) {
            bitBetBlueNumberDan = generateByBalls(blueBalls.split("\\)")[0].split(CommonConstant.SPACE_SPLIT_STR));
            bitBetBlueNumberTuo = generateByBalls(blueBalls.split("\\)")[1].split(CommonConstant.SPACE_SPLIT_STR));
        } else {
            bitBetBlueNumberTuo = generateByBalls(blueBalls.split(CommonConstant.SPACE_SPLIT_STR)); //蓝球不是胆拖模式
        }
        long[] result =
                {bitBetRedNumberDan, calOneNumber(bitBetRedNumberDan), bitBetRedNumberTuo, calOneNumber
                        (bitBetRedNumberTuo),
                        bitBetBlueNumberDan, calOneNumber(bitBetBlueNumberDan), bitBetBlueNumberTuo,
                        calOneNumber(bitBetBlueNumberTuo)};
        return result;
    }

    /*
        * 奇偶比
        * 质合比
        * 大小比 1-16 17-33
        * 三区比 1-11 12-22 23-33
        * 012路比
        * 尾012路比
        * 尾大小比
        * 和值
        * 尾和
        * 和尾
        * 不同尾数
        * AC值
        *
        * */
    @Override
    public List<Map<String, String>> getNumberProperties(String lotteryNumber, String gameEn) {
        String stakeNumber = lotteryNumber.split(CommonConstant.COMMON_COLON_STR)[0];
        /* 多行*/
        List<Map<String, String>> rows = new ArrayList<>();

        String[] stakeNumberArr = stakeNumber.split(CommonConstant.SPACE_SPLIT_STR);
        Integer[] primeArr = {1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
        List<Integer> primeList = new ArrayList<>();
        Collections.addAll(primeList, primeArr);
        int single = 0; //奇数
        int doubleInt = 0; //偶数
        int prime = 0;  //质数
        int composite = 0; //合数
        int big = 0; //大
        int small = 0; //小
        int div1 = 0; //三区1
        int div2 = 0; //三区2
        int div3 = 0; //三区3
        int way0 = 0; //0路
        int way1 = 0; //1路
        int way2 = 0; //2路
        int lastWay0 = 0; //0路
        int lastWay1 = 0; //1路
        int lastWay2 = 0; //2路
        int lastBig = 0; //大
        int lastSmall = 0; //小
        int sumValue = 0; //和值
        int lastSumValue = 0; //尾和
        String sumValueLast = "0"; //和尾
        Set diffLast = new HashSet();//不同尾数
        Set ACSet = new TreeSet();//AC值
        int lianHao = 0;
        int spanValue = 0;
        for (String eachNumber : stakeNumberArr) {
            // 能在遍历中完成的都在这里计算
            int intNumber = Integer.valueOf(eachNumber);
            int lastIntNumber = Integer.valueOf(eachNumber.substring(eachNumber.length() - 1, eachNumber.length()));
            if ((intNumber % 2) == 0) {
                doubleInt++;
            } else {
                single++;
            }

            if (primeList.contains(intNumber)) {
                prime++;
            } else {
                composite++;
            }

            if (intNumber < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberMiddleLength()) {
                small++;
            } else {
                big++;
            }

            if (intNumber < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberDiv1Length()) {
                div1++;
            } else if (intNumber < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberDiv2Length()) {
                div2++;
            } else {
                div3++;
            }

            if ((intNumber % 3) == 0) {
                way0++;
            } else if ((intNumber % 3) == 1) {
                way1++;
            } else if ((intNumber % 3) == 2) {
                way2++;
            }

            if ((lastIntNumber % 3) == 0) {
                lastWay0++;
            } else if ((lastIntNumber % 3) == 1) {
                lastWay1++;
            } else if ((lastIntNumber % 3) == 2) {
                lastWay2++;
            }

            if (lastIntNumber < 5) {
                lastSmall++;
            } else {
                lastBig++;
            }

            sumValue += intNumber;

            lastSumValue += lastIntNumber;

            diffLast.add(lastIntNumber);

            for (String secondEach : stakeNumberArr) {
                int secondEachInt = Integer.valueOf(secondEach);
                if (Math.abs(secondEachInt - intNumber) != 0) {
                    // AC值
                    ACSet.add(Math.abs(secondEachInt - intNumber));
                    // 跨度
                    if (Math.abs(secondEachInt - intNumber) > spanValue) {
                        spanValue = Math.abs(secondEachInt - intNumber);
                    }
                }
                if ((secondEachInt - intNumber) == 1) {
                    lianHao++;
                }
            }
        }
        sumValueLast = String.valueOf(sumValue).substring(String.valueOf(sumValue).length() - 1, String.valueOf
                (sumValue).length());

        // 拼接表格
        Map<String, String> rowTh = new LinkedHashMap<>();
        rowTh.put("1", "属性");
        rowTh.put("2", "值");
        rowTh.put("3", "属性");
        rowTh.put("4", "值");
        rows.add(rowTh);

        Map<String, String> rowOne = new LinkedHashMap<>();
        rowOne.put("1", "奇偶比");
        rowOne.put("2", single + CommonConstant.COMMON_COLON_STR + doubleInt);
        rowOne.put("3", "AC值");
        rowOne.put("4", String.valueOf(ACSet.size() - (GameEnum.getGameEnumByEn(gameEn).getGameRedNumberBLueLength()
                - 1)));
        rows.add(rowOne);

        Map<String, String> rowTwo = new LinkedHashMap<>();
        rowTwo.put("1", "质合比");
        rowTwo.put("2", prime + CommonConstant.COMMON_COLON_STR + composite);
        rowTwo.put("3", "和值");
        rowTwo.put("4", String.valueOf(sumValue));
        rows.add(rowTwo);

        Map<String, String> rowThree = new LinkedHashMap<>();
        rowThree.put("1", "大小比");
        rowThree.put("2", big + CommonConstant.COMMON_COLON_STR + small);
        rowThree.put("3", "连号");
        rowThree.put("4", String.valueOf(lianHao));
        rows.add(rowThree);

        Map<String, String> rowFour = new LinkedHashMap<>();
        rowFour.put("1", "三区比");
        rowFour.put("2", div1 + CommonConstant.COMMON_COLON_STR + div2 + CommonConstant.COMMON_COLON_STR + div3);
        rowFour.put("3", "跨度");
        rowFour.put("4", String.valueOf(spanValue));
        rows.add(rowFour);

        Map<String, String> rowFive = new LinkedHashMap<>();
        rowFive.put("1", "012路比");
        rowFive.put("2", way0 + CommonConstant.COMMON_COLON_STR + way1 + CommonConstant.COMMON_COLON_STR + way2);
        rowFive.put("3", "尾和");
        rowFive.put("4", String.valueOf(lastSumValue));
        rows.add(rowFive);

        Map<String, String> rowSix = new LinkedHashMap<>();
        rowSix.put("1", "尾012路比");
        rowSix.put("2", lastWay0 + CommonConstant.COMMON_COLON_STR + lastWay1 + CommonConstant.COMMON_COLON_STR +
                lastWay2);
        rowSix.put("3", "和尾");
        rowSix.put("4", String.valueOf(sumValueLast));
        rows.add(rowSix);

        Map<String, String> rowSeven = new LinkedHashMap<>();
        rowSeven.put("1", "尾大小比");
        rowSeven.put("2", lastBig + CommonConstant.COMMON_COLON_STR + lastSmall);
        rowSeven.put("3", "不同尾数");
        rowSeven.put("4", String.valueOf(diffLast.size()));
        rows.add(rowSeven);

        return rows;
    }

    @Override
    public List<Map<String, String>> getLastNumberBehave(String lotteryNumber, Integer ifValidType, String gameEn) {
        List<Map<String, String>> resultMapList = new ArrayList<>();
        try {
            // 先去redis 如果没有 然后取 冷热缓存 如果还是没有就取上一期
            Game game = GameCache.getGame(gameEn);
            Map<String, HashMap> lastBehaveMap = redisService.kryoHgetAll(RedisConstant.getBehaveKey(game.getGameId()),
                    String.class, HashMap.class);
            if (null == lastBehaveMap) {
                HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);
                lastBehaveMap = historyAward.buildLastNumberBehave(gameEn);
            }
            String[] lotteryNumberArr = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);

            // 红球和篮球放在一个key 使用 Hgetall
            if (ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE || ifValidType == ResultConstant
                    .LOTTERY_NUMBER_TYPE_MULTIPLE || ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE_NOT) {
                for (String number : lotteryNumberArr[0].split(CommonConstant.SPACE_SPLIT_STR)) {
                    resultMapList.add(lastBehaveMap.get(RedisConstant.RED_PRE + number));
                }
                for (String number : lotteryNumberArr[1].split(CommonConstant.SPACE_SPLIT_STR)) {
                    resultMapList.add(lastBehaveMap.get(RedisConstant.BLUE_PRE + number));
                }
            }
            if (ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_ONLY_BLUE) {
                for (String number : lotteryNumberArr[1].split(CommonConstant.SPACE_SPLIT_STR)) {
                    resultMapList.add(lastBehaveMap.get(RedisConstant.BLUE_PRE + number));
                }
            }
            if (ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_ONLY_RED) {
                for (String number : lotteryNumberArr[0].split(CommonConstant.SPACE_SPLIT_STR)) {
                    resultMapList.add(lastBehaveMap.get(RedisConstant.RED_PRE + number));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("lastNumberBehave error " + e.getMessage());
        }
        return resultMapList;
    }
    /* 奇偶比*//*
    protected abstract Map<String, String> jiOu(String stakeNumber);

    *//* 质合比*//*
    protected abstract Map<String, String> zhiHe(String stakeNumber);

    *//* 大小比*//*
    protected abstract Map<String, String> bigSmall(String stakeNumber);

    *//* 三区比*//*
    protected abstract Map<String, String> threeDiv(String stakeNumber);

    *//* 012路比*//*
    protected abstract Map<String, String> ZOT(String stakeNumber);

    *//* 尾012路比*//*
    protected abstract Map<String, String> LastZOT(String stakeNumber);

    *//* 尾大小比*//*
    protected abstract Map<String, String> LastBigSmall(String stakeNumber);

    *//* AC值*//*
    protected abstract Map<String, String> AC(String stakeNumber);

    *//* 和值*//*
    protected abstract Map<String, String> sumValue(String stakeNumber);

    *//* 连号*//*
    protected abstract Map<String, String> lianHao(String stakeNumber);

    *//* 跨度*//*
    protected abstract Map<String, String> span(String stakeNumber);

    *//* 尾和*//*
    protected abstract Map<String, String> lastSum(String stakeNumber);

    *//* 和尾*//*
    protected abstract Map<String, String> sumLast(String stakeNumber);

    *//* 不同尾数*//*
    protected abstract Map<String, String> diffLast(String stakeNumber);*/

    @Override
    public String getShowText(GamePeriod period) {
        // 如果当前时间小于该期次的结束时间
        if (DateUtil.compareDate(new Date(), period.getEndTime())) {
            return "第" + period.getPeriodId() + "期 " + DateUtil.getTodayTomorrow(period.getEndTime()) + "（" + DateUtil
                    .getTargetWeek(period.getEndTime()) + "）" + DateUtil.formatDate(period.getAwardTime(), DateUtil
                    .DATE_FORMAT_HHMM) + "开奖";
        }
        // 如果当前时间大于该期次结束时间 小于开奖时间
        if (DateUtil.compareDate(period.getEndTime(), new Date()) && DateUtil.compareDate(new Date(), period
                .getAwardTime())) {
            return "第" + period.getPeriodId() + "期 " + "官方投注已经截止，遗漏值仅供参考";
        }
        // 如果当前时间大于开奖时间
        if (DateUtil.compareDate(period.getAwardTime(), new Date())) {
            return "第" + period.getPeriodId() + "期 " + "开奖中，遗漏值暂无数据";
        }
        return "第" + period.getPeriodId() + "期 " + DateUtil.getTodayTomorrow(period.getEndTime()) + "（" + DateUtil
                .getTargetWeek(period.getEndTime()) + "）" + DateUtil.formatDate(period.getAwardTime(), DateUtil
                .DATE_FORMAT_HHMM) + "开奖";
    }

    @Override
    public String getAwardTitle(String gameEn) {
        String showTitle = redisService.kryoGet(RedisConstant.getShowTitleKey(gameEn), String.class);
        return showTitle;
    }

    @Override
    public List<Integer> getOmitNumber(GamePeriod period, String key) {
        List<Integer> omitNumbers = new ArrayList<>();
        try {
            String chartKey = RedisConstant.getCurrentChartKey(period.getGameId(), period.getPeriodId(), key, 30);
            Map<String, Object> omitNumberMap = redisService.kryoGet(chartKey, HashMap.class);
            List<Map<String, Object>> periodList = (List<Map<String, Object>>) omitNumberMap.get(TrendConstant
                    .KEY_TREND_PERIOD);
            omitNumbers = (List<Integer>) periodList.get(periodList.size() - 1).get(TrendConstant.KEY_TREND_OMIT_NUM);

        } catch (Exception e) {
            log.error("getOmitNumber error ", e.getMessage());
            throw new BusinessException("getOmitNumber error " + e.getMessage());
        }
        return omitNumbers;
    }

    @Override
    public Map<String, HashMap> buildLastNumberBehave(String gameEn) {
        return refreshNumberLastBehave(gameEn, null);
    }

    /* 将号码近期表现对象刷新到缓存*/
    public Map<String, HashMap> refreshNumberLastBehave(String gameEn, GamePeriod period) {
        /* 号码，当前遗漏，50期出现，50期理论，100期出现，100期理论*/
        Game game = GameCache.getGame(gameEn);
        if (null == game) {
            log.error("gameEn error when refreshNumberLastBehave gameEn", gameEn);
            throw new BusinessException("gameEn error when refreshNumberLastBehave gameEn" + gameEn);
        }
        /* 获取当前期次信息*/

        GamePeriod lastOpenPeriod;
        Boolean prePeriodFlag = Boolean.FALSE;
        if (null == period) {
            lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        } else {
            prePeriodFlag = Boolean.TRUE;
            lastOpenPeriod = period;
        }
        if (null == lastOpenPeriod) {
            log.error("currentGamePeriod error when refreshNumberLastBehave gameEn", gameEn);
            throw new BusinessException("currentGamePeriod error when refreshNumberLastBehave gameEn" + gameEn);
        }
        String behaveKey = RedisConstant.getBehaveKey(game.getGameId());
        Map<String, HashMap> resultMap = new HashMap<>();
        /* 获取冷热的缓存信息 trendRedis:22017088*/
        String blueColdHotKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastOpenPeriod.getPeriodId(),
                RedisConstant.Hot_KEY_MAP.get(gameEn + RedisConstant.RED_AFTER), null);
        Map<String, HashMap> tempMap = saveBehaveMap(blueColdHotKey, RedisConstant.RED_PRE, gameEn);
        if (null == tempMap) {
            GamePeriod prePeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod
                    .getPeriodId());
            if (!prePeriodFlag) {
                return refreshNumberLastBehave(gameEn, prePeriod);
            } else {
                throw new BusinessException("get trendRedis two periods always null");
            }
        }
        resultMap.putAll(tempMap);
        /* 获取冷热的缓存信息 trendRedis:22017088*/
        String redColdHotKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastOpenPeriod.getPeriodId(),
                RedisConstant.Hot_KEY_MAP.get(gameEn + RedisConstant.BLUE_AFTER), null);
        resultMap.putAll(saveBehaveMap(redColdHotKey, RedisConstant.BLUE_PRE, gameEn));

        redisService.kryoHmset(behaveKey, resultMap);
        redisService.expire(behaveKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil
                .getIntervalMinutes(DateUtil.getCurrentTimestamp(), 600)));
        // 分析结果不包含第2017089期（今天21:15开奖）
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod
                .getPeriodId
                ());
        redisService.kryoSetEx(RedisConstant.getShowTitleKey(gameEn), (int) DateUtil.getDiffSeconds(DateUtil
                .getCurrentTimestamp(), DateUtil
                .getIntervalMinutes(DateUtil.getCurrentTimestamp(), 600)), getShowTitleByPeriod(nextPeriod, game));
        return resultMap;
    }

    public String getShowTitleByPeriod(GamePeriod period, Game game) {
        return "分析结果不包含第" + period.getPeriodId() + "期（" + DateUtil.getTodayTomorrow(period.getEndTime())
                + DateUtil.formatDate(period.getAwardTime(), DateUtil.FMT_DATE_HHMM) + "开奖）";
    }

    public Map<String, HashMap> saveBehaveMap(String key, String numberKey, String gameEn) {
        List<Map<String, String>> listMap = redisService.kryoGet(key, ArrayList.class);
        Map<String, HashMap> stringMapMap = new HashMap<>();
        if (null != listMap) {
            for (Map<String, String> map : listMap) {
                HashMap<String, String> numberLastBehaveMap = new HashMap();
                numberLastBehaveMap.put("number", map.get("codeNum"));//号码
                numberLastBehaveMap.put("omitNum", map.get("omitNum"));//遗漏
                numberLastBehaveMap.put("period50", map.get("period50"));//五十期出现
                numberLastBehaveMap.put("period50theory", RedisConstant.THEORY_VALUE_MAP.get(numberKey + gameEn +
                        RedisConstant.GET_50_PERIOD));
                //五十期理论出现
                numberLastBehaveMap.put("period100", map.get("period100"));//100期出现
                numberLastBehaveMap.put("period100theory", RedisConstant.THEORY_VALUE_MAP.get(numberKey + gameEn +
                        RedisConstant.GET_100_PERIOD))
                ;//一百期理论出现
                numberLastBehaveMap.put("color", numberKey.substring(0, numberKey.length() - 1));//一百期理论出现
                stringMapMap.put(numberKey + map.get("codeNum"), numberLastBehaveMap);
            }
        }
        return stringMapMap;
    }
}
