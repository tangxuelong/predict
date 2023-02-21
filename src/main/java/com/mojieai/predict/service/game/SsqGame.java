package com.mojieai.predict.service.game;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.GameUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class SsqGame extends AbstractGame {

    public final static List<AwardInfo> SSQ_AWARD_INFO_LIST = new ArrayList<>();
    private static final String SSQ_BEGIN_TIME = "20:00:00";
    public final static Set SSQ_FOUR_CONSECUTIVENUMBERS = new HashSet();
    public final static Map<String, String> WIN_CONDITION = new HashMap();

    private static String regexp = "^(\\(){0,1}((0[1-9]|1\\d|2\\d|3[0123]) ){0,4}(0[1-9]|1\\d|2\\d|3[0123]){0,1}(\\))"
            + "{0,1}((0[1-9]|1\\d|2\\d|3[0123]) ){1,31}(0[1-9]|1\\d|2\\d|3[0123])"
            + ":((0[1-9]|1[0-6]) ){0,15}(0[1-9]|1[0-6])$";
    private static String winningNUmberRegexp = "^((0[1-9]|1\\d|2\\d|3[0123]) ){5}(0[1-9]|1\\d|2\\d|3[0123])"
            + ":((0[1-9]|1[0-6]))$";
    private static Pattern pattern = Pattern.compile(regexp);
    private static Pattern blueBallPattern = Pattern.compile("^((0[1-9]|1[0-6]) ){0,15}(0[1-9]|1[0123456])$");
    private static Pattern redBallPattern = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){5,32}(0[1-9]|1\\d|2\\d|3[0123])$");

    /* 6 || 1*/
    private static Pattern redBallPatternSingle = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){5}(0[1-9]|1\\d|2\\d|3[0123])$");
    private static Pattern blueBallPatternSingle = Pattern.compile("^(0[1-9]|1[0123456])$");

    /* 6+ || 1+*/
    private static Pattern redBallPatternMultiple = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){6,32}(0[1-9]|1\\d|2\\d|3[0123])$");
    private static Pattern blueBallPatternMultiple = Pattern.compile("^((0[1-9]|1[0-6]) ){1,15}(0[1-9]|1[0123456])$");

    /* 5- || 1-*/
    private static Pattern redBallPatternNotOne = Pattern.compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){0,4}" +
            "(0[1-9]|1\\d|2\\d|3[0123])$");
    private static Pattern dBallPattern = Pattern
            .compile("^(\\(){0,1}((0[1-9]|1\\d|2\\d|3[0123]) ){0,4}(0[1-9]|1\\d|2\\d|3[0123])(\\))$");
    private static Pattern tBallPattern = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){1,31}(0[1-9]|1\\d|2\\d|3[0123])$");

    private static Pattern redBallPatternOnly = Pattern.compile("^((0[1-9]|1\\d|2\\d|3[0123]) ){0,32}" +
            "(0[1-9]|1\\d|2\\d|3[0123])$");


    static {
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("1", "一等奖", new BigDecimal(-1)));
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("2", "二等奖", new BigDecimal(-1)));
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("3", "三等奖", new BigDecimal(3000)));
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("4", "四等奖", new BigDecimal(200)));
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("5", "五等奖", new BigDecimal(10)));
        SSQ_AWARD_INFO_LIST.add(new AwardInfo("6", "六等奖", new BigDecimal(5)));

        for (int i = 1; i <= 30; i++) {
            StringBuffer tempSb = new StringBuffer();
            tempSb.append(CommonUtil.convertStrNum(i)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 1)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 2)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 3)).append(CommonConstant.SPACE_SPLIT_STR);
            SSQ_FOUR_CONSECUTIVENUMBERS.add(tempSb.toString().trim());
        }

        WIN_CONDITION.put("1", "6+1");
        WIN_CONDITION.put("2", "6+0");
        WIN_CONDITION.put("3", "5+1");
        WIN_CONDITION.put("4", "5+0#4+1");
        WIN_CONDITION.put("5", "4+0#3+1");
        WIN_CONDITION.put("6", "2+1#1+1#\n0+1");
    }

    public static final String PUSH_URL_WINNING_NUMBER = "mjLottery://mjNative?page=kjlb&gameName=双色球&gameEn=ssq";

    /* 获取game*/
    @Override
    public Game getGame() {
        return GameEnum.SSQ.getGame();
    }

    /* 获取官方的开始时间*/
    @Override
    public Timestamp getOfficialStartTime(GamePeriod gamePeriod) {
        return getOfficialStartTime(gamePeriod, SSQ_BEGIN_TIME);
    }

    /* 期次格式2017012*/
    @Override
    public String getPeriodDateFormat() {
        return DateUtil.DATE_FORMAT_YYYY;
    }

    @Override
    public String getWinningNumberPushUrl() {
        return PUSH_URL_WINNING_NUMBER;
    }

    @Override
    public String[] getAllRedNums() {
        return new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
                "33"};
    }

    @Override
    public boolean checkLotteryNumberIfValid(String lotteryNumber) {
        lotteryNumber = lotteryNumber.trim();
        if (StringUtils.isBlank(lotteryNumber)) {
            log.error("[校验投注号码格式]投注号码为空");
            return false;
        }
        if (lotteryNumber.contains(CommonConstant.COMMON_BRACKET_LEFT)) {
            if (!checkSsqDantuoIfValid(lotteryNumber)) {
                return false;
            }
        } else {
            if (!GameUtil.checkDanshiFushiIfValid(lotteryNumber, redBallPattern, blueBallPattern)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Integer checkLotteryNumberTypeIfValid(String lotteryNumber) {
        lotteryNumber = lotteryNumber.trim();
        if (StringUtils.isBlank(lotteryNumber)) {
            log.error("[校验投注号码格式]投注号码为空");
            return ResultConstant.ERROR;
        }
        if (lotteryNumber.contains(CommonConstant.COMMON_BRACKET_LEFT)) {
            if (!checkSsqDantuoIfValid(lotteryNumber)) {
                return ResultConstant.ERROR;
            }
        } else {
            /* 不够一注*/
            /* 只有红球 1-33*/
            if (StringUtils.endsWith(lotteryNumber, CommonConstant.COMMON_COLON_STR)) {
                if (GameUtil.checkRedBallValid(lotteryNumber, redBallPatternOnly)) {
                    return ResultConstant.LOTTERY_NUMBER_TYPE_ONLY_RED;
                }
            }
            /* 只有蓝球 1-16*/
            if (StringUtils.startsWith(lotteryNumber, CommonConstant.COMMON_COLON_STR)) {
                if (GameUtil.checkRedBallValid(lotteryNumber, blueBallPattern)) {
                    return ResultConstant.LOTTERY_NUMBER_TYPE_ONLY_BLUE;
                }
            }
            /* 复式*/
            if (GameUtil.checkDanshiFushiIfValid(lotteryNumber, redBallPatternMultiple, blueBallPattern) || GameUtil
                    .checkDanshiFushiIfValid(lotteryNumber, redBallPattern, blueBallPatternMultiple)) {
                return ResultConstant.LOTTERY_NUMBER_TYPE_MULTIPLE;
            }
            /* 单式*/
            if (GameUtil.checkDanshiFushiIfValid(lotteryNumber, redBallPatternSingle, blueBallPatternSingle)) {
                return ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE;
            }
            /* 红球篮球都有 不够一注 1-5 + 1-16*/
            if (GameUtil.checkDanshiFushiIfValid(lotteryNumber, redBallPatternNotOne, blueBallPattern)) {
                return ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE_NOT;
            }
            log.error("红球篮球格式校验非法" + lotteryNumber);
        }
        return ResultConstant.ERROR;
    }

    private boolean checkSsqDantuoIfValid(String lotteryNumber) {
        String[] redAndBlue = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
        try {
            String dRedBalls = redAndBlue[0].substring(0, redAndBlue[0].indexOf(")") + 1);
            if (!(dBallPattern.matcher(dRedBalls).find())) {
                log.error("[校验投注号码格式]红球胆码格式非法" + lotteryNumber);
                return false;
            }
            String tRedBalls = redAndBlue[0].substring(redAndBlue[0].indexOf(")") + 1);
            if (!(tBallPattern.matcher(tRedBalls).find())) {
                log.error("[校验投注号码格式]红球拖码格式非法" + lotteryNumber);
                return false;
            }
            if (!(blueBallPattern.matcher(redAndBlue[1]).find())) {
                log.error("[校验投注号码格式]单复式蓝球格式非法" + lotteryNumber);
                return false;
            }
            String[] redBalls = redAndBlue[0].replace(")", CommonConstant.SPACE_SPLIT_STR).substring(1).split
                    (CommonConstant.SPACE_SPLIT_STR);
            if (redBalls.length > 33) {
                return false;
            }
            String[] blueBalls = redAndBlue[1].split(CommonConstant.SPACE_SPLIT_STR);
            Arrays.sort(redBalls);
            Arrays.sort(blueBalls);
            if (GameUtil.checkDuplicate(redBalls) || GameUtil.checkDuplicate(blueBalls)) {
                log.info("[校验投注号码格式]stakeNumber is not valid! duplicate error!" + lotteryNumber);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[校验投注号码格式_胆拖]异常" + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period) {
        String[] bidCoreRedBalls = new String[0];
        String tuoBalls;
        if (bidBalls.contains(CommonConstant.COMMON_BRACKET_RIGHT)) {
            String bidCoreBalls = bidBalls.split(CommonConstant.COMMON_ESCAPE_STR + ")")[0].substring(1).trim();
            tuoBalls = bidBalls.split(CommonConstant.COMMON_ESCAPE_STR + ")")[1].trim();
            bidCoreRedBalls = bidCoreBalls.split(CommonConstant.SPACE_SPLIT_STR); // 胆码红球
            Arrays.sort(bidCoreRedBalls);//对胆码红球进行排序
        } else {
            tuoBalls = bidBalls;
        }

        String[] balls = tuoBalls.split(CommonConstant.COMMON_COLON_STR);
        String[] bidRedBalls = balls[0].split(CommonConstant.SPACE_SPLIT_STR); // 拖码红球
        String[] bidBlueBalls = balls[1].split(CommonConstant.SPACE_SPLIT_STR); // 蓝球
        Arrays.sort(bidRedBalls);//对拖码红球进行排序
        Arrays.sort(bidBlueBalls);//对篮球球进行排序
        balls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR);
        String[] targetRedBall = balls[0].split(CommonConstant.SPACE_SPLIT_STR);
        String targetBlueBall = balls[1];
        return analyseBidAwardLevels(bidCoreRedBalls, bidRedBalls, bidBlueBalls, targetRedBall, targetBlueBall);
    }

    /**
     * 当期投注额的50%，其中49%用于当期奖金分配，1%用于调节基金。
     * 高等奖奖金＝奖金总额－固定奖奖金
     * 一等奖：
     * 一等奖：
     * 当奖池资金低于1亿元时，奖金总额为当期高奖级奖金的75%与奖池中累积的资金之和，单注奖金按注均分，单注最高限额封顶500万元。
     * 当奖池资金高于1亿元（含）时，奖金总额包括两部分，
     * 一部分为当期高奖级奖金的55%与奖池中累积的资金之和，单注奖金按注均分，单注最高限额封顶500万元；
     * 另一部分为当期高奖级奖金的20%，单注奖金按注均分，单注最高限额封顶500万元。
     * 二等奖：
     * 二等奖：奖金总额为当期高奖级奖金的25%，单注奖金按注均分，单注最高限额封顶500万元。
     * 如果没有数据报警，并且默认按照最高奖金算，一等奖1000w，二等奖500w
     */
    @Override
    public Map<String, Object[]> getAwardLevelRatioMap() {
        Map<String, Object[]> awardLevelRationMap = new HashMap<>();
        List<String[]> ratio1List = new ArrayList<>();
        ratio1List.add(new String[]{"0.55", "0.20"});
        ratio1List.add(new String[]{"0.75"});
        awardLevelRationMap.put("1", new Object[]{ratio1List, new BigDecimal[]{CommonConstant.BONUS_10000_LIMIT}});
        List<String[]> ratio2List = new ArrayList<>();
        ratio2List.add(new String[]{"0.25"});
        awardLevelRationMap.put("2", new Object[]{ratio2List});
        return awardLevelRationMap;
    }

    /**
     * @param bidCoreRedBall 胆码红球
     * @param bidRedBall     拖码红球
     * @param bidBlueBall    蓝球
     * @param targetRedBall  中奖红球
     * @param targetBlueBall 中奖蓝球
     * @return
     */
    public int[] analyseBidAwardLevels(String[] bidCoreRedBall, String[] bidRedBall, String[] bidBlueBall,
                                       String[] targetRedBall, String targetBlueBall) {
        int[] result = new int[]{0, 0, 0, 0, 0, 0};
        // 返回{拖码红球中奖数，蓝球中奖数，胆码红球中奖数}
        int[] bingoNum = bingoNum(bidCoreRedBall, bidRedBall, bidBlueBall, targetRedBall, targetBlueBall);
        return analyseBidAwardLevels(bidCoreRedBall.length, bidRedBall.length, bidBlueBall.length, 0, bingoNum[2],
                bingoNum[0], bingoNum[1], 0, null);
    }

    public int[] analyseBidAwardLevels(int redDan, int redTuo, int blueDan, int blueTuo, int bingoRedDan, int
            bingoRedTuo, int bingoBlueDan, int bingoBlueTuo, String periodId) {
        if (redDan < 0 || redTuo < 0 || blueDan < 0 || blueTuo < 0 || bingoRedDan < 0 || bingoRedTuo < 0 ||
                bingoBlueDan < 0 || bingoBlueTuo < 0) {
            throw new BusinessException("参数有误！");
        }
        int[] result = new int[]{0, 0, 0, 0, 0, 0};
        int[][] levelInfo = SsqGameEnum.LEVEL_INFO;
        for (int i = 0; i < levelInfo.length; i++) {
            int count = bingoRedMultiple(levelInfo[i][1], bingoRedDan, bingoRedTuo, redDan, redTuo) *
                    bingoBlueMultiple(levelInfo[i][2], bingoBlueDan, blueDan, bingoBlueTuo, blueTuo);
            result[levelInfo[i][0] - 1] += count;
        }
        return result;
    }

    /**
     * 返回红蓝球中了的个数
     *
     * @return
     */
    private int[] bingoNum(String[] bidCoreRedBall, String[] bidRedBall, String[] bidBlueBall, String[] targetRedBalls,
                           String targetBlueBall) {
        int[] numArray = {0, 0, 0};
        int j = 0;
        for (int i = 0; i < bidRedBall.length; i++) {
            int compareResult = bidRedBall[i].compareTo(targetRedBalls[j]);
            if (compareResult == 0) {
                j++;
                numArray[0]++;
            } else if (compareResult > 0) {
                j++;
                i--;
            }
            if (j >= targetRedBalls.length) {
                break;
            }
            continue;
        }

        j = 0;
        for (int i = 0; i < bidCoreRedBall.length; i++) {
            int compareResult = bidCoreRedBall[i].compareTo(targetRedBalls[j]);
            if (compareResult == 0) {
                j++;
                numArray[2]++;
            } else if (compareResult > 0) {
                j++;
                i--;
            }
            if (j >= targetRedBalls.length) {
                break;
            }
            continue;
        }

        for (String blueBall : bidBlueBall) {
            if (blueBall.equalsIgnoreCase(targetBlueBall)) {
                numArray[1] = 1;
            }
        }
        return numArray;
    }

    /*
     * 返回篮球中奖的倍数
     */
    private int bingoBlueMultiple(int requestBlue, int bingoBlue, int bidBlueBallNum, int bingoBuleTuo, int blueTuo) {
        if (bingoBlue <= 0 && bingoBuleTuo > 0) {
            bingoBlue = bingoBuleTuo;
        }
        if (bidBlueBallNum <= 0 && blueTuo > 0) {
            bidBlueBallNum = blueTuo;
        }
        if (requestBlue == 1) // 要求篮球要中
        {
            return bingoBlue; // 篮球中返回1，否则返回0
        } else {
            return bidBlueBallNum - bingoBlue; // 所选篮球的个数减去中了的
        }
    }

    /**
     * 返回红球中奖的倍数
     *
     * @param requestRed           要中奖红球数
     * @param bingoCore            胆码中奖数
     * @param bingoRed             拖码中奖数
     * @param bidCoreRedBallLength 胆码红球数
     * @param bidCoreRedBallLength 拖码红球数
     */
    private int bingoRedMultiple(int requestRed, int bingoCore, int bingoRed, int bidCoreRedBallLength,
                                 int bidRedBallLength) {
        int maxBingoRed = Math.min(SsqGameEnum.RED_BALL_NUM - bidCoreRedBallLength, bingoRed); // 最多中奖拖码个数
        /*
         * 如果胆码+拖码中奖数不足，则不可能 如果是胆拖，且胆码中奖数已经超过要求，则不可能
		 */
        if (maxBingoRed + bingoCore < requestRed || (maxBingoRed >= 0 && bingoCore > requestRed)) {
            return 0;
        }
        return CommonUtil.combine(bingoRed, requestRed - bingoCore) * CommonUtil.combine(bidRedBallLength -
                bingoRed, SsqGameEnum.RED_BALL_NUM - bidCoreRedBallLength - requestRed + bingoCore);
    }

    /* 开奖号码长度*/
    @Override
    protected int getWinningNumberLength() {
        return 6; //按照空格分隔之后的数组的长度
    }

    /* 开奖号码正则表达式*/
    @Override
    protected String getWinningNumberRegexp() {
        return winningNUmberRegexp;
    }

    @Override
    //大盘彩返回1期，get未来三天的期次返回的其实是一年的
    public Integer getDailyPeriod() {
        return 1;
    }

    @Override
    public Integer getAwardInfoSize() {
        return SSQ_AWARD_INFO_LIST.size();
    }

    @Override
    public Set<String> getDefaultConsecutiveNumbers() {
        return SSQ_FOUR_CONSECUTIVENUMBERS;
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList() {
        return SSQ_AWARD_INFO_LIST;
    }

    public String getWinCondition(String level) {
        if (!WIN_CONDITION.containsKey(level)) {
            return "-";
        }
        return WIN_CONDITION.get(level);
    }

    /* 拆分
    *
    * "area":"黑龙江1注,江苏1注,浙江10注,福建1注,山东1注,湖北2注,湖南1注,广西1注,四川1注,甘肃1注,宁夏1注,深圳1注"
    * */
    @Override
    public List splitWinArea(String area) {
        if (StringUtils.isBlank(area)) {
            return null;
        }
        List<Map<String, String>> res = new ArrayList<>();
        String[] areaInfos = area.split(CommonConstant.COMMA_SPLIT_STR);
        for (String areaInfo : areaInfos) {
            Map<String, String> tempArea = new HashMap<>();
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            String baseCount = p.matcher(areaInfo).replaceAll("") + "注";
            String areaName = areaInfo.substring(0, areaInfo.indexOf(baseCount));

            tempArea.put("areaName", areaName);
            tempArea.put("baseCount", baseCount);
            res.add(tempArea);
        }
        return res;
    }

    @Override
    public List<String> getGameAwardCondition() {
        List<String> awardCondition = new ArrayList<>();
        awardCondition.add("6+1");
        awardCondition.add("6+0");
        awardCondition.add("5+1");
        awardCondition.add("5+0");
        awardCondition.add("4+1");
        awardCondition.add("4+0");
        awardCondition.add("3+1");
        awardCondition.add("2+1");
        awardCondition.add("1+1");
        awardCondition.add("0+1");
        return awardCondition;
    }

    @Override
    public List<Object> awardCondition() {
        List<Object> awardConditionList = new ArrayList<>();

        Map<String, Object> awardCondition1 = new HashMap<>();
        awardCondition1.put("levelName", "一等奖");
        awardCondition1.put("levelCondition", "6+1");
        awardCondition1.put("awardAmount", "A元");
        awardConditionList.add(awardCondition1);

        Map<String, Object> awardCondition2 = new HashMap<>();
        awardCondition2.put("levelName", "二等奖");
        awardCondition2.put("levelCondition", "6+0");
        awardCondition2.put("awardAmount", "B元");
        awardConditionList.add(awardCondition2);

        Map<String, Object> awardCondition3 = new HashMap<>();
        awardCondition3.put("levelName", "三等奖");
        awardCondition3.put("levelCondition", "5+1");
        awardCondition3.put("awardAmount", "3000元");
        awardConditionList.add(awardCondition3);

        Map<String, Object> awardCondition4 = new HashMap<>();
        awardCondition4.put("levelName", "四等奖");
        awardCondition4.put("levelCondition", "5+0，4+1");
        awardCondition4.put("awardAmount", "200元");
        awardConditionList.add(awardCondition4);

        Map<String, Object> awardCondition5 = new HashMap<>();
        awardCondition5.put("levelName", "五等奖");
        awardCondition5.put("levelCondition", "4+0，3+1");
        awardCondition5.put("awardAmount", "10元");
        awardConditionList.add(awardCondition5);

        Map<String, Object> awardCondition6 = new HashMap<>();
        awardCondition6.put("levelName", "六等奖");
        awardCondition6.put("levelCondition", "2+1，1+1，0+1");
        awardCondition6.put("awardAmount", "5元");
        awardConditionList.add(awardCondition6);

        return awardConditionList;
    }

    @Override
    public String getAwardDesc() {
        return "A是一等奖对应的浮动奖金，一般为500-1000万；B是二等奖 对应的浮动奖金10-100万";
    }

    @Override
    public List<AwardInfo> getAwardAmountList() {
        return SSQ_AWARD_INFO_LIST;
    }
}