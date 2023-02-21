package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.PeriodCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.enums.DltGameEnum;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.service.AwardService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.PredictNumService;
import com.mojieai.predict.service.TestAwardService;
import com.mojieai.predict.util.CommonUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.JedisCluster;

import java.math.BigDecimal;
import java.util.*;

@RequestMapping("/analysisTest")
@Controller
public class AnalysisTestController extends BaseController {

    @Autowired
    private AwardService awardService;
    @Autowired
    private TestAwardService testAwardService;

    @RequestMapping("/testRandom")
    @ResponseBody
    public Object testRandom(@RequestParam String num, @RequestParam String gameEn) {
        Set<String> numberList = new HashSet<>();
        if (gameEn.equals(GameConstant.DLT)) {
            for (int i = 0; i < Integer.parseInt(num); i++) {
                List<String> frontList = new ArrayList<>(DltGameEnum.DLT_FRONT_NUMBERS);
                Collections.shuffle(frontList, new Random(System.currentTimeMillis()));
                List<String> backList = new ArrayList<>(DltGameEnum.DLT_BACK_NUMBERS);
                Collections.shuffle(backList, new Random(System.currentTimeMillis()));
                String number = frontList.get(0) + " " + frontList.get(1) + " " + frontList.get(2) + " " + frontList
                        .get(3) + " " + frontList.get(4) + ":" + backList.get(0) + " " + backList.get(1);
                numberList.add(number);
            }
        }
        if (gameEn.equals(GameConstant.SSQ)) {
            for (int i = 0; i < Integer.parseInt(num); i++) {
                List<String> redList = new ArrayList<>(SsqGameEnum.SSQ_RED_NUMBERS);
                Collections.shuffle(redList, new Random(System.currentTimeMillis()));
                List<String> blueList = new ArrayList<>(SsqGameEnum.SSQ_BLUE_NUMBERS);
                Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
                String number = redList.get(0) + " " + redList.get(1) + " " + redList.get(2) + " " + redList.get(3) +
                        " " + redList.get(4) + " " + redList.get(5) + ":" + blueList.get(0);
                numberList.add(number);
            }
        }
        log.info(ArrayUtils.toString(numberList));
        List<AwardDetail> details = awardService.calcAwardDetail(GameCache.getGame(gameEn).getGameId(), 0,
                new ArrayList<>(numberList));
        Object[] info = calcBonus(details);
        BigDecimal totalBonus = (BigDecimal) info[0];
        int[] awardLevel = (int[]) info[1];
        List<AwardDetail> resultList = (List<AwardDetail>) info[2];
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalBonus", totalBonus);
        resultMap.put("awardLevel", awardLevel);
        resultMap.put("resultList", resultList);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/calcNumber")
    @ResponseBody
    public Object calcNumber(@RequestParam(defaultValue = "0") Integer num, @RequestParam String gameEn, @RequestParam
            String number) {
        List<String> numbers = new ArrayList<>();
        numbers.add(number);
        List<AwardDetail> details = awardService.calcAwardDetail(GameCache.getGame(gameEn).getGameId(), num, numbers);
        Object[] info = calcBonus(details);
        BigDecimal totalBonus = (BigDecimal) info[0];
        int[] awardLevel = (int[]) info[1];
        List<AwardDetail> resultList = (List<AwardDetail>) info[2];
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalBonus", totalBonus);
        resultMap.put("awardLevel", awardLevel);
        resultMap.put("resultList", resultList);
        return buildSuccJson(resultMap);
    }

    /*
        一等奖：1/17721088
        二等奖：15/17721088
        三等奖：162/17721088
        四等奖：7695/17721088
        五等奖：137475/17721088
        六等奖：1043640/17721088=5.9%*/
    @RequestMapping("/test")
    @ResponseBody
    public Object test(@RequestParam(defaultValue = "0") Integer num, @RequestParam String gameEn, @RequestParam
            (defaultValue = "0") Integer type) {
        List<AwardDetail> details = testAwardService.calcAwardDetail(GameCache.getGame(gameEn).getGameId(), num, type);
        Object[] info = calcAvgBonus(details, type);
        BigDecimal totalBonus = (BigDecimal) info[0];
        int[] awardLevel = (int[]) info[1];
        BigDecimal ratio = (BigDecimal) info[2];
        BigDecimal moneyRatio = (BigDecimal) info[3];
        BigDecimal totalMoney = (BigDecimal) info[4];
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalBonus", totalBonus);
        resultMap.put("totalMoney", totalMoney);
        resultMap.put("awardLevel", awardLevel);
        resultMap.put("ratio", ratio);
        resultMap.put("moneyRatio", moneyRatio);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/total")
    @ResponseBody
    public Object total(@RequestParam Integer type, @RequestParam(defaultValue = "10000") Integer
            count) {
        Long gameId = GameCache.getGame(GameConstant.SSQ).getGameId();
        List<GamePeriod> periods = PeriodCache.getPeriodMap().get(CommonUtil.mergeUnionKey(gameId, RedisConstant
                .LAST_ALL_OPEN_PERIOD));
        int numberCount = CommonConstant.TEST_AWARD_NUM_MAP.get(type);
        BigDecimal totalBonus = BigDecimal.ZERO;
        long levelCount = 0;
        for (int i = 0; i < count; i++) {
            List<AwardDetail> details = testAwardService.calcAwardDetail(gameId, 0, type);
            for (AwardDetail detail : details) {
                totalBonus = totalBonus.add(detail.getBonus());
                for (int m = 0; m < detail.getAwardLevel().length; m++) {
                    levelCount = levelCount + detail.getAwardLevel()[m];
                }
            }
        }
        BigDecimal totalNumber = new BigDecimal(numberCount).multiply(new BigDecimal(periods.size())).multiply(new
                BigDecimal(count));
        BigDecimal ratio = new BigDecimal(levelCount).divide(totalNumber, 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalMoney = new BigDecimal(2).multiply(totalNumber);
        BigDecimal moneyRatio = totalBonus.divide(totalMoney, 5, BigDecimal.ROUND_HALF_UP);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalBonus", totalBonus);
        resultMap.put("totalMoney", totalMoney);
        resultMap.put("ratio", ratio);
        resultMap.put("moneyRatio", moneyRatio);
        return buildSuccJson(resultMap);
    }

    //官方中奖率6.7094526024587%
    private Object[] calcAvgBonus(List<AwardDetail> details, int type) {
        BigDecimal totalBonus = BigDecimal.ZERO;
        int[] awardLevel = null;
        Long gameId = null;
        for (AwardDetail detail : details) {
            gameId = detail.getGameId();
            totalBonus = totalBonus.add(detail.getBonus());
            if (awardLevel == null) {
                awardLevel = Arrays.copyOf(detail.getAwardLevel(), detail.getAwardLevel().length);
            } else {
                for (int i = 0; i < awardLevel.length; i++) {
                    awardLevel[i] = awardLevel[i] + detail.getAwardLevel()[i];
                }
            }
        }
        int awardCount = 0;
        for (int count : awardLevel) {
            awardCount = awardCount + count;
        }
        List<GamePeriod> periods = PeriodCache.getPeriodMap().get(CommonUtil.mergeUnionKey(gameId, RedisConstant
                .LAST_ALL_OPEN_PERIOD));
        int numberCount = CommonConstant.TEST_AWARD_NUM_MAP.get(type);
        BigDecimal totalNumber = new BigDecimal(numberCount).multiply(new BigDecimal(periods.size()));
        BigDecimal ratio = new BigDecimal(awardCount).divide(totalNumber, 5, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalMoney = new BigDecimal(2).multiply(totalNumber);
        BigDecimal moneyRatio = totalBonus.divide(totalMoney, 5, BigDecimal.ROUND_HALF_UP);
        return new Object[]{totalBonus, awardLevel, ratio, moneyRatio, totalMoney};
    }

    private Object[] calcBonus(List<AwardDetail> details) {
        BigDecimal totalBonus = BigDecimal.ZERO;
        int[] awardLevel = null;
        List<AwardDetail> resultList = new ArrayList<>();
        for (AwardDetail detail : details) {
            totalBonus = totalBonus.add(detail.getBonus());
            if (detail.getBonus().compareTo(BigDecimal.ZERO) > 0) {
                resultList.add(detail);
            }
            if (awardLevel == null) {
                awardLevel = Arrays.copyOf(detail.getAwardLevel(), detail.getAwardLevel().length);
            } else {
                for (int i = 0; i < awardLevel.length; i++) {
                    awardLevel[i] = awardLevel[i] + detail.getAwardLevel()[i];
                }
            }
        }
        return new Object[]{totalBonus, awardLevel, resultList};
    }
}
