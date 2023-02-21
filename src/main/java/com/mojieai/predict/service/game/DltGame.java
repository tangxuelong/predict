package com.mojieai.predict.service.game;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.DltGameEnum;
import com.mojieai.predict.enums.GameEnum;
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

/**
 * Created by kyle on 2017/2/4.
 */
@Component
public class DltGame extends AbstractGame {
    //生肖乐玩法一直到 2012053 截止;8个奖级变成6个一直到 2014051 截止
    public static final BigDecimal DLT_PRICE_APPEND = new BigDecimal("3");
    public final static List<AwardInfo> DLT_AWARD_INFO_LIST = new ArrayList<>();
    public final static List<AwardInfo> DLT_AWARD_INFO_OLD_LIST = new ArrayList<>();
    public final static List<AwardInfo> DLT_AWARD_AMOUNT_LIST = new ArrayList<>();
    public static final String DLT_APPEND_AWARDLEVEL_SUFFIX = "_z";
    private static final String DLT_BEGIN_TIME = "20:00:00";
    public final static Set DLT_FOUR_CONSECUTIVENUMBERS = new HashSet();

    public final static Map<String, String> WIN_CONDITION = new HashMap();
    //注意正则中的空格是必不可少的
    private static Pattern redBallPattern = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){4,34}(0[1-9]|1\\d|2\\d|3[0-5])$");
    private static Pattern blueBallPattern = Pattern.compile("^((0[1-9]|1[0-2]) ){1,11}(0[1-9]|1[0-2])$");
    private static Pattern dRedBallPatternNew = Pattern
            .compile("^(\\(){0,1}((0[1-9]|1\\d|2\\d|3[0-5]) ){0,3}(0[1-9]|1\\d|2\\d|3[0-5])(\\))$");
    private static Pattern tRedBallPatternNew = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){1,33}(0[1-9]|1\\d|2\\d|3[0-5])$");
    private static Pattern dBlueBallPatternNew = Pattern.compile("^(\\()0[1-9]|1[0-2](\\))$"); //蓝球也是胆拖的话，蓝胆必然会有一个
    private static Pattern tBlueBallPatternNew = Pattern.compile("^((0[1-9]|1[0-2]) ){1,10}(0[1-9]|1[0-2])$");
    //蓝球也是胆拖的话，蓝胆必然会有一个，所以蓝拖最多11个


    private static Pattern redBallPatternMultiple = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){5,34}(0[1-9]|1\\d|2\\d|3[0-5])$");

    private static Pattern blueBallPatternMultiple = Pattern.compile("^((0[1-9]|1[0-2]) ){2,11}(0[1-9]|1[0-2])$");

    private static Pattern redBallPatternSingle = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){4}(0[1-9]|1\\d|2\\d|3[0-5])$");

    private static Pattern blueBallPatternSingle = Pattern.compile("^((0[1-9]|1[0-2]) )(0[1-9]|1[0-2])$");

    private static Pattern redBallPatternNotOne = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){0,3}(0[1-9]|1\\d|2\\d|3[0-5])$");

    private static Pattern blueBallPatternNotOne = Pattern.compile("^(0[1-9]|1[0-2])$");

    private static Pattern redBallPatternOnly = Pattern
            .compile("^((0[1-9]|1\\d|2\\d|3[0-5]) ){0,34}(0[1-9]|1\\d|2\\d|3[0-5])$");
    private static Pattern blueBallPatternOnly = Pattern.compile("^((0[1-9]|1[0-2]) ){0,11}(0[1-9]|1[0-2])$");


    static {
        DLT_AWARD_INFO_LIST.add(new AwardInfo("1", "一等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("1_z", "一等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("2", "二等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("2_z", "二等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("3", "三等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("3_z", "三等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("4", "四等奖[基本]", new BigDecimal(200)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("4_z", "四等奖[追加]", new BigDecimal(100)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("5", "五等奖[基本]", new BigDecimal(10)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("5_z", "五等奖[追加]", new BigDecimal(5)));
        DLT_AWARD_INFO_LIST.add(new AwardInfo("6", "六等奖[基本]", new BigDecimal(5)));

        for (int i = 1; i <= 32; i++) {
            StringBuffer tempSb = new StringBuffer();
            tempSb.append(CommonUtil.convertStrNum(i)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 1)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 2)).append(CommonConstant.SPACE_SPLIT_STR);
            tempSb.append(CommonUtil.convertStrNum(i + 3)).append(CommonConstant.SPACE_SPLIT_STR);
            DLT_FOUR_CONSECUTIVENUMBERS.add(tempSb.toString().trim());
        }
    }

    static {
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("1", "一等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("1_z", "一等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("2", "二等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("2_z", "二等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("3", "三等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("3_z", "三等奖[追加]", new BigDecimal(-1)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("4", "四等奖[基本]", new BigDecimal(200)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("5", "四等奖[追加]", new BigDecimal(100)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("6", "五等奖[基本]", new BigDecimal(10)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("7", "五等奖[追加]", new BigDecimal(5)));
        DLT_AWARD_INFO_OLD_LIST.add(new AwardInfo("8", "六等奖[基本]", new BigDecimal(5)));

        WIN_CONDITION.put("1", "5+2");
        WIN_CONDITION.put("2", "5+1");
        WIN_CONDITION.put("3", "5+0#4+2");
        WIN_CONDITION.put("4", "4+1#3+2");
        WIN_CONDITION.put("6", "4+0#3+1#\n2+2");
        WIN_CONDITION.put("8", "3+0#1+2#\n2+1#0+2");

        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("1", "一等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("2", "二等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("3", "三等奖[基本]", new BigDecimal(-1)));
        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("4", "四等奖[基本]", new BigDecimal(200)));
        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("5", "五等奖[基本]", new BigDecimal(10)));
        DLT_AWARD_AMOUNT_LIST.add(new AwardInfo("6", "六等奖[基本]", new BigDecimal(5)));

    }


    private static final String PUSH_URL_WINNING_NUMBER = "mjLottery://mjNative?page=kjlb&gameName=大乐透&gameEn=dlt";

    @Override
    public Game getGame() {
        return GameEnum.DLT.getGame();
    }

    @Override
    public Timestamp getOfficialStartTime(GamePeriod gamePeriod) {
        return getOfficialStartTime(gamePeriod, DLT_BEGIN_TIME);
    }

    @Override
    public String getPeriodDateFormat() {
        return DateUtil.DATE_FORMAT_YY;
    }

    @Override
    public String getWinningNumberPushUrl() {
        return PUSH_URL_WINNING_NUMBER;
    }

    @Override
    public String[] getAllRedNums() {
        return new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
                "33", "34", "35"};
    }

    @Override
    protected int getWinningNumberLength() {
        return 6; //01 02 03 04 05:01 02，按照空格分隔之后的数组的长度
    }

    @Override
    protected String getWinningNumberRegexp() {
        return "((0[1-9]|1\\d|2\\d|3[0-5]) ){4}(0[1-9]|1\\d|2\\d|3[0-5]):((0[1-9]|1[0-2]) (0[1-9]|1[0-2]))";
    }

    @Override
    public Integer getDailyPeriod() {
        return 1;
    }

    @Override
    public Integer getAwardInfoSize() {
        return DLT_AWARD_INFO_LIST.size();
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList() {
        return DLT_AWARD_INFO_OLD_LIST;
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList(GamePeriod period) {
        if (period.getPeriodId().compareTo(CommonConstant.DLT_EIGHT_AWARD_LEVEL_LAST_PEIROD_ID) > 0) {
            return DLT_AWARD_INFO_LIST;
        }
        return DLT_AWARD_INFO_OLD_LIST;
    }

    @Override
    public boolean checkLotteryNumberIfValid(String lotteryNumber) {
        lotteryNumber = lotteryNumber.trim();
        if (StringUtils.isBlank(lotteryNumber)) {
            log.error("[校验投注号码格式]投注号码为空");
            return false;
        }
        if (lotteryNumber.contains(CommonConstant.COMMON_BRACKET_LEFT)) {
            if (!checkDltDantuoIfValid(lotteryNumber)) {
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
            if (!checkDltDantuoIfValid(lotteryNumber)) {
                return ResultConstant.ERROR;
            }
        } else {
            /* 不够一注*/
            if (StringUtils.endsWith(lotteryNumber, CommonConstant.COMMON_COLON_STR)) {
                if (GameUtil.checkRedBallValid(lotteryNumber, redBallPatternOnly)) {
                    return ResultConstant.LOTTERY_NUMBER_TYPE_ONLY_RED;
                }
            }
            if (StringUtils.startsWith(lotteryNumber, CommonConstant.COMMON_COLON_STR)) {
                if (GameUtil.checkRedBallValid(lotteryNumber, blueBallPatternOnly)) {
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
            /* 红球篮球都有 不够一注*/
            if (GameUtil.checkDanshiFushiIfValid(lotteryNumber, redBallPatternOnly, blueBallPatternOnly)) {
                return ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE_NOT;
            }
            log.error("红球篮球格式校验非法" + lotteryNumber);
        }
        return ResultConstant.ERROR;
    }

    @Override
    public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period) {
        String[] balls = bidBalls.split(CommonConstant.COMMON_COLON_STR);
        String redBalls = balls[0]; //投注的红球
        String blueBalls = balls[1]; //投注的蓝球
        balls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR);
        String[] targetRedBall = balls[0].split(CommonConstant.SPACE_SPLIT_STR); //开奖红球号码
        String[] targetBlueBall = balls[1].split(CommonConstant.SPACE_SPLIT_STR); //开奖篮球号码
        String[][] dantuoBalls = GameUtil.dantuo(redBalls, DltGameEnum.FRONT_BALL_NUM); //投注红球胆、拖码
        int bingoDanRed = bingoNum(dantuoBalls[0], targetRedBall); //红球胆码中球数
        int bingoTuoRed = bingoNum(dantuoBalls[1], targetRedBall); //红球拖码中球数
        int bidDanRed = dantuoBalls[0].length; //红球胆码个数
        int bidTuoRed = dantuoBalls[1].length; //红球拖码个数
        dantuoBalls = GameUtil.dantuo(blueBalls, DltGameEnum.BACK_BALL_NUM); //投注的蓝球胆、拖码
        int bingoDanBlue = bingoNum(dantuoBalls[0], targetBlueBall); //蓝球胆码中球数
        int bingoTuoBlue = bingoNum(dantuoBalls[1], targetBlueBall); //蓝球拖码中球数
        int bidDanBlue = dantuoBalls[0].length; //蓝球胆码的个数
        int bidTuoBlue = dantuoBalls[1].length; //蓝球拖码的个数
        return analyseBidAwardLevels(bidDanRed, bidTuoRed, bidDanBlue, bidTuoBlue, bingoDanRed, bingoTuoRed,
                bingoDanBlue, bingoTuoBlue, period.getPeriodId());
    }

    public int[] analyseBidAwardLevels(int redDan, int redTuo, int blueDan, int blueTuo, int bingoRedDan, int
            bingoRedTuo, int bingoBlueDan, int bingoBlueTuo, String periodId) {
        if (redDan < 0 || redTuo < 0 || blueDan < 0 || blueTuo < 0 || bingoRedDan < 0 || bingoRedTuo < 0 ||
                bingoBlueDan < 0 || bingoBlueTuo < 0) {
            throw new BusinessException("参数有误！");
        }
        int[] result;
        int[][] levelInfo;
        if (periodId.compareTo(CommonConstant.DLT_EIGHT_AWARD_LEVEL_LAST_PEIROD_ID) > 0) {
            result = new int[]{0, 0, 0, 0, 0, 0};
            levelInfo = DltGameEnum.LEVEL_INFO;
        } else {
            result = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
            levelInfo = DltGameEnum.LEVEL_INFO_OLD;
        }
        for (int i = 0; i < levelInfo.length; i++) {
            int count = bingoDanTuo(levelInfo[i][1], bingoRedDan, bingoRedTuo, redDan, redTuo, DltGameEnum
                    .FRONT_BALL_NUM)
                    * bingoDanTuo(levelInfo[i][2], bingoBlueDan, bingoBlueTuo, blueDan, blueTuo, DltGameEnum
                    .BACK_BALL_NUM);
            result[levelInfo[i][0] - 1] += count;
        }
        return result;
    }

    public boolean checkDltDantuoIfValid(String stakeNumber) {
        String[] redAndBlue = stakeNumber.split(":");
        try {
            String dRedBalls = redAndBlue[0].substring(0, redAndBlue[0].indexOf(")") + 1);
            if (!(dRedBallPatternNew.matcher(dRedBalls).find())) {
                log.info("[校验投注号码格式]红球胆码格式非法" + stakeNumber);
                return false;
            }
            String tRedBalls = redAndBlue[0].substring(redAndBlue[0].indexOf(")") + 1);
            if (!(tRedBallPatternNew.matcher(tRedBalls).find())) {
                log.info("[校验投注号码格式]红球拖码格式非法" + stakeNumber);
                return false;
            }
            String[] blueBalls = null;
            if (redAndBlue[1].contains(")")) {
                String dBlueBalls = redAndBlue[1].substring(0, redAndBlue[1].indexOf(")") + 1);
                if (!(dRedBallPatternNew.matcher(dBlueBalls).find())) {
                    log.info("[校验投注号码格式]蓝球胆码格式非法" + stakeNumber);
                    return false;
                }
                String tBlueBalls = redAndBlue[1].substring(redAndBlue[1].indexOf(")") + 1);
                if (!(tRedBallPatternNew.matcher(tBlueBalls).find())) {
                    log.info("[校验投注号码格式]蓝球拖码格式非法" + stakeNumber);
                    return false;
                }
                blueBalls = redAndBlue[1].replace(")", " ").substring(1).split(" ");
                if (blueBalls.length > 12) {
                    log.info("[校验投注号码格式]蓝球长度过长" + stakeNumber);
                    return false;
                }
            } else {
                if (!(blueBallPattern.matcher(redAndBlue[1]).find())) {
                    log.info("[校验投注号码格式]单复式蓝球格式非法" + stakeNumber);
                    return false;
                }
                blueBalls = redAndBlue[1].split(" ");
            }

            String[] redBalls = redAndBlue[0].replace(")", " ").substring(1).split(" ");
            if (redBalls.length > 35) {
                return false;
            }

            Arrays.sort(redBalls);
            Arrays.sort(blueBalls);
            if (GameUtil.checkDuplicate(redBalls) || GameUtil.checkDuplicate(blueBalls)) {
                log.info("[校验投注号码格式]stakeNumber is not valid! duplicate error!" + stakeNumber);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.info("[校验投注号码格式_胆拖]异常" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 49%为当期奖金，2%为调节基金
     * 高等奖奖金＝奖金总额－固定奖奖金
     * 一等奖：
     * 当奖池资金低于1亿元时，
     * 奖金总额为当期奖金额减去固定奖总额后的75%与奖池中累积的奖金之和，单注奖金按注均分，单注最高限额封顶500万元。
     * 当奖池资金高于1亿元(含)且低于3亿元时，奖金总额包括两部分，
     * 一部分为当期奖金额减去固定奖总额后的58%与奖池中累积的奖金之和，单注奖金按注均分，单注最高限额封顶500万元；
     * 另一部分为当期奖金额减去固定奖总额后的17%，单注奖金按注均分，单注最高限额封顶500万元。
     * 当奖池资金高于3亿元（含）时，奖金总额包括两部分，
     * 第一部分为当期奖金额减去固定奖总额后的42%与奖池中累积的奖金之和，单注奖金按注均分，单注最高限额封顶500万元；
     * 第二部分为当期奖金额减去固定奖总额后的33%，单注奖金按注均分，单注最高限额封顶500万元。
     * 二等奖：
     * 奖金总额为当期奖金额减去固定奖总额后的18%，单注奖金按注均分，单注最高限额封顶500万元。
     * 三等奖：
     * 奖金总额为当期奖金额减去固定奖总额后的7%，单注奖金按注均分，单注最高限额封顶500万元。
     *
     * @param pool
     * @param sale
     * @param awardInfo
     * @param awardInfos
     * @return
     */
    @Override
    public Map<String, Object[]> getAwardLevelRatioMap() {
        Map<String, Object[]> awardLevelRationMap = new HashMap<>();
        List<String[]> ratio1List = new ArrayList<>();
        ratio1List.add(new String[]{"0.42", "0.33"});
        ratio1List.add(new String[]{"0.58", "0.17"});
        ratio1List.add(new String[]{"0.75"});
        awardLevelRationMap.put("1", new Object[]{ratio1List, new BigDecimal[]{CommonConstant.BONUS_30000_LIMIT,
                CommonConstant.BONUS_10000_LIMIT}});
        List<String[]> ratio2List = new ArrayList<>();
        ratio2List.add(new String[]{"0.18"});
        awardLevelRationMap.put("2", new Object[]{ratio2List});
        List<String[]> ratio3List = new ArrayList<>();
        ratio3List.add(new String[]{"0.07"});
        awardLevelRationMap.put("3", new Object[]{ratio3List});
        return awardLevelRationMap;
    }

    @Override
    public Set<String> getDefaultConsecutiveNumbers() {
        return DLT_FOUR_CONSECUTIVENUMBERS;
    }

    @Override
    public String getWinCondition(String level) {
        if (!WIN_CONDITION.containsKey(level)) {
            return "-";
        }
        return WIN_CONDITION.get(level);
    }

    /* 拆分
    *
    * "area":"河南(基本1注),湖南(基本1注追加1注),贵州(基本1注)"
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
            areaInfo = areaInfo.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_BRACKET_LEFT,
                    CommonConstant.SPACE_SPLIT_STR).replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMON_BRACKET_RIGHT, CommonConstant.SPACE_SPLIT_STR);
            String[] areaInfoArr = areaInfo.split(CommonConstant.SPACE_SPLIT_STR);
            String areaName = "";
            String baseCount = "-";
            String addCount = "-";
            if (areaInfoArr.length > 0) {
                areaName = areaInfoArr[0];
                String[] zhuShu = areaInfoArr[1].split("追加");
                baseCount = zhuShu[0].replace("基本", "");
                if (zhuShu.length == 2) {
                    addCount = zhuShu[1];
                }
            }

            tempArea.put("areaName", areaName);
            tempArea.put("baseCount", baseCount);
            tempArea.put("addCount", addCount);
            res.add(tempArea);
        }
        return res;
    }

    @Override
    public List<String> getGameAwardCondition() {
        List<String> awardCondition = new ArrayList<>();
        awardCondition.add("5+2");
        awardCondition.add("5+1");
        awardCondition.add("5+0");
        awardCondition.add("4+2");
        awardCondition.add("4+1");
        awardCondition.add("4+0");
        awardCondition.add("3+2");
        awardCondition.add("3+1");
        awardCondition.add("3+1");
        awardCondition.add("3+0");
        awardCondition.add("2+2");
        awardCondition.add("2+1");
        awardCondition.add("0+2");
        return awardCondition;
    }

    @Override
    public List<Object> awardCondition() {
        List<Object> awardConditionList = new ArrayList<>();

        Map<String, Object> awardCondition1 = new HashMap<>();
        awardCondition1.put("levelName", "一等奖");
        awardCondition1.put("levelCondition", "5+2");
        awardCondition1.put("awardAmount", "A元");
        awardConditionList.add(awardCondition1);

        Map<String, Object> awardCondition2 = new HashMap<>();
        awardCondition2.put("levelName", "二等奖");
        awardCondition2.put("levelCondition", "5+1");
        awardCondition2.put("awardAmount", "B元");
        awardConditionList.add(awardCondition2);

        Map<String, Object> awardCondition3 = new HashMap<>();
        awardCondition3.put("levelName", "三等奖");
        awardCondition3.put("levelCondition", "5+0，4+2");
        awardCondition3.put("awardAmount", "C元");
        awardConditionList.add(awardCondition3);

        Map<String, Object> awardCondition4 = new HashMap<>();
        awardCondition4.put("levelName", "四等奖");
        awardCondition4.put("levelCondition", "5+1，3+2");
        awardCondition4.put("awardAmount", "200元");
        awardConditionList.add(awardCondition4);

        Map<String, Object> awardCondition5 = new HashMap<>();
        awardCondition5.put("levelName", "五等奖");
        awardCondition5.put("levelCondition", "4+0，3+1，2+2");
        awardCondition5.put("awardAmount", "10元");
        awardConditionList.add(awardCondition5);

        Map<String, Object> awardCondition6 = new HashMap<>();
        awardCondition6.put("levelName", "六等奖");
        awardCondition6.put("levelCondition", "3+0，1+2，2+1，0+2");
        awardCondition6.put("awardAmount", "5元");
        awardConditionList.add(awardCondition6);

        return awardConditionList;
    }

    @Override
    public String getAwardDesc() {
        return "A是一等奖对应的浮动奖金，一般为500~1000万；B是二等奖对应的浮动奖金，一般为10~100万；C是三等奖对应的浮动奖金，一般为3000~8000。";
    }

    @Override
    public List<AwardInfo> getAwardAmountList() {
        return DLT_AWARD_AMOUNT_LIST;
    }
}
