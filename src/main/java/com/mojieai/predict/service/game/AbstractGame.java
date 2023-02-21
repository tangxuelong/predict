package com.mojieai.predict.service.game;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.bo.FestivalTimeRange;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.AwardInfoVo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.GameUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractGame {
    public static final BigDecimal PRICE_PER_STAKE = new BigDecimal(2);// 单注价格
    public static final int[] DLT_WEEK_RANGE = {2, 4, 7}; //大乐透
    public static final int[] SSQ_WEEK_RANGE = {1, 3, 5}; //双色球
    public static final int[] FC3D_WEEK_RANGE = {1, 2, 3, 4, 5, 6, 7}; //福彩3d
    public static final int COMMON_GAME_WINNING_NUMBER_LENGTH = 20;
    public static final int FC3D_GAME_WINNING_NUMBER_LENGTH = 5;

    public static final List<Integer> COMMON_GAME_PRIME_LIST = Arrays.asList(new Integer[]{1, 2, 3, 5, 7, 11, 13, 17,
            19, 23, 29, 31});//游戏的质数

    protected Logger log = LogConstant.commonLog;

    abstract public Game getGame();

    abstract public String getPeriodDateFormat();

    public String getWinningNumberPushUrl() {
        throw new AbstractMethodError("AbstractMethodError");
    }

    public String[] getAllRedNums(){
        throw new AbstractMethodError("getAllRedNums");
    }

    abstract protected int getWinningNumberLength();

    abstract protected String getWinningNumberRegexp();

    abstract public Integer getDailyPeriod();

    abstract public Timestamp getOfficialStartTime(GamePeriod gamePeriod);

    abstract public List<AwardInfo> getDefaultAwardInfoList();

    abstract public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period);

    public int[] analyseBidAwardLevels(int redDan, int redTuo, int blueDan, int blueTuo, int bingoRedDan, int
            bingoRedTuo, int bingoBlueDan, int bingoBlueTuo, String periodId) {
        return null;
    }

    public List<String> getGameAwardCondition() {
        return null;
    }

    public List<Object> awardCondition() {
        return null;
    }

    public String getAwardDesc() {
        return null;
    }

    public List<AwardInfo> getDefaultAwardInfoList(GamePeriod period) {
        return getDefaultAwardInfoList();
    }

    public List<AwardInfo> getAwardAmountList() {
        return null;
    }

    public Set<String> getDefaultConsecutiveNumbers() {
        return null;
    }

    public static int floatAwardLevelCount(String gameEn) {
        if (gameEn.equals(GameConstant.DLT)) {
            return 3;
        } else if (gameEn.equals(GameConstant.SSQ)) {
            return 2;
        } else {
            return 0;
        }
    }

    /*最牛杀号*/
    public Boolean judgeIsBestKillNum(Integer maxKillNum, Integer userKillNum, Integer userHitNum) {
        Boolean res = Boolean.FALSE;
        String bestKillNumStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.BEST_KILL_NUM_PRE + getGame()
                .getGameEn());
        if (StringUtils.isBlank(bestKillNumStr)) {
            return res;
        }
        Map bestKillNum = JSONObject.parseObject(bestKillNumStr, HashMap.class);
        if (bestKillNum.containsKey("maxKill_" + maxKillNum)) {
            Map bestKillCondition = (Map) bestKillNum.get("maxKill_" + maxKillNum);
            String conditionKey = userKillNum + CommonConstant.COMMON_SPLIT_STR + userHitNum;
            if (bestKillCondition.containsKey(conditionKey) && Integer.valueOf(bestKillCondition.get(conditionKey)
                    .toString()).equals(1)) {
                return true;
            }
        }
        return res;
    }

    //key为奖级，Object[]一等奖有两项，其他奖级只有一项
    //第一项为对应奖级占有奖池高奖级奖金，类型为List<String[]>；第二项为划分奖池等级，类型为BigDecimal[]
    public Map<String, Object[]> getAwardLevelRatioMap() {
        return new HashMap<>();
    }

    public BigDecimal calcAwardLevelBonus(BigDecimal pool, BigDecimal sale, AwardInfo awardInfo, List<AwardInfo>
            awardInfos) {
        if (getGame().getGameEn().equals(GameConstant.DLT) || getGame().getGameEn().equals(GameConstant.SSQ)) {
            Map<String, Object[]> awardLevelRatioMap = getAwardLevelRatioMap();
            BigDecimal bigAwardBonus = GameUtil.calcBigAwardBonus(awardLevelRatioMap.keySet().size(), "0.49", sale,
                    awardInfos);
            BigDecimal bonus;
            Object[] info = awardLevelRatioMap.get(awardInfo.getAwardLevel());
            List<String[]> ratioList = (List<String[]>) info[0];
            if (info.length == 1) {
                bonus = GameUtil.calcBonusAndPool(false, ratioList.get(0)[0], bigAwardBonus, pool);
            } else {
                BigDecimal[] poolLevel = (BigDecimal[]) info[1];
                bonus = GameUtil.calcBonusContainPool(pool, bigAwardBonus, poolLevel, ratioList);
            }
            if (bonus == null || bonus.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("calcAwardLevelBonus bonus is error." + CommonUtil.mergeUnionKey(pool, sale, awardInfo,
                        awardInfos));
                return CommonConstant.BONUS_MAX_500_LIMIT;
            }
            return bonus;
        }
        return awardInfo.getBonus();
    }

    public boolean checkLotteryNumberIfValid(String lotteryNumber) {
        return true;
    }

    public Integer checkLotteryNumberTypeIfValid(String lotteryNumber) {
        return 0;
    }

    public BigDecimal processAwardInfo(int[] totalAward, GamePeriod period, List<AwardInfo> awardInfos) {
        if (awardInfos == null) {
            awardInfos = getDefaultAwardInfoList(period);
        }
        BigDecimal calculateAward = BigDecimal.ZERO;
        for (int i = 0; i < totalAward.length; i++) {
            if (totalAward[i] <= 0) {
                continue;
            }
            // 迭代所有的奖金信息
            for (AwardInfo awardInfo : awardInfos) {
                if (awardInfo.getAwardLevel().equals(String.valueOf(i + 1))) {
                    if (awardInfo.getBonus().compareTo(BigDecimal.ZERO) == 0) {//计算奖级奖金
                        BigDecimal bonus;//如果由于奖池和销量计算不出来，一等奖按照1000w，其他奖项按照500w计算
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(period.getRemark());
                            String pool = jsonObject.getString("pool").replaceAll(CommonConstant.COMMA_SPLIT_STR, "")
                                    .replaceAll("元", "");
                            String sale = jsonObject.getString("sale").replace(CommonConstant.COMMA_SPLIT_STR, "")
                                    .replaceAll("元", "");
                            BigDecimal poolNum = pool.contains("亿") ? new BigDecimal(pool.replaceAll("亿", ""))
                                    .multiply(CommonConstant.BONUS_10000_LIMIT) : new BigDecimal(pool);
                            BigDecimal saleNum = sale.contains("亿") ? new BigDecimal(sale.replaceAll("亿", ""))
                                    .multiply(CommonConstant.BONUS_10000_LIMIT) : new BigDecimal(sale);
                            bonus = calcAwardLevelBonus(poolNum, saleNum, awardInfo, awardInfos);
                        } catch (Exception e) {
                            log.error("bonus is error" + period, e);
                            bonus = (i + 1 == 1 ? CommonConstant.BONUS_MAX_1000_LIMIT : CommonConstant
                                    .BONUS_MAX_500_LIMIT);
                        }
                        awardInfo.setBonus(bonus);
                    }
                    calculateAward = calculateAward.add(awardInfo.getBonus().multiply(new BigDecimal(totalAward[i])));
                }
            }
        }
        return calculateAward;
    }

    /**
     * 根据给定期次，生成后续的totalPeriod数量的期次， 可能会跨年。
     *
     * @param fromPeriod
     * @return
     */
    public List<GamePeriod> getFuturePeriods(GamePeriod fromPeriod) {
        if (null == fromPeriod) {
            return null;
        }
        int totalDays;
        List<GamePeriod> resultList;
        Game game = GameCache.getGame(fromPeriod.getGameId());
        if (Objects.equals(game.getGameType(), Game.GAME_TYPE_HIGH_FREQUENCY)) {
            String dateStr = fromPeriod.getPeriodId().substring(0, getPeriodDateFormat().length());
            totalDays = 3;
            int[] intervalDays = new int[totalDays];//明天 后天 大后天与今天天数间隔
            String[] newDayStrArray = new String[totalDays];
            processFestivalDay(dateStr, intervalDays, newDayStrArray);
            List<GamePeriod> basicPeriodList = getBasicPeriodList(fromPeriod.getGameId(), dateStr);
            if (basicPeriodList.isEmpty()) {
                return null;
            }
            resultList = generatePrePeriods(fromPeriod.getGameId(), intervalDays, newDayStrArray,
                    basicPeriodList);
        } else if (Objects.equals(game.getGameType(), Game.GAME_TYPE_COMMON)) {
            totalDays = 365;
            int[] weekRange;
            if (Objects.equals(GameCache.getGame(fromPeriod.getGameId()).getGameEn(), GameConstant.DLT)) {
                //Calendar中getDayOfWeek的返回值1是周日，7是周六；
                weekRange = DLT_WEEK_RANGE;//大乐透
            } else if (Objects.equals(GameCache.getGame(fromPeriod.getGameId()).getGameEn(), GameConstant.SSQ)) {
                weekRange = SSQ_WEEK_RANGE;//双色球
            } else if (Objects.equals(GameCache.getGame(fromPeriod.getGameId()).getGameEn(), GameConstant.FC3D)) {
                weekRange = FC3D_WEEK_RANGE;
            } else {
                throw new BusinessException("please define the game week range");
            }
            resultList = generatePrePeriods(fromPeriod, totalDays, weekRange);
        } else {
            log.error("错误的彩种类型");
            return null;
        }

        return resultList;
    }

    private List<GamePeriod> generatePrePeriods(GamePeriod fromPeriod, int predictionDays, int[] weekRange) {
        ArrayList<GamePeriod> periods = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYYMMDD);
        SimpleDateFormat yearFormat = new SimpleDateFormat(getPeriodDateFormat());
        String dateFormatStr = format.format(fromPeriod.getEndTime());
        String currentYearStr = yearFormat.format(fromPeriod.getEndTime());
        String lastYearStr = currentYearStr;
        DecimalFormat decimalFormat = new DecimalFormat("000");
        GamePeriod lastGamePeriod = fromPeriod;
        //endTime 10 分钟之后开售下一期
        Integer periodIndex = Integer.parseInt(fromPeriod.getPeriodId().substring(fromPeriod.getPeriodId().length() -
                3)) + 1;
        for (int i = 1; i < predictionDays; i++) {
            int intervalDays = i;
            Timestamp newDay = DateUtil.getIntervalDateFormat(dateFormatStr, DateUtil.DATE_FORMAT_YYYYMMDD,
                    intervalDays);

            FestivalTimeRange timeRange = FestivalConstant.getFestivalDays(newDay);
            //如果处于春节期间，期次后延
            if (timeRange != null) {
                continue;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(newDay.getTime());
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (!ArrayUtils.contains(weekRange, dayOfWeek)) {
                continue;
            }
            currentYearStr = yearFormat.format(new Date(newDay.getTime()));
            if (currentYearStr.compareTo(lastYearStr) > 0) {
                //跨年了，期次的序号需要重置
                periodIndex = 1;
            }
            String newPeriodId = currentYearStr + decimalFormat.format(periodIndex);
            GamePeriod newPeriod = predictionCommonPeriod(fromPeriod, lastGamePeriod, newPeriodId, intervalDays);
            periods.add(newPeriod);
            lastYearStr = currentYearStr;
            periodIndex += 1;
            lastGamePeriod = newPeriod;
        }
        return periods;
    }

    private GamePeriod predictionCommonPeriod(GamePeriod fromPeriod, GamePeriod lastGamePeriod, String periodId, int
            intervalDays) {
        GamePeriod period = new GamePeriod(lastGamePeriod.getGameId());
        period.setPeriodId(periodId);
        period.setStartTime(lastGamePeriod.getEndTime());
        period.setEndTime(DateUtil.getIntervalDays(fromPeriod.getEndTime(), intervalDays));
        period.setAwardTime(DateUtil.getIntervalDays(fromPeriod.getAwardTime(), intervalDays));
        return period;
    }

    private void processFestivalDay(String dateStr, int[] intervalDays, String[] newDays) {
        //初始化数据
        for (int i = 0; i < intervalDays.length; i++) {
            intervalDays[i] = i + 1;
            Timestamp newDayTime = DateUtil.getIntervalDateFormat(dateStr, getPeriodDateFormat(), intervalDays[i]);
            newDays[i] = DateUtil.formatDate(newDayTime, getPeriodDateFormat());
            FestivalTimeRange festivalTimeRange = FestivalConstant.getFestivalDays(newDayTime);
            if (festivalTimeRange != null) {
                int days = festivalTimeRange.restDays();
                for (int j = i; j < intervalDays.length; j++) {
                    intervalDays[j] += days;
                    newDays[j] = DateUtil.formatDate(DateUtil.getIntervalDateFormat(dateStr, getPeriodDateFormat(),
                            intervalDays[j]), getPeriodDateFormat());
                }
            }
        }
    }

    private List<GamePeriod> generatePrePeriods(Long gameId, int[] intervalDays, String[] nextDayArray, List<GamePeriod>
            basicPeriodList) {
        List<GamePeriod> resultList = new ArrayList<>();
        for (GamePeriod period : basicPeriodList) {
            for (int i = 0; i < intervalDays.length; i++) {
                GamePeriod prePeriod = predictionPeriod(period, gameId, intervalDays[i], nextDayArray[i]);
                resultList.add(prePeriod);
            }
        }
        return resultList;
    }

    private List<GamePeriod> getBasicPeriodList(Long gameId, String dateStr) {
        List<GamePeriod> allPeriodList = PeriodRedis.getTodayAllPeriods(gameId);
        List<GamePeriod> basicPeriodList = new ArrayList<>();
        for (int i = allPeriodList.size() - 1; i >= 0; i--) {
            GamePeriod period = allPeriodList.get(i);
            if (period != null) {
                if (period.getPeriodId().startsWith(dateStr)) {
                    basicPeriodList.add(period);
                } else if (period.getPeriodId().compareTo(dateStr) < 0) {
                    break;
                }
            } else {
                log.error("Generate future period is null, pls check it!");
            }
        }
        return basicPeriodList;
    }

    private GamePeriod predictionPeriod(GamePeriod srcGamePeriod, Long gameId, int intervalDays, String nextDayStr) {
        GamePeriod prePeriod = new GamePeriod(gameId);
        prePeriod.setPeriodId(nextDayStr + srcGamePeriod.getPeriodId().substring(getPeriodDateFormat().length()));
        prePeriod.setStartTime(DateUtil.getIntervalDays(srcGamePeriod.getStartTime(), intervalDays));
        prePeriod.setEndTime(DateUtil.getIntervalDays(srcGamePeriod.getEndTime(), intervalDays));
        prePeriod.setAwardTime(DateUtil.getIntervalDays(srcGamePeriod.getAwardTime(), intervalDays));
        return prePeriod;
    }

    public Boolean isValidWinningNumber(String winningNumber) {
        boolean isRightFormat = true;
        try {
            String[] numbers = winningNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SPACE_SPLIT_STR);
            if (numbers.length != getWinningNumberLength()) {
                isRightFormat = false;
            }
            Pattern pattern = Pattern.compile(getWinningNumberRegexp());
            Matcher m = pattern.matcher(winningNumber.trim());
            if (!m.find()) {
                isRightFormat = false;
            }
        } catch (Exception e) {
            throw new BusinessException("开奖号码格式错误，联系合作商！！！gameEn=" + getGame().getGameEn() + "获取开奖号码：" + winningNumber);
        }
        return isRightFormat;
    }

    /**
     * 返回红蓝球中了的个数
     *
     * @return
     */
    protected int bingoNum(String[] bidBalls, String[] targetBalls) {
        int result = 0;
        for (int i = 0; i < bidBalls.length; i++) {
            for (int j = 0; j < targetBalls.length; j++) {
                int compareResult = bidBalls[i].compareTo(targetBalls[j]);
                if (compareResult == 0) {
                    result++;
                }
            }
        }
        return result;
    }

    public Integer getPeriodInterval() {
        return null;
    }

    public String getInitPeriodFormat() {
        throw new BusinessException("method is not implemented!");
    }

    public Map<String, Object> getPlayTypeAndBonus(RedisService redisService, String periodId, String gameEn) {
        List<AwardInfo> awardInfos = getAwardInfoList(redisService, periodId);
        List<String> resultList = new ArrayList<>();
        List<String> resultList1 = new ArrayList<>();
        Game game = GameCache.getGame(gameEn);
        Map<String, Object> map = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.##");
        for (AwardInfo info : awardInfos) {
            String bonusStr = info.getBonus().compareTo(new BigDecimal(-1)) == 0 ? CommonConstant.COMMON_DASH_STR :
                    df.format(info.getBonus());
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                resultList.add(new StringBuffer().append(info.getLevelName()).append(CommonConstant.COMMA_SPLIT_STR)
                        .append(info.getAwardCount() == null ? CommonConstant.COMMON_DASH_STR : info.getAwardCount())
                        .append(CommonConstant.COMMA_SPLIT_STR).append(bonusStr).toString());
                resultList1.add(new StringBuffer().append(info.getLevelName()).append(CommonConstant.COMMA_SPLIT_STR)
                        .append(getWinCondition(info.getAwardLevel())).append(CommonConstant.COMMA_SPLIT_STR)
                        .append(info.getAwardCount() == null ? CommonConstant.COMMON_DASH_STR : info.getAwardCount())
                        .append(CommonConstant.COMMA_SPLIT_STR).append(bonusStr).toString());
                continue;
            }
            resultList.add(info.getLevelName() + CommonConstant.COMMA_SPLIT_STR + bonusStr);
            resultList1.add(info.getLevelName() + CommonConstant.COMMA_SPLIT_STR + bonusStr);
        }
        if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriodDb(game.getGameId(), periodId);
            JSONObject jsonObject = JSONObject.parseObject(period.getRemark());
            String pool = null;
            String sale = null;
            String testNum = null;
            if (jsonObject != null) {
                pool = jsonObject.getString("pool");
                sale = jsonObject.getString("sale");
                testNum = jsonObject.getString("testNum");
            }
            map.put("periodSale", StringUtils.isBlank(sale) ? CommonConstant.COMMON_DASH_STR : sale);
            map.put("poolBonus", StringUtils.isBlank(pool) ? CommonConstant.COMMON_DASH_STR : pool);
            map.put("testNum", StringUtils.isBlank(testNum) ? CommonConstant.COMMON_DASH_STR : testNum);
        }
        map.put("awardInfoList", resultList);
        map.put("awardInfoListMore", resultList1);
        return map;
    }

    public Timestamp getOfficialStartTime(GamePeriod period, String beginTime) {
        Timestamp timestamp = period.getStartTime();
        String dateStr = DateUtil.getDate(timestamp, DateUtil.DEFAULT_DATE_FORMAT) + CommonConstant.SPACE_SPLIT_STR +
                beginTime;
        SimpleDateFormat format = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS);
        Timestamp start = null;
        try {
            start = new Timestamp(format.parse(dateStr).getTime());
        } catch (Exception ex) {
            throw new BusinessException("error to parse the official start time");
        }
        //北京肇彩的票在第二天八点半才能出
        return DateUtil.getIntervalMinutes(start, 60 * 13);
    }

    public Integer getAwardInfoSize() {
        return null;
    }

    public List<AwardInfo> getAwardInfoList(RedisService redisService, String periodId) {
        List<AwardInfo> awardInfos = redisService.kryoHget(RedisConstant.getAwardInfoKey(getGame().getGameId()),
                periodId, ArrayList.class);
        if (awardInfos == null) {
            return getDefaultAwardInfoList();
        }
        return awardInfos;
    }

    /* 期次的开奖信息*/
    public AwardInfoVo calcWinningNumber(GamePeriod gamePeriod) {
        Map<String, Object> resultMap = new HashMap<>(); // 开奖结果集合
        Integer awardStatus = 0; // 开奖状态

        String winningNumbers = gamePeriod.getWinningNumbers(); // 开奖号码
        if (!StringUtils.isBlank(winningNumbers) && winningNumbers.length() == getWinningNumberAllLength(gamePeriod
                .getGameId())) {
            winningNumbers = winningNumbers.trim();
            awardStatus = 1;
        } else {
            resultMap.put("awardStatusCn", "等待开奖");
        }
        AwardInfoVo awardInfoVo = new AwardInfoVo(gamePeriod.getPeriodId(), winningNumbers, resultMap, GameCache
                .getGame(gamePeriod.getGameId()).getGameName(), GameCache.getGame(gamePeriod.getGameId()).getGameEn()
                , awardStatus, DateUtil.getCommonGameAwardTime(gamePeriod.getAwardTime()));
        return awardInfoVo;
    }

    /**
     * @param requestNum 需要的数量
     * @param bingoDan   胆码中奖数量
     * @param bingoTuo   拖码中奖数量
     * @param bidDan     胆码个数
     * @param bidAll     一注要求的个数
     * @return
     */
    protected int bingoDanTuo(int requestNum, int bingoDan, int bingoTuo, int bidDan, int bidTuo, int bidAll) {
        if (bingoDan > requestNum || bingoDan + bingoTuo < requestNum)
            return 0;

        int leftTuo = requestNum - bingoDan; // 拖码需中球数
        if (leftTuo + bidDan > bidAll)
            return 0;

        int missingTuo = bidTuo - bingoTuo; // 拖码未中球数
        int bidMissingTuo = bidAll - bidDan - leftTuo;

        return CommonUtil.combine(bingoTuo, leftTuo) * CommonUtil.combine(missingTuo, bidMissingTuo);
    }

    public static int getWinningNumberAllLength(String gameEn) {
        if (gameEn.equals(GameConstant.FC3D)) {
            return FC3D_GAME_WINNING_NUMBER_LENGTH;
        }
        return COMMON_GAME_WINNING_NUMBER_LENGTH;
    }

    public static int getWinningNumberAllLength(long gameId) {
        String gameEn = GameCache.getGame(gameId).getGameEn();
        if (gameEn.equals(GameConstant.FC3D)) {
            return FC3D_GAME_WINNING_NUMBER_LENGTH;
        }
        return COMMON_GAME_WINNING_NUMBER_LENGTH;
    }

    public String getWinCondition(String level) {
        return "-";
    }

    public List splitWinArea(String area) {
        return null;
    }
}