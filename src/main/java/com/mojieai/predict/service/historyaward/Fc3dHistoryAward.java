package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.TrendDao;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Fc3dHistoryAward extends RedBlueGameHistoryAward {

    @Autowired
    private RedisService redisService;
    @Autowired
    private TrendDao trendDao;

    @Override
    protected int getTotalBlueCount() {
        return 0;
    }

    @Override
    protected int getBetRedBallNum() {
        return 3;
    }

    @Override
    protected int getBetBlueBallNum() {
        return 0;
    }

    @Override
    protected int[][] getLevelInfos() {
        //每行为奖级、百位球数 十位球  个位球
        int[][] level = {{1, 1, 1, 1}};
        return level;
    }

    @Override
    protected boolean noBingGo(int matchRedNumber, int matchBlueNumber) {
        return false;
    }

    @Override
    protected Game getGame() {
        return GameCache.getGame(GameConstant.FC3D);
    }

    @Override
    protected int getHistoryAwardDetailDisplayNum() {
        return IniCache.getIniIntValue(IniConstant.HISTORY_AWARD_DISPLAY_NUM + GameConstant.FC3D, 5);
    }

    @Override
    public Map<String, HashMap> buildLastNumberBehave(String gameEn) {
        return refreshNumberLastBehave(gameEn, null);
    }

    @Override
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
        String hundredColdHotKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastOpenPeriod.getPeriodId(),
                RedisConstant.Hot_KEY_MAP.get(gameEn + RedisConstant.HUNDRED_AFTER), null);
        Map<String, HashMap> tempMap = saveBehaveMap(hundredColdHotKey, RedisConstant.HUNDRED_PRE, gameEn, "百位");
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
        String tenColdHotKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastOpenPeriod.getPeriodId(),
                RedisConstant.Hot_KEY_MAP.get(gameEn + RedisConstant.TEN_AFTER), null);
        resultMap.putAll(saveBehaveMap(tenColdHotKey, RedisConstant.TEN_PRE, gameEn, "十位"));
        String oneColdHotKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastOpenPeriod.getPeriodId(),
                RedisConstant.Hot_KEY_MAP.get(gameEn + RedisConstant.ONE_AFTER), null);
        resultMap.putAll(saveBehaveMap(oneColdHotKey, RedisConstant.ONE_PRE, gameEn, "个位"));


        redisService.kryoHmset(behaveKey, resultMap);
        redisService.expire(behaveKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil
                .getIntervalMinutes(DateUtil.getCurrentTimestamp(), 600)));
        // 分析结果不包含第2017089期（今天21:15开奖）
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod
                .getPeriodId());
        redisService.kryoSetEx(RedisConstant.getShowTitleKey(gameEn), (int) DateUtil.getDiffSeconds(DateUtil
                        .getCurrentTimestamp(), DateUtil.getIntervalMinutes(DateUtil.getCurrentTimestamp(), 600)),
                getShowTitleByPeriod(nextPeriod, game));
        return resultMap;
    }

    public Map<String, HashMap> saveBehaveMap(String key, String numberKey, String gameEn, String positon) {
        List<Map<String, String>> listMap = redisService.kryoGet(key, ArrayList.class);
        Map<String, HashMap> stringMapMap = new HashMap<>();
        if (null != listMap) {
            HashMap positonMap = new HashMap();
            positonMap.put("name", positon);
            HashMap<String, Object> behave = new HashMap<>();
            for (Map<String, String> map : listMap) {
                HashMap<String, String> numberLastBehaveMap = new HashMap();
                numberLastBehaveMap.put("number", Integer.valueOf(map.get("codeNum").toString()) + "");//号码
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
                behave.put(numberKey + map.get("codeNum"), numberLastBehaveMap);
            }
            stringMapMap.put("positon" + numberKey, positonMap);
            stringMapMap.put("behave" + numberKey, behave);
        }
        return stringMapMap;
    }

    @Override
    public List getLastNumberBehave(String lotteryNumber, Integer ifValidType, String gameEn) {
        List resultMapList = new ArrayList<>();
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

            if (lotteryNumberArr.length == 3) {
                //百位
                String[] hundred = lotteryNumberArr[0].split(CommonConstant.COMMA_SPLIT_STR);
                Map<String, Object> hundredMap = getBehaveFromRedis(lastBehaveMap, hundred, RedisConstant.HUNDRED_PRE);
                resultMapList.add(hundredMap);

                String[] ten = lotteryNumberArr[1].split(CommonConstant.COMMA_SPLIT_STR);
                Map<String, Object> tenMap = getBehaveFromRedis(lastBehaveMap, ten, RedisConstant.TEN_PRE);
                resultMapList.add(tenMap);

                String[] one = lotteryNumberArr[2].split(CommonConstant.COMMA_SPLIT_STR);
                Map<String, Object> oneMap = getBehaveFromRedis(lastBehaveMap, one, RedisConstant.ONE_PRE);
                resultMapList.add(oneMap);
            }
        } catch (Exception e) {
            throw new BusinessException("lastNumberBehave error " + e.getMessage());
        }
        return resultMapList;
    }

    private Map<String, Object> getBehaveFromRedis(Map<String, HashMap> lastBehaveMap, String[] hundred, String key) {
        Map<String, Object> hundredMap = new HashMap();
        List behave = new ArrayList();
        for (String num : hundred) {
            if (Integer.valueOf(num) < 10) {
                num = CommonUtil.getBallStr(Integer.valueOf(num));
            }
            behave.add(lastBehaveMap.get("behave" + key).get(key + num));
        }
        hundredMap.put("positon", lastBehaveMap.get("positon" + key).get("name"));
        hundredMap.put("behave", behave);
        return hundredMap;
    }

    @Override
    public Map<String, Object> getHistoryAwardContent(String gameEn, String lotteryNumber, int limit) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, String>> content = new ArrayList<>();
        Map<String, Object[]> records = getAllAwardRecords(lotteryNumber, 0);
        String desc = "";
        boolean isMore = false;
        for (String key : records.keySet()) {
            if (Integer.valueOf(key) > 0) {
                Integer awardNum = (Integer) records.get(key)[0];
                if (awardNum > 0) {
                    desc = "此号码在历史中出现过<font color=\"#FF5050\">" + awardNum + "</font>次";
                }
                List<Map<String, String>> awardInfos = (List<Map<String, String>>) records.get(key)[1];
                if (awardInfos != null) {
                    int count = 0;
                    for (Map<String, String> awardInfo : awardInfos) {
                        if (limit > 0 && count >= limit) {
                            break;
                        }
                        Map temp = new HashMap();
                        String winNum = awardInfo.get("awardNumber");
                        if (StringUtils.isNotBlank(winNum)) {
                            winNum = winNum.replaceAll(CommonConstant.SPACE_SPLIT_STR, CommonConstant
                                    .COMMA_SPLIT_STR_CN);
                        }
                        temp.put("period", "第" + awardInfo.get("period") + "期");
                        temp.put("awardNumber", winNum);
                        content.add(temp);
                        count++;
                    }
                    if (limit > 0 && awardInfos.size() > limit) {
                        isMore = true;
                    }
                }
            }
        }
        res.put("isMore", isMore);
        res.put("awardNum", desc);
        res.put("content", content);
        return res;
    }

    @Override
    public Map<String, Object[]> getAllAwardRecords(String lotteryNumber, int periodNum) {
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
        String[] numberArray = lotteryNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SPACE_SPLIT_STR);
        long[][] betNumberInfos = new long[numberArray.length][6];
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

    @Override
    public List<Map<String, Object>> getNumHistoryOmit(String lotteryNumber, String gameEn) {
        Game game = GameCache.getGame(gameEn);
        Map<String, HashMap> historyOmitMap = redisService.kryoHgetAll(RedisConstant.getHistoryOmit(game.getGameEn()),
                String.class, HashMap.class);
        if (null == historyOmitMap) {
            HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);
            historyOmitMap = historyAward.buildHistoryOmit(game);
        }
        List<Map<String, Object>> hundredList = new ArrayList<>();
        if (historyOmitMap != null) {
            String[] numArr = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
            String[] hundred = numArr[0].split(CommonConstant.COMMA_SPLIT_STR);
            String[] ten = numArr[1].split(CommonConstant.COMMA_SPLIT_STR);
            String[] one = numArr[2].split(CommonConstant.COMMA_SPLIT_STR);
            Map<String, Object> hundredHistory = getHistoryOmitAndContinue(historyOmitMap, hundred, RedisConstant
                    .HUNDRED_PRE);
            Map<String, Object> tenHistory = getHistoryOmitAndContinue(historyOmitMap, ten, RedisConstant.TEN_PRE);
            Map<String, Object> oneHistory = getHistoryOmitAndContinue(historyOmitMap, one, RedisConstant.ONE_PRE);

            hundredList.add(hundredHistory);
            hundredList.add(tenHistory);
            hundredList.add(oneHistory);
        }
        return hundredList;
    }

    private Map<String, Object> getHistoryOmitAndContinue(Map<String, HashMap> allHistoryOmitMap, String[] placeNum,
                                                          String prefix) {
        Map<String, Object> historyOmitMapRes = new HashMap<>();
        HashMap somePlaceOmit = allHistoryOmitMap.get(prefix + "historyOmit");
        List<Map<String, String>> historyOmits = new ArrayList<>();
        for (String num : placeNum) {
            Map<String, String> tempHistory = new HashMap<>();
            tempHistory.put("currentContinue", somePlaceOmit.get("currtContinue_" + num).toString());
            tempHistory.put("maxContinue", somePlaceOmit.get("maxContinue_" + num).toString());
            tempHistory.put("currentOmit", somePlaceOmit.get("currtOmit_" + num).toString());
            tempHistory.put("maxOmit", somePlaceOmit.get("maxOmit_" + num).toString());
            tempHistory.put("number", num);
            historyOmits.add(tempHistory);
        }

        historyOmitMapRes.put("positon", getPositionName(prefix));
        historyOmitMapRes.put("historyOmit", historyOmits);
        return historyOmitMapRes;
    }

    private String getPositionName(String prefix) {
        if (prefix.equals(RedisConstant.HUNDRED_PRE)) {
            return "百位";
        } else if (prefix.equals(RedisConstant.TEN_PRE)) {
            return "十位";
        } else if (prefix.equals(RedisConstant.ONE_PRE)) {
            return "个位";
        }
        return "";
    }

    @Override
    public Map<String, HashMap> buildHistoryOmit(Game game) {
        Map<String, HashMap> res = new HashMap<>();
        String historyOmitKey = RedisConstant.getHistoryOmit(game.getGameEn());

        List<GamePeriod> allOpenPeriod = PeriodRedis.getLastAllOpenPeriodsByGameId(game.getGameId());
        HashMap historyOmit100 = new HashMap();
        HashMap historyOmit10 = new HashMap();
        HashMap historyOmit1 = new HashMap();
        if (allOpenPeriod == null || allOpenPeriod.size() <= 1) {
            log.error("buildHistoryOmit error allOpenPeriod is null");
            return null;
        }
        for (int i = allOpenPeriod.size() - 1; i >= 0; i--) {
            GamePeriod gamePeriod = allOpenPeriod.get(i);
            String winNum = gamePeriod.getWinningNumbers();
            String[] winNumArr = winNum.split(CommonConstant.SPACE_SPLIT_STR);
            historyOmit100 = analysisWinNum(Integer.valueOf(winNumArr[0]), historyOmit100);
            historyOmit10 = analysisWinNum(Integer.valueOf(winNumArr[1]), historyOmit10);
            historyOmit1 = analysisWinNum(Integer.valueOf(winNumArr[2]), historyOmit1);
        }
        res.put(RedisConstant.HUNDRED_PRE + "historyOmit", historyOmit100);
        res.put(RedisConstant.TEN_PRE + "historyOmit", historyOmit10);
        res.put(RedisConstant.ONE_PRE + "historyOmit", historyOmit1);

        redisService.kryoHmset(historyOmitKey, res);
        redisService.expire(historyOmitKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil
                .getIntervalMinutes(DateUtil.getCurrentTimestamp(), 600)));
        return res;
    }

    @Override
    public List<Map<String, String>> getNumberProperties(String lotteryNumber, String gameEn) {
        /* 多行*/
        List<Map<String, String>> rows = new ArrayList<>();

        String[] stakeNumberArr = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
        Integer[] primeArr = {1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
        List<Integer> primeList = new ArrayList<>();
        Collections.addAll(primeList, primeArr);
        int single = 0; //奇数
        int doubleInt = 0; //偶数
        int prime = 0;  //质数
        int composite = 0; //合数
        int big = 0; //大
        int small = 0; //小
        int way0 = 0; //0路
        int way1 = 0; //1路
        int way2 = 0; //2路
        int sumValue = 0; //和值
        int spanValue = 0;
        for (String eachNumber : stakeNumberArr) {
            // 能在遍历中完成的都在这里计算
            int intNumber = Integer.valueOf(eachNumber);
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

            if ((intNumber % 3) == 0) {
                way0++;
            } else if ((intNumber % 3) == 1) {
                way1++;
            } else if ((intNumber % 3) == 2) {
                way2++;
            }

            sumValue += intNumber;

            for (String secondEach : stakeNumberArr) {
                int secondEachInt = Integer.valueOf(secondEach);
                if (Math.abs(secondEachInt - intNumber) != 0) {
                    // 跨度
                    if (Math.abs(secondEachInt - intNumber) > spanValue) {
                        spanValue = Math.abs(secondEachInt - intNumber);
                    }
                }
            }
        }

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
        rowOne.put("3", "和值");
        rowOne.put("4", sumValue + "");
        rows.add(rowOne);

        Map<String, String> rowTwo = new LinkedHashMap<>();
        rowTwo.put("1", "质合比");
        rowTwo.put("2", prime + CommonConstant.COMMON_COLON_STR + composite);
        rowTwo.put("3", "跨度");
        rowTwo.put("4", String.valueOf(spanValue));
        rows.add(rowTwo);

        Map<String, String> rowThree = new LinkedHashMap<>();
        rowThree.put("1", "大小比");
        rowThree.put("2", big + CommonConstant.COMMON_COLON_STR + small);
        rowThree.put("3", "012路");
        rowThree.put("4", way0 + CommonConstant.COMMON_COLON_STR + way1 + CommonConstant.COMMON_COLON_STR + way2);
        rows.add(rowThree);
        return rows;
    }

    private HashMap analysisWinNum(int winBall, HashMap historyOmit100) {
        HashMap res = historyOmit100;
        for (int i = 0; i < 10; i++) {
            int currtContinue = res.get("currtContinue_" + i) == null ? 0 : (int) res.get("currtContinue_" + i);
            int maxContinue = res.get("maxContinue_" + i) == null ? 0 : (int) res.get("maxContinue_" + i);
            int currtOmit = res.get("currtOmit_" + i) == null ? 0 : (int) res.get("currtOmit_" + i);
            int maxOmit = res.get("maxOmit_" + i) == null ? 0 : (int) res.get("maxOmit_" + i);
            if (winBall == i) {
                currtContinue += 1;
                if (currtContinue > maxContinue) {
                    maxContinue = currtContinue;
                }
                currtOmit = 0;
            } else {
                currtContinue = 0;
                currtOmit += 1;
                if (currtOmit > maxOmit) {
                    maxOmit = currtOmit;
                }
            }
            res.put("currtContinue_" + i, currtContinue);
            res.put("maxContinue_" + i, maxContinue);
            res.put("currtOmit_" + i, currtOmit);
            res.put("maxOmit_" + i, maxOmit);
        }
        return res;
    }

    private void calcOneHistoryAward(int countLevel, GamePeriod period, long[][] betNumberInfos, Map<String,
            Object[]> resultMap) {

        HistoryAwardDetail dto = generateHistoryAwardDto(period);
        //每个奖级中奖明细都是一个list，这些list保存在一个list数组里面，大小即为奖级数
        //奖级明细list每个元素都是一个数组，数组由期次号、时间、中奖号组成
        List<String[]>[] detailArray = new ArrayList[countLevel];
        long resultForOnePeriod = 0l; //某一期所选号码列表的中奖信息
        long hundredWinningNumber = dto.getWinningNumber() >> 20;
        long tenWinningNumber = dto.getWinningNumber() >> 10;
        long oneWinningNumber = dto.getWinningNumber() & ((1l << 10) - 1);

        for (long[] betNumberInfo : betNumberInfos) {
            //如果对于某一期，投注号码已经命中所有奖级，那么剩下的投注号码不用再计算了。因为计算的是中奖次数，不是注数
            if (calOneNumber(resultForOnePeriod) == countLevel) {
                break;
            }
            long historyAwardForOnePeriod = getHistoryAwardForOnePeriod(betNumberInfo, hundredWinningNumber,
                    tenWinningNumber, oneWinningNumber, getLevelInfos(), getBetRedBallNum(), getBetBlueBallNum());
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
                            .fc3dToLotteryNumber(dto.getWinningNumber(), getTotalBlueCount())});
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
                    list.addAll(detailList);
                    resultMap.get(key)[1] = list;
                }
            }
            resultForOnePeriod >>= 1;
            level++;
        }
    }

    private long getHistoryAwardForOnePeriod(long[] betNumberInfo, long redWinningNumber, long blueWinningNumber,
                                             long oneWinningNumber, int[][] levelInfos, int betRedBallNum, int
                                                     betBlueBallNum) {
        long hasWinningLevel = 0l; //某一期该注号码的中奖信息。有多注号码中了X等奖，也只算中了一次X等奖。最低位第0位表示是否中一等奖

        int matchHundredNumberMin = calOneNumber(redWinningNumber & betNumberInfo[0]);
        int matchTenNumberMin = calOneNumber(blueWinningNumber & betNumberInfo[2]);
        int matchOneNumberMax = calOneNumber(oneWinningNumber & betNumberInfo[4]);
        for (int[] level : levelInfos) {
            if (level[1] == matchHundredNumberMin && level[2] == matchTenNumberMin && level[3] == matchOneNumberMax) {
                hasWinningLevel |= (1l << (level[0] - 1));
            }
        }
        return hasWinningLevel;
    }

    private HistoryAwardDetail generateHistoryAwardDto(GamePeriod period) {
        String winningNumber = period.getWinningNumbers();
        int oneCount = 10;
        String[] numberArray = winningNumber.split(CommonConstant.SPACE_SPLIT_STR);
        String one = numberArray[2];
        String ten = numberArray[1];
        String hundred = numberArray[0];

        long bitWinningNumber = 0l; //位图法表示的中奖号码。每一bit分别对应一个红球或者蓝球，1表示中了，0表示每中
        bitWinningNumber |= (1l << Integer.parseInt(one)); //低位存个位
        bitWinningNumber |= (1l << (Integer.parseInt(ten) + oneCount)); //中位十位
        bitWinningNumber |= (1l << (Integer.parseInt(hundred) + 20)); //高位百位

        HistoryAwardDetail wrh = new HistoryAwardDetail();
        wrh.setAwardTime(DateUtil.formatTime(period.getAwardTime(), DateUtil.DEFAULT_DATE_FORMAT));
        wrh.setPeriodId(period.getPeriodId());
        wrh.setWinningNumber(bitWinningNumber);
        return wrh;
    }

    /**
     * 将投注号码解析成拖的位图形式
     */
    protected long[] parseLotteryNumber(String stakeNumber) {
        String[] hundredTenAndOne = stakeNumber.split(":");

        String hundred = hundredTenAndOne[0].trim();
        String ten = hundredTenAndOne[1].trim();
        String one = hundredTenAndOne[2].trim();

        long bitBet100NumberTuo = 0l;
        long bitBet10NumberTuo = 0l;
        long bitBet1NumberTuo = 0l;

        bitBet100NumberTuo = generateByBalls(hundred.split(CommonConstant.COMMA_SPLIT_STR)); //百位不是胆拖模式
        bitBet10NumberTuo = generateByBalls(ten.split(CommonConstant.COMMA_SPLIT_STR)); //百位不是胆拖模式
        bitBet1NumberTuo = generateByBalls(one.split(CommonConstant.COMMA_SPLIT_STR)); //百位不是胆拖模式

        long[] result =
                {bitBet100NumberTuo, calOneNumber(bitBet100NumberTuo), bitBet10NumberTuo, calOneNumber
                        (bitBet10NumberTuo), bitBet1NumberTuo, calOneNumber(bitBet1NumberTuo)};
        return result;
    }

    /**
     * 号码位图法表示
     */
    protected long generateByBalls(String[] balls) {
        long result = 0l;
        for (String ball : balls) {
            result |= (1l << Integer.parseInt(ball));
        }
        return result;
    }

}
