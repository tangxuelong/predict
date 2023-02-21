package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.TimelineEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AwardInfoService;
import com.mojieai.predict.service.PeriodService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.service.historyaward.HistoryAward;
import com.mojieai.predict.service.historyaward.HistoryAwardFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@RequestMapping("/history")
@Controller
public class HistoryAwardController extends BaseController {
    private final String SUM_TEXT_FIRST = "此号码中过一等奖";
    private final String SUM_TEXT_SECOND = "此号码中过二等奖";
    private final String SUM_TEXT_NOT = "此号码没有中过一等奖和二等奖";

    @Autowired
    private RedisService redisService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private AwardInfoService awardInfoService;

    @RequestMapping("/index")
    @ResponseBody
    public Object showText(@RequestParam String gameEn) {
        Map<String, Object> resultMap = new HashMap<>();

        Game game = GameCache.getGame(gameEn);
        /* 获取最近开奖的期次，当前展示是最近开奖的下一期*/
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod currentPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod
                .getPeriodId());
        HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);

        String showText = historyAward.getShowText(currentPeriod);
        resultMap.put("showText", showText);
        resultMap.put("periodId", currentPeriod.getPeriodId());

        // 期次
        if (gameEn.equals(GameConstant.FC3D)) {
            List<Integer> hundredOmitNumber = new ArrayList<>();
            List<Integer> tenOmitNumber = new ArrayList<>();
            List<Integer> oneOmitNumber = new ArrayList<>();
            // 如果当前时间大于开奖时间
            if (DateUtil.compareDate(new Date(), currentPeriod.getAwardTime())) {
                hundredOmitNumber = historyAward.getOmitNumber(lastOpenPeriod, RedisConstant.OMIT_KEY_MAP.get(gameEn
                        + RedisConstant.HUNDRED_AFTER));
                tenOmitNumber = historyAward.getOmitNumber(lastOpenPeriod, RedisConstant.OMIT_KEY_MAP.get(gameEn +
                        RedisConstant.TEN_AFTER));
                oneOmitNumber = historyAward.getOmitNumber(lastOpenPeriod, RedisConstant.OMIT_KEY_MAP.get(gameEn +
                        RedisConstant.ONE_AFTER));
            }
            Collections.replaceAll(hundredOmitNumber, -2, 0);
            Collections.replaceAll(tenOmitNumber, -2, 0);
            Collections.replaceAll(oneOmitNumber, -2, 0);
            resultMap.put("hundredOmitNumber", hundredOmitNumber);
            resultMap.put("tenOmitNumber", tenOmitNumber);
            resultMap.put("oneOmitNumber", oneOmitNumber);
        } else {
            List<Integer> redOmitNumber = new ArrayList<>();
            List<Integer> blueOmitNumber = new ArrayList<>();
            // 如果当前时间大于开奖时间
            if (DateUtil.compareDate(new Date(), currentPeriod.getAwardTime())) {
                redOmitNumber = historyAward.getOmitNumber(lastOpenPeriod, RedisConstant
                        .OMIT_KEY_MAP.get(gameEn + RedisConstant.RED_AFTER));
                blueOmitNumber = historyAward.getOmitNumber(lastOpenPeriod, RedisConstant
                        .OMIT_KEY_MAP.get(gameEn + RedisConstant.BLUE_AFTER));
            }
            Collections.replaceAll(redOmitNumber, -2, 0);
            Collections.replaceAll(blueOmitNumber, -2, 0);
            resultMap.put("redOmitNumber", redOmitNumber);
            resultMap.put("blueOmitNumber", blueOmitNumber);
        }

        return buildSuccJson(resultMap);
    }

    @RequestMapping("/award")
    @ResponseBody
    public Object award(@RequestParam String lotteryNumber, @RequestParam String gameEn, @RequestParam(required =
            false, defaultValue = "0") Integer periodNum) {
        int count = lotteryNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR).length;
        //支持单式、复制、胆拖，也支持多条号码一起算但投注号码条数不能超过指定量，不然响应速度慢
        if (count > IniCache.getIniIntValue(IniConstant.HISTORY_AWARD_MAX_BET_NUMBER, 1000)) {
            log.error("[HistoryAwardController]too many number." + count);
            return buildErrJson("号码记录过多，请重新选择");
        }
        Integer ifValidType = checkLotteryNumberType(lotteryNumber, gameEn);
        if (ifValidType == ResultConstant.ERROR) {
            log.error("[HistoryAwardController]lotteryNumber pattern is error." + count);
            return buildErrJson("号码格式有误");
        }
        Map<String, Object> finalResult = new HashMap<>();
        HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);
        /* 单式和复式 有 历史中奖情况*/
        if (!gameEn.equals(GameConstant.FC3D) && (ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE ||
                ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_MULTIPLE)) {

            Map<String, Object[]> resultMap = historyAward.getAllAwardRecords(lotteryNumber, periodNum);
            List<Map<String, Object>> resultList = new ArrayList<>();
            String currentPeriodId = "";
            if (resultMap.containsKey(CommonConstant.STR_MINUS_ONE)) //是否包含当前计算的期次
            {
                currentPeriodId = (String) resultMap.get(CommonConstant.STR_MINUS_ONE)[0];
                resultMap.remove(CommonConstant.STR_MINUS_ONE);
            }
            Integer[] resultScore = {0, 0, 0, 0, 0, 0};
            for (String key : resultMap.keySet()) {
                Map<String, Object> map = new HashMap<>();
                Integer awardNum = (Integer) resultMap.get(key)[0];
                map.put("levelId", key);
                map.put("awardNum", awardNum);
                map.put("levelName", CommonConstant.levelName[Integer.parseInt(key) - 1]);
                map.put("awardDetail", resultMap.get(key)[1]);//list<String[]> 数组由期次号、开奖时间、中奖号组成
                resultList.add(map);
                if (resultMap.get(key)[1] != null) {
                    resultScore[(new Integer(key) - 1)] = 1;
                }
            }
            if (resultList == null || resultList.size() == 0) {
                log.error("[HistoryAwardController]result is null！" + CommonUtil.mergeUnionKey(gameEn, lotteryNumber,
                        periodNum));
                return buildErrJson("计算历史中奖详情有误，请稍后再试");
            }
            Collections.sort(resultList, Comparator.comparing(map -> ((String) map.get("levelId"))));

            finalResult.put("content", resultList);
            finalResult.put("currentPeriodId", currentPeriodId);
            String sumText;
            if (resultScore[0] == 1 || resultScore[1] == 1) {
                sumText = resultScore[0] == 1 ? SUM_TEXT_FIRST : SUM_TEXT_SECOND;
            } else {
                sumText = SUM_TEXT_NOT;
            }
            finalResult.put("sumText", sumText);
        } else if (gameEn.equals(GameConstant.FC3D)) {
            Map<String, Object> resultCont = historyAward.getHistoryAwardContent(gameEn, lotteryNumber, 5);
            finalResult.put("content", resultCont.get("content"));
            finalResult.put("sumText", resultCont.get("awardNum"));
            finalResult.put("userNumber", lotteryNumber);
            finalResult.put("isMore", resultCont.get("isMore"));
            //历史遗漏和连出
            List<Map<String, Object>> historyOmit = historyAward.getNumHistoryOmit(lotteryNumber, gameEn);
            finalResult.put("historyOmit", historyOmit);
        }
        /* 单式有号码属性*/
        if (ifValidType == ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE) {
            List<Map<String, String>> numberProperties = historyAward.getNumberProperties(lotteryNumber, gameEn);
            finalResult.put("numberProperties", numberProperties);
        }
        //historyAward.buildLastNumberBehave(gameEn);
        List<Map<String, String>> lastNumberBehave = historyAward.getLastNumberBehave(lotteryNumber, ifValidType,
                gameEn);
        finalResult.put("lastNumberBehave", lastNumberBehave);

        // 分析号码不包含期次文案暂时不展示
        /*String showTitle = historyAward.getAwardTitle(gameEn);
        finalResult.put("showText", showTitle);*/
        return buildSuccJson(finalResult);
    }

    @RequestMapping("/historyAwardList")
    @ResponseBody
    public Object historyAwardList(@RequestParam String lotteryNumber, @RequestParam String gameEn) {
        Map<String, Object> finalResult = new HashMap<>();

        if (checkLotteryNumberType(lotteryNumber, gameEn) == ResultConstant.ERROR) {
            log.error("[HistoryAwardController]lotteryNumber pattern is error.");
            return buildErrJson("号码格式有误");
        }
        HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);

        Map<String, Object> resultCont = historyAward.getHistoryAwardContent(gameEn, lotteryNumber, -1);
        finalResult.put("content", resultCont.get("content"));
        finalResult.put("sumText", resultCont.get("awardNum"));
        finalResult.put("userNumber", lotteryNumber);
        finalResult.put("isMore", resultCont.get("isMore"));
        return buildSuccJson(finalResult);
    }


    private boolean checkLotteryNumber(String lotteryNumberStr, String gameEn) {
        String[] numArray = lotteryNumberStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);
        for (String lotteryNumber : numArray) {
            boolean result = GameFactory.getInstance().getGameBean(gameEn).checkLotteryNumberIfValid(lotteryNumber);
            if (!result) {
                return result;
            }
        }
        return Boolean.TRUE;
    }


    private Integer checkLotteryNumberType(String lotteryNumberStr, String gameEn) {
        String[] numArray = lotteryNumberStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);
        if (gameEn.equals(GameConstant.FC3D)) {
            numArray[0] = lotteryNumberStr;
        }
        for (String lotteryNumber : numArray) {
            Integer result = GameFactory.getInstance().getGameBean(gameEn).checkLotteryNumberTypeIfValid(lotteryNumber);
            return result;
        }
        return ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE_NOT;
    }

    @RequestMapping("/calculator/index")
    @ResponseBody
    public Object index(@RequestParam String gameEn) {
        List<GamePeriod> gamePeriods = PeriodRedis.getHistory30AwardPeriod(GameCache.getGame(gameEn).getGameId());
        List<Map<String, Object>> periods = new ArrayList<>();
        for (GamePeriod gamePeriod : gamePeriods) {
            if (gamePeriod == null || StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                continue;
            }
            Map temp = new HashMap();
            temp.put("periodId", gamePeriod.getPeriodId());
            temp.put("winNum", gamePeriod.getWinningNumbers());
            periods.add(temp);
        }
        return buildSuccJson(periods);
    }

    @RequestMapping("/calculator/danTuo")
    @ResponseBody
    public Object danTuo(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam Integer redDan,
                         @RequestParam Integer redTuo, @RequestParam Integer blueDan, @RequestParam Integer blueTuo,
                         @RequestParam Integer bingoRedDan, @RequestParam Integer bingoRedTuo, @RequestParam Integer
                                 bingoBlueDan, @RequestParam Integer bingoBlueTuo) {
        Game game = GameCache.getGame(gameEn);
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameEn);
        int[] awardLevels = ag.analyseBidAwardLevels(redDan, redTuo, blueDan, blueTuo, bingoRedDan, bingoRedTuo,
                bingoBlueDan, bingoBlueTuo, periodId);
        return buildSuccJson(generateResultMap(awardLevels, ag, game, periodId));
    }

    @RequestMapping("/calculator/normal")
    @ResponseBody
    public Object normal(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam String
            lotteryNumber) {
        Game game = GameCache.getGame(gameEn);
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameEn);
        int[] awardLevels = ag.analyseBidAwardLevels(lotteryNumber, PeriodRedis.getPeriodByGameIdAndPeriod(game
                .getGameId(), periodId));
        return buildSuccJson(generateResultMap(awardLevels, ag, game, periodId));
    }

    private Map<String, Object> generateResultMap(int[] awardLevels, AbstractGame ag, Game game, String periodId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<AwardInfo> awardInfos = ag.getDefaultAwardInfoList();//这里赋值默认奖级，可能在没有抓取到奖级的时候被再次赋值为null
        List<String> resultList = new ArrayList<>();
        BigDecimal calculateAward = BigDecimal.ZERO;
        Boolean changeInfoFlag = Boolean.FALSE;
        Boolean defaultAward = Boolean.FALSE;
        int level = -1;//默认没有浮动奖设置为-1，后续如果发现有大奖，把最大的奖级赋值给level
        int floatLevelCount = AbstractGame.floatAwardLevelCount(game.getGameEn());
        DecimalFormat df = new DecimalFormat("#.##");
        for (int i = 0; i < awardLevels.length; i++) {
            if (i < floatLevelCount && awardLevels[i] > 0 && !changeInfoFlag) {
                awardInfos = redisService.kryoHget(RedisConstant.getAwardInfoKey(game.getGameId()), periodId,
                        ArrayList.class);
                if (awardInfos == null) {
                    awardInfos = awardInfoService.getAwardInfos(game.getGameId(), periodId);
                }
                if (awardInfos == null) {//仍然取不到，说明还没有奖级信息
                    awardInfos = ag.getDefaultAwardInfoList();
                    level = i;
                }
                changeInfoFlag = Boolean.TRUE;
            }
            for (AwardInfo awardInfo : awardInfos) {
                boolean flag = false;
                if (game.getGameEn().equals(GameConstant.SSQ) && awardInfo.getAwardLevel().equals(String.valueOf(i +
                        1))) {
                    flag = true;
                } else if (game.getGameEn().equals(GameConstant.DLT)) {
                    if (i < 4 && awardInfo.getAwardLevel().equals(String.valueOf(i + 1))) {
                        flag = true;
                    } else if (i == 4 && awardInfo.getAwardLevel().equals(String.valueOf(i + 2))) {
                        flag = true;
                    } else if (i == 5 && awardInfo.getAwardLevel().equals(String.valueOf(i + 3))) {
                        flag = true;
                    }
                }

                if (flag) {
                    String bonusStr = awardInfo.getBonus().compareTo(new BigDecimal(-1)) == 0 ? CommonConstant
                            .COMMON_DASH_STR : df.format(awardInfo.getBonus());
                    resultList.add(new StringBuffer().append(awardInfo.getLevelName()).append(CommonConstant
                            .COMMA_SPLIT_STR).append(ag.getWinCondition(awardInfo.getAwardLevel())).append
                            (CommonConstant.COMMA_SPLIT_STR).append(bonusStr).append(CommonConstant.COMMA_SPLIT_STR)
                            .append(awardLevels[i]).toString());
                    calculateAward = calculateAward.add(awardInfo.getBonus().multiply(new BigDecimal(awardLevels[i])));
                    if (level == i && awardInfo.getBonus().compareTo(new BigDecimal(-1)) == 0) {
                        int end = awardInfo.getLevelName().indexOf(CommonConstant.COMMON_SQUARE_BRACKET_LEFT);
                        if (end < 0) {
                            end = awardInfo.getLevelName().length();
                        }
                        resultMap.put("calculateAward", awardInfo.getLevelName().substring(0, end));
                        resultMap.put("calculateAwardCn", "奖金暂未公布");
                        defaultAward = Boolean.TRUE;
                    }
                }
            }
        }
        if (!defaultAward) {//说明没有大奖或者有大奖没有奖级信息
            resultMap.put("calculateAward", calculateAward);
            resultMap.put("calculateAwardCn", "元");
        }
        resultMap.put("resultList", resultList);
        return resultMap;
    }

    @RequestMapping("/samePeriod/index")
    @ResponseBody
    public Object samePeriodIndex(@RequestParam String gameEn) {
        Map<String, Object> resultMap = new HashMap<>();
        Game game = GameCache.getGame(gameEn);
        String key = TimelineEnum.AWARD_TIME.getTimelineKey(game.getGameId());
        List<String> futureList = redisService.kryoZRangeByScoreGet(key, System.currentTimeMillis(), Long.MAX_VALUE,
                0, 5, String.class);
        Collections.reverse(futureList);
        List<String> historyList = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, System.currentTimeMillis
                (), 0, 5, String.class);
        Set<String> periods = new LinkedHashSet<>();
        for (String future : futureList) {
            periods.add(future);
        }
        String current = PeriodRedis.getCurrentPeriod(game.getGameId()).getPeriodId();
        periods.add(current);
        for (String history : historyList) {
            periods.add(history);
        }
        resultMap.put("periods", periods);
        resultMap.put("defaultPeriod", current);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/samePeriod/query")
    @ResponseBody
    public Object samePeriod(@RequestParam String gameEn, @RequestParam String periodId) {
        Game game = GameCache.getGame(gameEn);
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameEn);
        String key = RedisConstant.PREFIX_SAME_PERIOD + gameEn + CommonConstant.COMMON_COLON_STR + periodId;
        List<GamePeriod> periods = redisService.kryoGet(key, ArrayList.class);
        if (periods == null) {
            int yearLength = ag.getPeriodDateFormat().length();
            String periodStr = periodId.substring(yearLength);
            String yearStr = periodId.substring(0, yearLength);
            yearStr = yearStr.length() != 4 ? "20" + yearStr : yearStr;// 如果不满4位前面补20
            Set<String> periodIds = new HashSet<>();
            for (int i = 2003; i <= Integer.parseInt(yearStr); i++) {//这里有点硬编码，双色球最早是2003年开始，其他彩种都在这个之后
                String periodYear = String.valueOf(i);
                periodIds.add(periodYear.substring(periodYear.length() - yearLength, periodYear.length()) + periodStr);
            }
            periods = periodService.getPeriodsByGameIdAndPeriods(game.getGameId(), periodIds);
            Collections.sort(periods, (o1, o2) -> o2.getPeriodId().compareTo(o1.getPeriodId()));

            int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), PeriodRedis
                    .getCurrentPeriod(game.getGameId()).getEndTime());
            redisService.kryoSetEx(key, expireSeconds, periods);
        }
        //这里可能periodId已经开奖，但是缓存是旧的没有更新，所以需要拿出来list里面的第一个元素比对下
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), periodId);
        GamePeriod first = periods.get(0);
        if (!first.getPeriodId().equals(period.getPeriodId()) && StringUtils.isNotBlank(period.getWinningNumbers())) {
            periods.add(0, period);
        }
        GamePeriod firstShow = periods.get(0);
        if (StringUtils.isBlank(firstShow.getWinningNumbers())) {
            periods.remove(0);
        }
        return buildSuccJson(periods);
    }
}
